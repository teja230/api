# Onboarding App

## Goal

This project provides a backend API for integrating various services (Slack, GitHub, Google Calendar, Jira) and managing
OAuth tokens. It is designed to facilitate secure service integrations and token management for enterprise applications.

## Project Structure

- `integrations/`: Contains all service integrations
    - `github/`: GitHub integration service
    - `google/`: Google Calendar integration service
    - `jira/`: Jira integration service
    - `slack/`: Slack integration service
- `ui-app/`: Frontend UI application (React)

## Prerequisites

- Java 17+
- Maven 3.6+
- Node.js 18+ (for UI)
- Redis (for token storage)
- Nginx (for reverse proxy)

## Setup

1. **Clone the repository**
   ```sh
   git clone <your-repo-url>
   cd <repo-root>
   ```

2. **Install prerequisites**
   Run the helper script to install Java, Maven, Node.js, Redis and Nginx on Debian/Ubuntu systems:
   ```sh
   ./install-dependencies.sh
   ```

3. **Configure environment variables**
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

- GitHub: 8081
- Google Calendar: 8082
- Slack: 8083
- Jira: 8084

1. **Start all services at once**
   ```sh
   cd /path/to/project
   mvn spring-boot:run -pl integrations/slack & mvn spring-boot:run -pl integrations/github & mvn spring-boot:run -pl integrations/jira & mvn spring-boot:run -pl integrations/google
   ```

   Or start them individually:

   ```sh
   # Start Jira Integration
   cd integrations/jira
   mvn spring-boot:run

   # Start GitHub Integration
   cd integrations/github
   mvn spring-boot:run

   # Start Google Calendar Integration
   cd integrations/google
   mvn spring-boot:run

   # Start Slack Integration
   cd integrations/slack
   mvn spring-boot:run
   ```

### Frontend UI

1. Install dependencies and run the UI:
   ```sh
   cd ui-app
   npm install
   npm start
   ```
   The UI will be available at `http://localhost:3000`.

### Nginx Reverse Proxy

1. Start Nginx with the provided configuration:
   ```sh
   sudo nginx -c /path/to/project/nginx.conf
   ```

2. To reload Nginx configuration after changes:
   ```sh
   sudo nginx -s reload
   ```

## Service Health Monitoring

The application includes a health check dashboard that monitors the status of all integration services. The health check
endpoints are:

- API Layer: `http://localhost:8080/health/api`
- Slack: `http://localhost:8080/health/slack`
- GitHub: `http://localhost:8080/health/github`
- Jira: `http://localhost:8080/health/jira`
- Google: `http://localhost:8080/health/google`

Each service exposes a Spring Boot Actuator health endpoint at `/actuator/health` that returns the service status.

### Health Check UI

The health check dashboard is available at `http://localhost:3000/health` after logging in. It provides:

- Real-time status of all services
- Last checked timestamp
- Service details and uptime
- Manual refresh option
- Auto-refresh every 30 seconds

## Key Endpoints

### GitHub Integration (Port 8081)

- `/api/github/oauth/url` - Get GitHub OAuth URL
- `/api/github/oauth/callback` - GitHub OAuth callback
- `/api/github/status` - Check GitHub integration status
- `/actuator/health` - Service health check

### Google Calendar Integration (Port 8082)

- `/api/google/calendar/oauth/url` - Get Google Calendar OAuth URL
- `/api/google/calendar/oauth/callback` - Google Calendar OAuth callback
- `/api/google/calendar/status` - Check Google Calendar integration status
- `/actuator/health` - Service health check

### Slack Integration (Port 8083)
- `/api/slack/oauth/url` - Get Slack OAuth URL
- `/api/slack/oauth/callback` - Slack OAuth callback
- `/api/slack/status` - Check Slack integration status
- `/actuator/health` - Service health check

### Jira Integration (Port 8084)

- `/api/jira/oauth/url` - Get Jira OAuth URL
- `/api/jira/oauth/callback` - Jira OAuth callback
- `/api/jira/status` - Check Jira integration status
- `/actuator/health` - Service health check

## Nginx Reverse Proxy Setup

The `nginx.conf` file in the project root configures Nginx as a reverse proxy for all services. It handles:

- Health check endpoint routing
- API endpoint routing
- CORS configuration
- Request forwarding to appropriate services

### Configuration Details

```nginx
# Health check endpoints
location /health/api {
    proxy_pass http://localhost:8085/actuator/health;
}
location /health/slack {
    proxy_pass http://localhost:8083/actuator/health;
}
location /health/github {
    proxy_pass http://localhost:8081/actuator/health;
}
location /health/jira {
    proxy_pass http://localhost:8084/actuator/health;
}
location /health/google {
    proxy_pass http://localhost:8082/actuator/health;
}

# API endpoints
location /api/jira/ {
    proxy_pass http://localhost:8084/api/jira/;
}
location /api/slack/ {
    proxy_pass http://localhost:8083/api/slack/;
}
location /api/github/ {
    proxy_pass http://localhost:8081/api/github/;
}
location /api/google/ {
    proxy_pass http://localhost:8082/api/google/;
}
```

## Running Tests

Run backend and frontend tests to verify the installation:

```sh
mvn test
npm test --prefix ui-app
```

If Maven fails to resolve dependencies because of restricted network access,
pre-install them or provide an offline repository via the `~/.m2` directory
before running the tests.

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
   - Check the health check dashboard for service status

3. **OAuth Issues**
    - Verify OAuth credentials are correct
    - Check redirect URIs match exactly
    - Ensure callback URLs are accessible

4. **Health Check Issues**
    - Verify all services are running
    - Check Nginx configuration
    - Ensure services are accessible on their respective ports
    - Check service logs for any errors

5. **Offline Builds**
    - If Maven or npm cannot download dependencies, populate a local
      repository in `~/.m2` and install node packages manually before
      running builds or tests.

---
