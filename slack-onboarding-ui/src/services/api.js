const API_BASE_URL = 'http://localhost:8080'; // API Layer base URL

// Integration Services
const INTEGRATION_SERVICES = {
  slack: { port: 8081, baseUrl: 'http://localhost:8081' },
  jira: { port: 8082, baseUrl: 'http://localhost:8082' },
  github: { port: 8083, baseUrl: 'http://localhost:8083' },
  google: { port: 8084, baseUrl: 'http://localhost:8084' }
};

// Teams API
export const teamsApi = {
  getTeams: async () => {
    const response = await fetch(`${API_BASE_URL}/api/teams`);
    if (!response.ok) throw new Error('Failed to fetch teams');
    return response.json();
  },

  createTeam: async (teamData) => {
    const response = await fetch(`${API_BASE_URL}/api/teams`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(teamData)
    });
    if (!response.ok) throw new Error('Failed to create team');
    return response.json();
  },

  updateTeam: async (teamId, teamData) => {
    const response = await fetch(`${API_BASE_URL}/api/teams/${teamId}`, {
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
      apiLayer: { url: API_BASE_URL, name: 'API Layer' },
      slack: { url: INTEGRATION_SERVICES.slack.baseUrl, name: 'Slack Integration' },
      jira: { url: INTEGRATION_SERVICES.jira.baseUrl, name: 'Jira Integration' },
      github: { url: INTEGRATION_SERVICES.github.baseUrl, name: 'GitHub Integration' },
      google: { url: INTEGRATION_SERVICES.google.baseUrl, name: 'Google Calendar Integration' }
    };

    const results = await Promise.all(
      Object.entries(services).map(async ([key, service]) => {
        try {
          const response = await fetch(`${service.url}/health`);
          const data = await response.json();
          return {
            service: key,
            name: service.name,
            status: response.ok ? 'healthy' : 'unhealthy',
            details: data,
            lastChecked: new Date().toISOString()
          };
        } catch (error) {
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