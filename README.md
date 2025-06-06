# Autonomous Agents API

## Goal

This project provides a backend API for integrating various services (Slack, GitHub, Google Calendar, Jira) and managing
OAuth tokens. It is designed to facilitate secure service integrations and token management for enterprise applications.

## Project Structure

- `integrations/`: Contains all service integrations
    - `github/`: GitHub integration service
    - `google/`: Google Calendar integration service
    - `jira/`: Jira integration service
    - `slack/`: Slack integration service
- `ui/`: Frontend UI application (React)

## Prerequisites

- Java 23+
- Maven 3.6+
- Node.js 18+ (for UI)
- Redis (for token storage)

## Setup

1. **Clone the repository**
   ```sh
   git clone <your-repo-url>
   cd <repo-root>
   ```

2. **Configure environment variables**
   For each integration service, configure the respective credentials in their `application.properties` files:

    - GitHub (`integrations/github/src/main/resources/application.properties`):
      ```properties
      github.oauth.client-id=<your-client-id>
      github.oauth.client-secret=<your-client-secret>
      github.oauth.redirect-uri=http://localhost:8082/api/github/oauth/callback
      ```

    - Google Calendar (`integrations/google/src/main/resources/application.properties`):
      ```properties
      google.oauth.client-id=<your-client-id>
      google.oauth.client-secret=<your-client-secret>
      google.oauth.redirect-uri=http://localhost:8083/api/google/calendar/oauth/callback
      ```

    - Jira (`integrations/jira/src/main/resources/application.properties`):
      ```properties
      jira.oauth.client-id=<your-client-id>
      jira.oauth.client-secret=<your-client-secret>
      jira.oauth.redirect-uri=http://localhost:8080/api/jira/oauth/callback
      ```

    - Slack (`integrations/slack/src/main/resources/application.properties`):
      ```properties
      slack.oauth.client-id=<your-client-id>
      slack.oauth.client-secret=<your-client-secret>
      slack.oauth.redirect-uri=http://localhost:8084/api/slack/oauth/callback
      ```

## How to Run

### Integration Services

Each integration service runs on its own port:

- Jira: 8080
- GitHub: 8082
- Google Calendar: 8083
- Slack: 8084

1. **Start Jira Integration**
   ```sh
   cd integrations/jira
   mvn spring-boot:run
   ```

2. **Start GitHub Integration**
   ```sh
   cd integrations/github
   mvn spring-boot:run
   ```

3. **Start Google Calendar Integration**
   ```sh
   cd integrations/google
   mvn spring-boot:run
   ```

4. **Start Slack Integration**
   ```sh
   cd integrations/slack
   mvn spring-boot:run
   ```

### Frontend UI

1. Install dependencies and run the UI:
   ```sh
   cd ui
   npm install
   npm start
   ```
   The UI will be available at `http://localhost:3000`.

## Key Endpoints

### Jira Integration (Port 8080)

- `/api/jira/oauth/url` - Get Jira OAuth URL
- `/api/jira/oauth/callback` - Jira OAuth callback
- `/api/jira/status` - Check Jira integration status

### GitHub Integration (Port 8082)

- `/api/github/oauth/url` - Get GitHub OAuth URL
- `/api/github/oauth/callback` - GitHub OAuth callback
- `/api/github/status` - Check GitHub integration status

### Google Calendar Integration (Port 8083)

- `/api/google/calendar/oauth/url` - Get Google Calendar OAuth URL
- `/api/google/calendar/oauth/callback` - Google Calendar OAuth callback
- `/api/google/calendar/status` - Check Google Calendar integration status

### Slack Integration (Port 8084)
- `/api/slack/oauth/url` - Get Slack OAuth URL
- `/api/slack/oauth/callback` - Slack OAuth callback
- `/api/slack/status` - Check Slack integration status

## Development Notes

- Each service uses an in-memory H2 database for development
- H2 console is available at `/h2-console` for each service
- Default security passwords are generated for each service
- For production:
    - Secure your credentials using environment variables or a secrets manager
    - Configure proper database connections
    - Set up proper security configurations
    - Use HTTPS for all endpoints

## Troubleshooting

1. **Port Already in Use**
   If you see "Port X is already in use" error:
   ```sh
   # Find the process using the port
   lsof -i :<port-number>
   # Kill the process
   kill <process-id>
   ```

2. **Service Not Responding**
    - Check if the service is running
    - Verify the port is not blocked
    - Check service logs for errors
    - Ensure all required environment variables are set

3. **OAuth Issues**
    - Verify OAuth credentials are correct
    - Check redirect URIs match exactly
    - Ensure callback URLs are accessible

