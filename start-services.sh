#!/bin/bash

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
    local context_path=$2
    local max_retries=$3
    local retry_count=0
    local wait_time=5

    # Normalize context path: if empty or '/', use empty string
    if [[ -z "$context_path" || "$context_path" == "/" ]]; then
        context_path=""
    fi

    echo "Checking health for $url$context_path"
    
    while [ $retry_count -lt $max_retries ]; do
        if curl -s "$url$context_path/actuator/health" | grep -q '"status":"UP"'; then
            echo "Service at $url$context_path is healthy!"
            return 0
        fi
        
        echo "Service not healthy yet. Retry $((retry_count + 1))/$max_retries"
        retry_count=$((retry_count + 1))
        sleep $wait_time
    done
    
    echo "Service at $url$context_path failed to become healthy after $max_retries retries"
    return 1
}

# Kill any existing Java processes
echo "Stopping any existing services..."
pkill -f "spring-boot:run"

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

# Start services in the background
echo "Starting services..."

# Start API Layer
echo "Starting API Layer on port $API_PORT..."
mvn spring-boot:run -pl api-layer -Dspring-boot.run.jvmArguments="-Dserver.port=$API_PORT" &
API_PID=$!

# Start GitHub Service
echo "Starting GitHub Service on port $GITHUB_PORT..."
mvn spring-boot:run -pl integrations/github -Dspring-boot.run.jvmArguments="-Dserver.port=$GITHUB_PORT" &
GITHUB_PID=$!

# Start Google Service
echo "Starting Google Service on port $GOOGLE_PORT..."
mvn spring-boot:run -pl integrations/google -Dspring-boot.run.jvmArguments="-Dserver.port=$GOOGLE_PORT" &
GOOGLE_PID=$!

# Start Slack Service
echo "Starting Slack Service on port $SLACK_PORT..."
mvn spring-boot:run -pl integrations/slack -Dspring-boot.run.jvmArguments="-Dserver.port=$SLACK_PORT" &
SLACK_PID=$!

# Start Jira Service
echo "Starting Jira Service on port $JIRA_PORT..."
mvn spring-boot:run -pl integrations/jira -Dspring-boot.run.jvmArguments="-Dserver.port=$JIRA_PORT" &
JIRA_PID=$!

# Wait for services to start
echo "Waiting for services to start..."
sleep 20

# Check health of each service
echo "Checking service health..."
check_health "http://localhost:$API_PORT" "$API_CONTEXT" 12 || exit 1
check_health "http://localhost:$GITHUB_PORT" "$GITHUB_CONTEXT" 12 || exit 1
check_health "http://localhost:$GOOGLE_PORT" "$GOOGLE_CONTEXT" 12 || exit 1
check_health "http://localhost:$SLACK_PORT" "$SLACK_CONTEXT" 12 || exit 1
check_health "http://localhost:$JIRA_PORT" "$JIRA_CONTEXT" 12 || exit 1

echo "All services are up and running!"

# Keep script running and handle cleanup on exit
trap "kill $API_PID $GITHUB_PID $GOOGLE_PID $SLACK_PID $JIRA_PID" EXIT
wait 