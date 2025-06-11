#!/bin/bash

# Function to check if a command exists and its version
check_command() {
    local cmd=$1
    local min_version=$2
    local version_cmd=$3

    if ! command -v $cmd &> /dev/null; then
        echo "Error: $cmd is required but not installed."
        return 1
    fi

    if [ -n "$min_version" ]; then
        local version=$($version_cmd)
        if [ $? -ne 0 ]; then
            echo "Error: Could not determine $cmd version"
            return 1
        fi
        echo "Found $cmd version: $version"
    fi

    return 0
}

# Function to check Java version
check_java_version() {
    local version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [ $? -ne 0 ]; then
        echo "Error: Could not determine Java version"
        return 1
    fi
    echo "Found Java version: $version"
    return 0
}

# Function to check Node.js version
check_node_version() {
    local version=$(node --version)
    if [ $? -ne 0 ]; then
        echo "Error: Could not determine Node.js version"
        return 1
    fi
    echo "Found Node.js version: $version"
    return 0
}

# Function to check Maven version
check_maven_version() {
    local version=$(mvn --version | grep "Apache Maven" | awk '{print $3}')
    if [ $? -ne 0 ]; then
        echo "Error: Could not determine Maven version"
        return 1
    fi
    echo "Found Maven version: $version"
    return 0
}

echo "Checking required dependencies..."

# Check Java
if ! check_java_version; then
    echo "Java is not installed or version check failed"
    exit 1
fi

# Check Node.js
if ! check_node_version; then
    echo "Node.js is not installed or version check failed"
    exit 1
fi

# Check Maven
if ! check_maven_version; then
    echo "Maven is not installed or version check failed"
    exit 1
fi

# Check other required commands
check_command redis-cli || { echo "Redis CLI is not installed"; exit 1; }
check_command nginx || { echo "Nginx is not installed"; exit 1; }

# Check if install script exists and is executable
if [ -f "install-dependencies.sh" ]; then
    if [ ! -x "install-dependencies.sh" ]; then
        echo "Making install-dependencies.sh executable..."
        chmod +x install-dependencies.sh
    fi
    
    echo "Running install-dependencies.sh..."
    ./install-dependencies.sh
    if [ $? -ne 0 ]; then
        echo "Error: Failed to run install-dependencies.sh"
        exit 1
    fi
else
    echo "Warning: install-dependencies.sh not found"
fi

echo "All dependencies checked successfully!" 