#!/bin/bash

# stop-services.sh
# This script stops all services started by the Onboarding App.

# Function to gracefully stop a process
graceful_stop() {
    local pid=$1
    local service=$2
    local timeout=30  # 30 seconds timeout

    if [ -n "$pid" ]; then
        echo "Stopping $service (PID: $pid)..."
        kill $pid  # Try graceful shutdown first
        
        # Wait for process to stop
        for i in $(seq 1 $timeout); do
            if ! kill -0 $pid 2>/dev/null; then
                echo "$service stopped gracefully"
                return 0
            fi
            sleep 1
        done
        
        # Force kill if still running
        echo "Force stopping $service..."
        kill -9 $pid
        echo "$service force stopped"
    else
        echo "No process found for $service"
    fi
}

# Detect OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    IS_MACOS=true
else
    IS_MACOS=false
fi

echo "Stopping all services..."

# Stop Spring Boot applications
echo "Stopping Spring Boot applications..."
for port in 8081 8082 8083 8084 8085; do
    pid=$(lsof -ti :$port)
    service="Service on port $port"
    graceful_stop "$pid" "$service"
done

# Stop UI (React dev server)
echo "Stopping UI..."
pid=$(lsof -ti :3000)
graceful_stop "$pid" "UI"

# Stop Redis
echo "Stopping Redis..."
pid=$(pgrep redis-server)
if [ -n "$pid" ]; then
    redis-cli shutdown
    echo "Redis stopped gracefully"
else
    echo "Redis is not running"
fi

# Stop Nginx
echo "Stopping Nginx..."
if pgrep nginx > /dev/null; then
    if [ "$IS_MACOS" = true ]; then
        # On macOS, use brew services
        brew services stop nginx
    else
        # On Linux, use system service
        sudo nginx -s stop
    fi
    echo "Nginx stopped gracefully"
else
    echo "Nginx is not running"
fi

# After stopping, check all relevant ports
PORTS=(8081 8082 8083 8084 8085 3000 6379 80)
for port in "${PORTS[@]}"; do
    if lsof -i :$port | grep LISTEN > /dev/null; then
        echo "Warning: Port $port is still in use after stop. Attempting forceful kill."
        PID=$(lsof -ti :$port)
        if [ -n "$PID" ]; then
            kill -9 $PID
            sleep 1
            if lsof -i :$port | grep LISTEN > /dev/null; then
                echo "Error: Could not free port $port after forceful kill. Manual intervention may be required."
            else
                echo "Port $port is now free."
            fi
        fi
    else
        echo "Port $port is free."
    fi
    # Clean up PID files if present
    case $port in
      8081) rm -f logs/.github.pid ;;
      8082) rm -f logs/.google.pid ;;
      8083) rm -f logs/.slack.pid ;;
      8084) rm -f logs/.jira.pid ;;
      8085) rm -f logs/.api.pid ;;
      3000) rm -f logs/.ui.pid ;;
    esac
    # Log action
    echo "Checked and cleaned up for port $port" >> logs/stop-services.log
done

# Cleanup temporary files
echo "Cleaning up..."
rm -f logs/*.log
rm -f /tmp/*.pid

echo "All services stopped." 