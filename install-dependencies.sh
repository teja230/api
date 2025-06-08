#!/usr/bin/env bash

# Installs required packages for running the Onboarding App.
# This script targets Debian/Ubuntu systems using apt-get. For other
# platforms, install Java 17+, Maven 3.6+, Node.js 18+, Redis and Nginx
# using your system's package manager.

set -e

if ! command -v apt-get >/dev/null; then
  echo "apt-get not found. Please install Java, Maven, Node.js, Redis and Nginx manually." >&2
  exit 1
fi

sudo apt-get update
sudo apt-get install -y openjdk-17-jdk maven redis-server nginx curl

# Install Node.js 18 from NodeSource
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

echo "All dependencies installed."
