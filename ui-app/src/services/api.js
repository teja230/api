export const API_BASE_URL = 'http://localhost:8080/api';

// Integration Services
const INTEGRATION_SERVICES = {
  slack: { path: '/slack' },
  jira: { path: '/jira' },
  github: { path: '/github' },
  google: { path: '/google' }
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
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.slack.path}/connect`);
      if (!response.ok) throw new Error('Failed to connect Slack');
      return response.json();
    },
    disconnect: async () => {
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.slack.path}/disconnect`, {
        method: 'POST'
      });
      if (!response.ok) throw new Error('Failed to disconnect Slack');
      return response.json();
    },
    getStatus: async () => {
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.slack.path}/status`);
      if (!response.ok) throw new Error('Failed to get Slack status');
      return response.json();
    }
  },

  // Jira Integration
  jira: {
    connect: async () => {
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.jira.path}/connect`);
      if (!response.ok) throw new Error('Failed to connect Jira');
      return response.json();
    },
    disconnect: async () => {
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.jira.path}/disconnect`, {
        method: 'POST'
      });
      if (!response.ok) throw new Error('Failed to disconnect Jira');
      return response.json();
    },
    getStatus: async () => {
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.jira.path}/status`);
      if (!response.ok) throw new Error('Failed to get Jira status');
      return response.json();
    }
  },

  // GitHub Integration
  github: {
    connect: async () => {
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.github.path}/connect`);
      if (!response.ok) throw new Error('Failed to connect GitHub');
      return response.json();
    },
    disconnect: async () => {
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.github.path}/disconnect`, {
        method: 'POST'
      });
      if (!response.ok) throw new Error('Failed to disconnect GitHub');
      return response.json();
    },
    getStatus: async () => {
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.github.path}/status`);
      if (!response.ok) throw new Error('Failed to get GitHub status');
      return response.json();
    }
  },

  // Google Calendar Integration
  google: {
    connect: async () => {
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.google.path}/connect`);
      if (!response.ok) throw new Error('Failed to connect Google Calendar');
      return response.json();
    },
    disconnect: async () => {
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.google.path}/disconnect`, {
        method: 'POST'
      });
      if (!response.ok) throw new Error('Failed to disconnect Google Calendar');
      return response.json();
    },
    getStatus: async () => {
      const response = await fetch(`${API_BASE_URL}${INTEGRATION_SERVICES.google.path}/status`);
      if (!response.ok) throw new Error('Failed to get Google Calendar status');
      return response.json();
    }
  }
};

// Health Check API
export const healthApi = {
  checkAllServices: async () => {
    const services = {
      apiLayer: { path: '/health', name: 'API Layer' },
      slack: { path: '/slack/health', name: 'Slack Integration' },
      jira: { path: '/jira/health', name: 'Jira Integration' },
      github: { path: '/github/health', name: 'GitHub Integration' },
      google: { path: '/google/health', name: 'Google Calendar Integration' }
    };

    const results = await Promise.all(
      Object.entries(services).map(async ([key, service]) => {
        try {
          const response = await fetch(`${API_BASE_URL}${service.path}`, {
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
    // First check the API layer health
    const apiHealthResponse = await fetch(`${API_BASE_URL}/health`, {
      credentials: 'include',
      headers: {
        'Accept': 'application/vnd.spring-boot.actuator.v3+json, application/json'
      }
    });

    if (!apiHealthResponse.ok) {
      throw new Error('API service is not responding');
    }

    const apiHealth = await apiHealthResponse.json();
    
    // Then check all integration services
    const services = {
      'api-layer': {
        name: 'API Service',
        path: '/health',
        status: apiHealth.status === 'UP' ? 'UP' : 'DOWN',
        lastChecked: new Date().toISOString(),
        details: apiHealth
      },
      'slack': {
        name: 'Slack Integration',
        path: '/slack/health'
      },
      'jira': {
        name: 'Jira Integration',
        path: '/jira/health'
      },
      'github': {
        name: 'GitHub Integration',
        path: '/github/health'
      },
      'google-calendar': {
        name: 'Google Calendar Integration',
        path: '/google/health'
      }
    };

    // Check each service in parallel
    const serviceChecks = Object.entries(services).map(async ([key, service]) => {
      if (key === 'api-layer') return [key, service]; // Skip API layer as we already checked it

      try {
        const response = await fetch(`${API_BASE_URL}${service.path}`, {
          headers: {
            'Accept': 'application/vnd.spring-boot.actuator.v3+json, application/json'
          }
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        return [key, {
          ...service,
          status: data.status === 'UP' ? 'UP' : 'DOWN',
          lastChecked: new Date().toISOString(),
          details: data
        }];
      } catch (error) {
        console.error(`Health check failed for ${service.name}:`, error);
        return [key, {
          ...service,
          status: 'DOWN',
          lastChecked: new Date().toISOString(),
          error: error.message
        }];
      }
    });

    const results = await Promise.all(serviceChecks);
    return {
      services: Object.fromEntries(results),
      timestamp: new Date().toISOString()
    };
  } catch (error) {
    console.error('Failed to fetch system health:', error);
    throw error;
  }
}; 