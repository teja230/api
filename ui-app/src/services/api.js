export const API_BASE_URL = 'http://localhost:8085/api';

// Integration Services
const INTEGRATION_SERVICES = {
  slack: { port: 8083, baseUrl: 'http://localhost:8083' },
  jira: { port: 8084, baseUrl: 'http://localhost:8084' },
  github: { port: 8081, baseUrl: 'http://localhost:8081' },
  google: { port: 8082, baseUrl: 'http://localhost:8082' }
};

// Teams API
export const teamsApi = {
  getTeams: async () => {
    const response = await fetch(`${API_BASE_URL}/teams`);
    if (!response.ok) throw new Error('Failed to fetch teams');
    return response.json();
  },

  createTeam: async (teamData) => {
    const response = await fetch(`${API_BASE_URL}/teams`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(teamData)
    });
    if (!response.ok) throw new Error('Failed to create team');
    return response.json();
  },

  updateTeam: async (teamId, teamData) => {
    const response = await fetch(`${API_BASE_URL}/teams/${teamId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(teamData)
    });
    if (!response.ok) throw new Error('Failed to update team');
    return response.json();
  }
};

// Integration APIs
export const integrationApi = {
  // Slack Integration
  slack: {
    connect: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.slack.baseUrl}/api/slack/connect`);
      if (!response.ok) throw new Error('Failed to connect Slack');
      return response.json();
    },
    disconnect: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.slack.baseUrl}/api/slack/disconnect`, {
        method: 'POST'
      });
      if (!response.ok) throw new Error('Failed to disconnect Slack');
      return response.json();
    },
    getStatus: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.slack.baseUrl}/api/slack/status`);
      if (!response.ok) throw new Error('Failed to get Slack status');
      return response.json();
    }
  },

  // Jira Integration
  jira: {
    connect: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.jira.baseUrl}/api/jira/connect`);
      if (!response.ok) throw new Error('Failed to connect Jira');
      return response.json();
    },
    disconnect: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.jira.baseUrl}/api/jira/disconnect`, {
        method: 'POST'
      });
      if (!response.ok) throw new Error('Failed to disconnect Jira');
      return response.json();
    },
    getStatus: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.jira.baseUrl}/api/jira/status`);
      if (!response.ok) throw new Error('Failed to get Jira status');
      return response.json();
    }
  },

  // GitHub Integration
  github: {
    connect: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.github.baseUrl}/api/github/connect`);
      if (!response.ok) throw new Error('Failed to connect GitHub');
      return response.json();
    },
    disconnect: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.github.baseUrl}/api/github/disconnect`, {
        method: 'POST'
      });
      if (!response.ok) throw new Error('Failed to disconnect GitHub');
      return response.json();
    },
    getStatus: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.github.baseUrl}/api/github/status`);
      if (!response.ok) throw new Error('Failed to get GitHub status');
      return response.json();
    }
  },

  // Google Calendar Integration
  google: {
    connect: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.google.baseUrl}/api/google/connect`);
      if (!response.ok) throw new Error('Failed to connect Google Calendar');
      return response.json();
    },
    disconnect: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.google.baseUrl}/api/google/disconnect`, {
        method: 'POST'
      });
      if (!response.ok) throw new Error('Failed to disconnect Google Calendar');
      return response.json();
    },
    getStatus: async () => {
      const response = await fetch(`${INTEGRATION_SERVICES.google.baseUrl}/api/google/status`);
      if (!response.ok) throw new Error('Failed to get Google Calendar status');
      return response.json();
    }
  }
};

// Health Check API
export const healthApi = {
  checkAllServices: async () => {
    const services = {
      apiLayer: { url: 'http://localhost:8085/actuator/health', name: 'API Layer' },
      slack: { url: 'http://localhost:8083/actuator/health', name: 'Slack Integration' },
      jira: { url: 'http://localhost:8084/api/jira/actuator/health', name: 'Jira Integration' },
      github: { url: 'http://localhost:8081/api/github/actuator/health', name: 'GitHub Integration' },
      google: { url: 'http://localhost:8082/actuator/health', name: 'Google Calendar Integration' }
    };

    const results = await Promise.all(
      Object.entries(services).map(async ([key, service]) => {
        try {
          const response = await fetch(service.url, {
            headers: {
              'Accept': 'application/vnd.spring-boot.actuator.v3+json, application/json'
            }
          });
          
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
          
          const data = await response.json();
          const status = data.status === 'UP' ? 'healthy' : 'unhealthy';
          
          return {
            service: key,
            name: service.name,
            status: status,
            details: data,
            lastChecked: new Date().toISOString()
          };
        } catch (error) {
          console.error(`Health check failed for ${service.name}:`, error);
          return {
            service: key,
            name: service.name,
            status: 'unhealthy',
            error: error.message,
            lastChecked: new Date().toISOString()
          };
        }
      })
    );

    return results;
  }
};

export const getSystemHealth = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/system/health`, {
      credentials: 'include',
      headers: {
        'Accept': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch health data');
    }
    
    return response.json();
  } catch (error) {
    console.error('Failed to fetch system health:', error);
    throw error;
  }
}; 