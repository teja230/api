# Autonomous Agents API

## Goal

This project provides a backend API for integrating Slack OAuth, managing Slack tokens, and supporting autonomous agent
workflows. It is designed to facilitate secure Slack integration and token management for enterprise applications.

## Project Structure

- `api-layer/`: Main Spring Boot API application (Java)
- `slack-integration/`: Slack integration logic and models
- `slack-onboarding-ui/`: Frontend UI for onboarding (React)

## Prerequisites

- Java 17+
- Maven 3.6+
- Redis (for token storage)
- Node.js (for UI, optional)

## Setup

1. **Clone the repository**
   ```sh
   git clone <your-repo-url>
   cd <repo-root>
   ```
2. **Configure environment variables**
    - Edit `api-layer/src/main/resources/application.yml` and set your Slack credentials and Redis connection:
      ```yaml
      slack:
        oauth:
          client-id: <your-client-id>
          client-secret: <your-client-secret>
          redirect-uri: http://localhost:8080/api/slack/oauth/callback
          token-expiration-seconds: 3600
      spring:
        redis:
          host: localhost
          port: 6379
      ```

## How to Run

### Backend (Spring Boot API)

1. Build and start Redis locally (if not already running):
   ```sh
   redis-server
   ```
2. Build and run the API:
   ```sh
   cd api-layer
   mvn clean spring-boot:run
   ```
   The API will be available at `http://localhost:8080`.

### Frontend (Optional)

1. Install dependencies and run the UI:
   ```sh
   cd slack-onboarding-ui
   npm install
   npm start
   ```
   The UI will be available at `http://localhost:3000`.

## Key Endpoints

- `/api/slack/oauth/url` - Get Slack OAuth URL
- `/api/slack/oauth/callback` - Slack OAuth callback
- `/api/slack/token` - Get stored Slack token
- `/api/slack/token/delete` - Delete stored Slack token
- `/api/slack/redis/health` - Check Redis health

## Notes

- Ensure Redis is running before starting the API.
- Slack credentials must be set in the configuration file.
- For production, secure your credentials and use environment variables or a secrets manager.

