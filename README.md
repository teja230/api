# Onboarding App

## Goal

This project provides a backend API for integrating various services (Slack, GitHub, Google Calendar, Jira) and managing
OAuth tokens. It is designed to facilitate secure service integrations and token management for enterprise applications
to help with Onboarding new employees.

![Onboarding App](img.png)

## Project Structure

- `integrations/`: Contains all service integrations
    - `github/`: GitHub integration service
    - `google/`: Google Calendar integration service
    - `jira/`: Jira integration service
    - `slack/`: Slack integration service
- `api-layer/`: API gateway layer (aggregates integration services)
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
      github.oauth.redirect-uri=http://localhost:8081/api/github/oauth/callback
      ```

    - Google Calendar (`integrations/google/src/main/resources/application.properties`):
      ```properties
      google.oauth.client-id=<your-client-id>
      google.oauth.client-secret=<your-client-secret>
      google.oauth.redirect-uri=http://localhost:8082/api/google/calendar/oauth/callback
      ```

    - Jira (`integrations/jira/src/main/resources/application.properties`):
      ```properties
      jira.oauth.client-id=<your-client-id>
      jira.oauth.client-secret=<your-client-secret>
      jira.oauth.redirect-uri=http://localhost:8084/api/jira/oauth/callback
      ```

    - Slack (`integrations/slack/src/main/resources/application.properties`):
      ```properties
      slack.oauth.client-id=<your-client-id>
      slack.oauth.client-secret=<your-client-secret>
      slack.oauth.redirect-uri=http://localhost:8083/api/slack/oauth/callback
      ```

## How to Run

### Start All Services (Preferred: Microservices)

Use the provided script to start all backend services in microservices mode:

```sh
./start-services.sh
```

This will start:

- GitHub Integration (port 8081)
- Google Calendar Integration (port 8082)
- Slack Integration (port 8083)
- Jira Integration (port 8084)
- API Layer (port 8085)

You can also start any service individually by running:

```sh
cd integrations/<service>
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

**All API and UI access should be done via Nginx on port 8080.**

## Service Health Monitoring

The application includes a health check dashboard that monitors the status of all integration services. The main health
check endpoint is:

- System Health: `http://localhost:8080/api/system/health`

Each service also exposes a Spring Boot Actuator health endpoint at `/actuator/health` on its respective port:

- API Layer: `http://localhost:8085/actuator/health`
- Slack: `http://localhost:8083/actuator/health`
- GitHub: `http://localhost:8081/actuator/health`
- Jira: `http://localhost:8084/actuator/health`
- Google: `http://localhost:8082/actuator/health`

### Health Check UI

The health check dashboard is available at `http://localhost:3000/health` after logging in. It provides:

- Real-time status of all services
- Last checked timestamp
- Service details and uptime
- Manual refresh option
- Auto-refresh every 30 seconds

## Key Endpoints (All via Nginx on port 8080)

### GitHub Integration
- `/api/github/oauth/url` - Get GitHub OAuth URL
- `/api/github/oauth/callback` - GitHub OAuth callback
- `/api/github/status` - Check GitHub integration status
- `/actuator/health` - Service health check

### Google Calendar Integration
- `/api/google/calendar/oauth/url` - Get Google Calendar OAuth URL
- `/api/google/calendar/oauth/callback` - Google Calendar OAuth callback
- `/api/google/calendar/status` - Check Google Calendar integration status
- `/actuator/health` - Service health check

### Slack Integration
- `/api/slack/oauth/url` - Get Slack OAuth URL
- `/api/slack/oauth/callback` - Slack OAuth callback
- `/api/slack/status` - Check Slack integration status
- `/actuator/health` - Service health check

### Jira Integration
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
location /api/system/health {
    proxy_pass http://localhost:8085/api/system/health;
}
location /actuator/health {
    proxy_pass http://localhost:8085/actuator/health;
}
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
