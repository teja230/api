#!/bin/bash

# Check dependencies
echo "Checking dependencies..."

# Function to check if a command exists
check_command() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check Java version
check_java() {
    if ! check_command java; then
        return 1
    fi
    JAVA_MAJOR=$(java -version 2>&1 | awk -F[\".] '/version/ {print $2}')
    [ "$JAVA_MAJOR" -ge 17 ]
}

# Function to check Node.js version
check_node() {
    if ! check_command node; then
        return 1
    fi
    NODE_MAJOR=$(node --version | sed 's/v\([0-9]*\).*/\1/')
    [ "$NODE_MAJOR" -ge 18 ]
}

# Function to check Maven version
check_maven() {
    if ! check_command mvn; then
        return 1
    fi
    MAVEN_MAJOR=$(mvn --version | awk '/Apache Maven/ {split($3,a,"."); print a[1]}')
    MAVEN_MINOR=$(mvn --version | awk '/Apache Maven/ {split($3,a,"."); print a[2]}')
    [ "$MAVEN_MAJOR" -gt 3 ] || { [ "$MAVEN_MAJOR" -eq 3 ] && [ "$MAVEN_MINOR" -ge 6 ]; }
}

# Function to check Redis
check_redis() {
    check_command redis-cli
}

# Function to check Nginx
check_nginx() {
    check_command nginx
}

# Check all dependencies
MISSING_DEPS=0
echo "Checking required dependencies..."

if ! check_java; then
    echo "Java 17 or higher is required"
    MISSING_DEPS=1
else
    echo "Found Java version: $(java -version 2>&1 | awk '/version/ {print $3}')"
fi

if ! check_node; then
    echo "Node.js 18 or higher is required"
    MISSING_DEPS=1
else
    echo "Found Node.js version: $(node --version)"
fi

if ! check_maven; then
    echo "Maven 3.6 or higher is required"
    MISSING_DEPS=1
else
    echo "Found Maven version: $(mvn --version | awk '/Apache Maven/ {print $3}')"
fi

if ! check_redis; then
    echo "Redis is required"
    MISSING_DEPS=1
fi

if ! check_nginx; then
    echo "Nginx is required"
    MISSING_DEPS=1
fi

# Only run install-dependencies.sh if any dependencies are missing
if [ $MISSING_DEPS -eq 1 ]; then
    echo "Some dependencies are missing. Running install-dependencies.sh..."
    if [ -f "install-dependencies.sh" ]; then
        chmod +x install-dependencies.sh
        if ! ./install-dependencies.sh; then
            echo "Error: Failed to run install-dependencies.sh" >&2
            exit 1
        fi
    else
        echo "Error: install-dependencies.sh not found" >&2
        exit 1
    fi
else
    echo "All dependencies are satisfied."
fi

# Check configuration files
echo "Checking configuration files..."
if [ ! -f "nginx.conf" ]; then
    echo "Error: nginx.conf not found"
    exit 1
fi

# Check if Redis is installed and configured
if [ ! -f "/usr/local/etc/redis.conf" ] && [ ! -f "/etc/redis/redis.conf" ]; then
    echo "Warning: Redis configuration file not found. Using default configuration."
fi

# Check port availability before starting services
PORTS=(8081 8082 8083 8084 8085 3000 80)
for port in "${PORTS[@]}"; do
    if lsof -i :$port | grep LISTEN > /dev/null; then
        echo "Error: Port $port is already in use by the following process(es):" | tee -a start-services.log
        lsof -i :$port | grep LISTEN | tee -a start-services.log
        echo "Aborting startup. Please free the port and try again." | tee -a start-services.log
        exit 1
    else
        echo "Port $port is free." | tee -a start-services.log
    fi
    echo "Checked port $port before startup" >> start-services.log
done

# Special handling for Redis (6379)
if lsof -i :6379 | grep LISTEN > /dev/null; then
    if lsof -i :6379 | grep 'redis-ser' > /dev/null; then
        echo "Redis is already running on port 6379. Skipping Redis startup." | tee -a start-services.log
    else
        echo "Error: Port 6379 is in use by a non-Redis process:" | tee -a start-services.log
        lsof -i :6379 | grep LISTEN | tee -a start-services.log
        echo "Aborting startup. Please free port 6379 and try again." | tee -a start-services.log
        exit 1
    fi
else
    echo "Port 6379 is free. Redis will be started by the script." | tee -a start-services.log
fi

# Start Nginx if not running
echo "Starting Nginx..."
if ! pgrep nginx > /dev/null; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # On macOS, use Homebrew's Nginx
        if ! nginx -t; then
            echo "Error: Nginx configuration test failed"
            exit 1
        fi
        nginx
        if [ $? -ne 0 ]; then
            echo "Failed to start Nginx"
            exit 1
        fi
    else
        # On Linux, use system Nginx with sudo
        if ! sudo nginx -t; then
            echo "Error: Nginx configuration test failed"
            exit 1
        fi
        sudo nginx
        if [ $? -ne 0 ]; then
            echo "Failed to start Nginx"
            exit 1
        fi
    fi
    echo "Nginx started successfully"
else
    echo "Nginx is already running"
fi

# Function to get port from application.properties or application.yml
get_port() {
    local config_file=$1
    if [ -f "$config_file" ]; then
        if [[ "$config_file" == *.yml ]]; then
            # For YAML files
            grep "^[[:space:]]*port:" "$config_file" | head -n 1 | awk '{print $2}'
        else
            # For properties files
            grep "^server.port=" "$config_file" | cut -d'=' -f2
        fi
    else
        echo "Error: $config_file not found"
        exit 1
    fi
}

# Function to get context path from application.properties or application.yml
get_context_path() {
    local config_file=$1
    if [ -f "$config_file" ]; then
        if [[ "$config_file" == *.yml ]]; then
            # For YAML files
            grep "^[[:space:]]*context-path:" "$config_file" | head -n 1 | awk '{print $2}'
        else
            # For properties files
            grep "^server.servlet.context-path=" "$config_file" | cut -d'=' -f2
        fi
    else
        echo ""
    fi
}

# Function to check if a service is healthy
check_health() {
    local url=$1
    local max_retries=3
    local retry=0
    local wait_time=5

    echo "Checking health for $url"
    
    while [ $retry_count -lt $max_retries ]; do
        if curl -s "$url/actuator/health" | grep -q '"status":"UP"'; then
            echo "Service at $url is healthy!"
            return 0
        fi
        retry=$((retry + 1))
        if [ $retry -lt $max_retries ]; then
            echo "Service not healthy yet. Retry $retry/$max_retries"
            sleep $wait_time
        fi
    done
    echo "Service at $url failed to become healthy after $max_retries retries"
    return 1
}

# Kill any existing Java processes
echo "Stopping any existing services..."
pkill -f "spring-boot:run"

# Start Redis if not running
echo "Starting Redis..."
if lsof -i :6379 > /dev/null; then
    echo "Redis is already running on port 6379. Skipping Redis startup."
else
    if ! pgrep redis-server > /dev/null; then
        redis-server &
        REDIS_PID=$!
        echo "Waiting for Redis to start..."
        for i in {1..5}; do
            if redis-cli ping > /dev/null; then
                echo "Redis started successfully"
                break
            fi
            if [ $i -eq 5 ]; then
                echo "Failed to start Redis after 5 attempts"
                exit 1
            fi
            sleep 1
        done
    else
        echo "Redis is already running (process found)"
    fi
fi

# Start Nginx if not running
echo "Starting Nginx..."
if ! pgrep nginx > /dev/null; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # On macOS, use Homebrew's Nginx
        if ! nginx -t; then
            echo "Error: Nginx configuration test failed"
            exit 1
        fi
        nginx
        if [ $? -ne 0 ]; then
            echo "Failed to start Nginx"
            exit 1
        fi
    else
        # On Linux, use system Nginx with sudo
        if ! sudo nginx -t; then
            echo "Error: Nginx configuration test failed"
            exit 1
        fi
        sudo nginx
        if [ $? -ne 0 ]; then
            echo "Failed to start Nginx"
            exit 1
        fi
    fi
    echo "Nginx started successfully"
else
    echo "Nginx is already running"
fi

# Get ports and context paths from configuration files
echo "Reading configurations..."
API_PORT=$(get_port "api-layer/src/main/resources/application.yml")
API_CONTEXT=$(get_context_path "api-layer/src/main/resources/application.yml")
GITHUB_PORT=$(get_port "integrations/github/src/main/resources/application.properties")
GITHUB_CONTEXT=$(get_context_path "integrations/github/src/main/resources/application.properties")
GOOGLE_PORT=$(get_port "integrations/google/src/main/resources/application.properties")
GOOGLE_CONTEXT=$(get_context_path "integrations/google/src/main/resources/application.properties")
SLACK_PORT=$(get_port "integrations/slack/src/main/resources/application.properties")
SLACK_CONTEXT=$(get_context_path "integrations/slack/src/main/resources/application.properties")
JIRA_PORT=$(get_port "integrations/jira/src/main/resources/application.properties")
JIRA_CONTEXT=$(get_context_path "integrations/jira/src/main/resources/application.properties")

echo "Using configurations:"
echo "API Layer: port=$API_PORT, context=$API_CONTEXT"
echo "GitHub: port=$GITHUB_PORT, context=$GITHUB_CONTEXT"
echo "Google: port=$GOOGLE_PORT, context=$GOOGLE_CONTEXT"
echo "Slack: port=$SLACK_PORT, context=$SLACK_CONTEXT"
echo "Jira: port=$JIRA_PORT, context=$JIRA_CONTEXT"

# Start services in the background with proper delays
echo "Starting services..."

# Start API Layer
echo "Starting API Layer on port $API_PORT..."
cd api-layer
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=$API_PORT" > ../api-layer.log 2>&1 &
API_PID=$!
cd ..
echo $API_PID > .api.pid
sleep 10  # Wait for API Layer to initialize

# Start GitHub Service
echo "Starting GitHub Service on port $GITHUB_PORT..."
cd integrations/github
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=$GITHUB_PORT" > ../../github.log 2>&1 &
GITHUB_PID=$!
cd ../..
echo $GITHUB_PID > .github.pid
sleep 5  # Wait for GitHub service to initialize

# Start Google Service
echo "Starting Google Service on port $GOOGLE_PORT..."
cd integrations/google
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=$GOOGLE_PORT" > ../../google.log 2>&1 &
GOOGLE_PID=$!
cd ../..
echo $GOOGLE_PID > .google.pid
sleep 5  # Wait for Google service to initialize

# Start Slack Service
echo "Starting Slack Service on port $SLACK_PORT..."
cd integrations/slack
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=$SLACK_PORT" > ../../slack.log 2>&1 &
SLACK_PID=$!
cd ../..
echo $SLACK_PID > .slack.pid
sleep 5  # Wait for Slack service to initialize

# Start Jira Service
echo "Starting Jira Service on port $JIRA_PORT..."
cd integrations/jira
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=$JIRA_PORT" > ../../jira.log 2>&1 &
JIRA_PID=$!
cd ../..
echo $JIRA_PID > .jira.pid
sleep 5  # Wait for Jira service to initialize

# Start UI application
echo "Starting UI application..."
cd ui-app
npm start > ../ui.log 2>&1 &
UI_PID=$!
cd ..
echo $UI_PID > .ui.pid

# Wait for services to start
echo "Waiting for services to start"
sleep 10  # Initial delay to allow services to start

# Check health of backend services
echo "Checking backend services health..."
MAX_RETRIES=8
RETRY_COUNT=0
SERVICES_READY=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ] && [ "$SERVICES_READY" = false ]; do
    echo "Attempt $((RETRY_COUNT + 1))/$MAX_RETRIES to check services..."
    
    # Direct API Layer health check (port 8085)
    if curl -s "http://localhost:8085/actuator/health" | grep -q '"status":"UP"'; then
        echo "API Layer (direct) is ready"
    else
        echo "API Layer (direct) not ready yet"
    fi

    # Nginx-proxied API Layer health check (port 8080)
    if curl -s "http://localhost:8080/api/actuator/health" | grep -q '"status":"UP"'; then
        echo "API Layer (nginx) is ready"

        # Check GitHub Service
        if curl -s "http://localhost:8080/github/actuator/health" | grep -q '"status":"UP"'; then
            echo "GitHub Service is ready"
            
            # Check Google Service
            if curl -s "http://localhost:8080/google/actuator/health" | grep -q '"status":"UP"'; then
                echo "Google Service is ready"
                
                # Check Slack Service
                if curl -s "http://localhost:8080/slack/actuator/health" | grep -q '"status":"UP"'; then
                    echo "Slack Service is ready"
                    
                    # Check Jira Service
                    if curl -s "http://localhost:8080/jira/actuator/health" | grep -q '"status":"UP"'; then
                        echo "Jira Service is ready"
                        SERVICES_READY=true
                    else
                        echo "Jira Service not ready yet"
                    fi
                else
                    echo "Slack Service not ready yet"
                fi
            else
                echo "Google Service not ready yet"
            fi
        else
            echo "GitHub Service not ready yet"
        fi
    else
        echo "API Layer (nginx) not ready yet"
    fi
    
    if [ "$SERVICES_READY" = false ]; then
        RETRY_COUNT=$((RETRY_COUNT + 1))
        if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
            echo "Waiting 10 seconds before next attempt..."
            sleep 10
        fi
    fi
done

if [ "$SERVICES_READY" = true ]; then
    echo "All backend services are ready. UI will be available at http://localhost:3000"
else
    echo "Warning: Some services failed to start properly. Check the logs for details."
fi

# Set up cleanup trap
cleanup() {
    echo "Shutting down services..."
    for pid_file in .*.pid; do
        if [ -f "$pid_file" ]; then
            pid=$(cat "$pid_file")
            kill $pid 2>/dev/null
            rm -f "$pid_file"
        fi
    done
    if [[ "$OSTYPE" == "darwin"* ]]; then
        nginx -s stop
    else
        sudo nginx -s stop
    fi
}
trap cleanup EXIT
wait

