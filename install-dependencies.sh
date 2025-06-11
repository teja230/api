#!/bin/bash

# Installs required packages for running the Onboarding App.
# This script supports both Debian/Ubuntu (using apt-get) and macOS (using Homebrew).

set -e

# Function to check if running as root
is_root() {
    [ "$(id -u)" -eq 0 ]
}

# Function to ensure sudo credentials are cached (only for Linux)
ensure_sudo() {
    if [[ "$OSTYPE" != "darwin"* ]] && ! is_root; then
        # Cache sudo credentials for the session
        sudo -v
        # Keep sudo credentials fresh
        while true; do
            sudo -n true
            sleep 60
            kill -0 "$$" 2>/dev/null || exit
        done &
    fi
}

# Function to run command with sudo if needed (only for Linux)
run_with_sudo() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        "$@"
    elif is_root; then
        "$@"
    else
        sudo "$@"
    fi
}

# Ensure sudo credentials are cached at the start (only for Linux)
ensure_sudo

# Detect OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    if ! command -v brew >/dev/null; then
        echo "Error: Homebrew is not installed. Please install it first." >&2
        exit 1
    fi

    echo "Installing dependencies using Homebrew..."
    # Never use sudo for Homebrew commands on macOS
    brew update
    brew install openjdk@17 maven node@18 redis nginx

    # Only use sudo for the Java symlink
    if [ -d "/opt/homebrew/opt/openjdk@17" ]; then
        sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    fi
else
    # Linux (Debian/Ubuntu)
    if ! command -v apt-get &> /dev/null; then
        echo "Error: This script only supports Debian/Ubuntu systems" >&2
        exit 1
    fi
    echo "Installing dependencies using apt-get..."
    run_with_sudo apt-get update
    run_with_sudo apt-get install -y openjdk-17-jdk maven nodejs npm redis-server nginx

    # Install Node.js 18 from NodeSource
    curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
    run_with_sudo apt-get install -y nodejs
fi

# Verify installations
echo "Verifying installations..."
echo "----------------------------------------"

# Check Java version
echo "Java version:"
java -version
JAVA_MAJOR=$(java -version 2>&1 | awk -F[\".] '/version/ {print $2}')
if [ "$JAVA_MAJOR" -lt 17 ]; then
    echo "Error: Java 17 or higher is required" >&2
    exit 1
fi

# Check Maven version
echo -e "\nMaven version:"
mvn --version
MAVEN_MAJOR=$(mvn --version | awk '/Apache Maven/ {split($3,a,"."); print a[1]}')
MAVEN_MINOR=$(mvn --version | awk '/Apache Maven/ {split($3,a,"."); print a[2]}')
if [ "$MAVEN_MAJOR" -lt 3 ] || { [ "$MAVEN_MAJOR" -eq 3 ] && [ "$MAVEN_MINOR" -lt 6 ]; }; then
    echo "Error: Maven 3.6 or higher is required" >&2
    exit 1
fi

# Check Node.js version
echo -e "\nNode.js version:"
node --version
NODE_MAJOR=$(node --version | sed 's/v\([0-9]*\).*/\1/')
if [ "$NODE_MAJOR" -lt 18 ]; then
    echo "Error: Node.js 18 or higher is required" >&2
    exit 1
fi

# Check npm version
echo -e "\nnpm version:"
npm --version
NPM_MAJOR=$(npm --version | cut -d. -f1)
if [ "$NPM_MAJOR" -lt 8 ]; then
    echo "Error: npm 8 or higher is required" >&2
    exit 1
fi

# Check Redis version
echo -e "\nRedis version:"
redis-cli --version
REDIS_MAJOR=$(redis-cli --version | awk '{split($2,a,"."); print a[1]}')
if [ "$REDIS_MAJOR" -lt 6 ]; then
    echo "Error: Redis 6 or higher is required" >&2
    exit 1
fi

# Check Nginx version
echo -e "\nNginx version:"
nginx -v 2>&1
NGINX_MAJOR=$(nginx -v 2>&1 | awk -F/ '{print $2}' | cut -d. -f1)
NGINX_MINOR=$(nginx -v 2>&1 | awk -F/ '{print $2}' | cut -d. -f2)
if [ "$NGINX_MAJOR" -lt 1 ] || { [ "$NGINX_MAJOR" -eq 1 ] && [ "$NGINX_MINOR" -lt 18 ]; }; then
    echo "Error: Nginx 1.18 or higher is required" >&2
    exit 1
fi

echo "----------------------------------------"
echo "All dependencies installed successfully!"
