import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes, useLocation, Link, Navigate, useNavigate, Link as RouterLink } from 'react-router-dom';
import './modern.css';
import {
  AppBar, Toolbar, Typography, Button, Card, Box, CircularProgress, Alert, List, ListItem, ListItemText, Container, Stack,
  IconButton, useTheme, ThemeProvider, createTheme, CssBaseline, Tooltip, Chip, Fade, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, Select, MenuItem, FormControl, InputLabel, Grid, Avatar, Divider, CardContent
} from '@mui/material';
import {
  CheckCircle, Error as ErrorIcon, Home as HomeIcon, Group as GroupIcon, InsertComment as SlackIcon,
  Assignment as JiraIcon, Brightness4, Brightness7, GitHub as GitHubIcon, CalendarMonth as CalendarIcon,
  Security as SecurityIcon, Cloud as CloudIcon, Email as EmailIcon, Storage as StorageIcon,
  Business as BusinessIcon, Settings as SettingsIcon, Refresh as RefreshIcon
} from '@mui/icons-material';
import SlackIntegration from './integrations/SlackIntegration';
import JiraIntegration from './integrations/JiraIntegration';
import GitHubIntegration from './integrations/GitHubIntegration';
import GoogleCalendarIntegration from './integrations/GoogleCalendarIntegration';
import { teamsApi, integrationApi, healthApi } from './services/api';

function useQuery() {
  return new URLSearchParams(useLocation().search);
}

function EnterpriseConfigDialog({ open, onClose, onSave, config }) {
  const [formData, setFormData] = useState(config || {
    enterpriseId: '',
    enterpriseName: '',
    jiraBaseUrl: '',
    slackWorkspaceId: '',
    githubOrgId: '',
    googleWorkspaceId: '',
    onboardingTemplateId: '',
    logoUrl: '',
    primaryColor: '#4A154B',
    secondaryColor: '#2eb67d'
  });

  const handleChange = (field) => (event) => {
    setFormData({ ...formData, [field]: event.target.value });
  };

  const handleSubmit = () => {
    onSave(formData);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Enterprise Configuration</DialogTitle>
      <DialogContent>
        <Grid container spacing={3} sx={{ mt: 1 }}>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Enterprise ID"
              value={formData.enterpriseId}
              onChange={handleChange('enterpriseId')}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Enterprise Name"
              value={formData.enterpriseName}
              onChange={handleChange('enterpriseName')}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Logo URL"
              value={formData.logoUrl}
              onChange={handleChange('logoUrl')}
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Primary Color"
              value={formData.primaryColor}
              onChange={handleChange('primaryColor')}
              margin="normal"
              type="color"
            />
            <TextField
              fullWidth
              label="Secondary Color"
              value={formData.secondaryColor}
              onChange={handleChange('secondaryColor')}
              margin="normal"
              type="color"
            />
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Integration Settings</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="JIRA Base URL"
              value={formData.jiraBaseUrl}
              onChange={handleChange('jiraBaseUrl')}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Slack Workspace ID"
              value={formData.slackWorkspaceId}
              onChange={handleChange('slackWorkspaceId')}
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="GitHub Organization ID"
              value={formData.githubOrgId}
              onChange={handleChange('githubOrgId')}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Google Workspace ID"
              value={formData.googleWorkspaceId}
              onChange={handleChange('googleWorkspaceId')}
              margin="normal"
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleSubmit} variant="contained" color="primary">
          Save Configuration
        </Button>
      </DialogActions>
    </Dialog>
  );
}

function MenuBar({ enterpriseConfig, onConfigClick, onLogout }) {
  const theme = useTheme();
  const [darkMode, setDarkMode] = useState(false);

  const toggleDarkMode = () => {
    setDarkMode(!darkMode);
  };

  return (
    <AppBar position="static" color="default" elevation={2} sx={{ mb: 4 }}>
      <Toolbar>
        {enterpriseConfig?.logoUrl ? (
          <Avatar src={enterpriseConfig.logoUrl} sx={{ mr: 1 }} />
        ) : (
          <BusinessIcon sx={{ color: theme.palette.primary.main, mr: 1 }} />
        )}
        <Typography variant="h6" color="primary" sx={{ flexGrow: 1, fontWeight: 700 }}>
          {enterpriseConfig?.enterpriseName || 'Onboarding Buddy'}
        </Typography>
        <Button component={Link} to="/" color="primary" startIcon={<HomeIcon />} sx={{ fontWeight: 600 }}>
          Home
        </Button>
        <Button component={Link} to="/teams" color="primary" startIcon={<GroupIcon />} sx={{ fontWeight: 600 }}>
          Teams
        </Button>
        <Button component={Link} to="/health" color="primary" startIcon={<CalendarIcon />} sx={{ fontWeight: 600 }}>
          Health Check
        </Button>
        <IconButton onClick={onConfigClick} color="inherit" sx={{ ml: 1 }}>
          <SettingsIcon />
        </IconButton>
        <IconButton onClick={toggleDarkMode} color="inherit" sx={{ ml: 1 }}>
          {darkMode ? <Brightness7 /> : <Brightness4 />}
        </IconButton>
        <Button onClick={onLogout} color="inherit" sx={{ ml: 1 }}>
          Logout
        </Button>
      </Toolbar>
    </AppBar>
  );
}

function Status() {
  const [status, setStatus] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch('/api/slack/status')
      .then(res => {
        if (!res.ok) throw new Error('API error');
        return res.json();
      })
      .then(data => setStatus(data.connected))
      .catch(err => setError('Failed to fetch status'));
  }, []);

  if (error) return <Alert severity="error">{error}</Alert>;

  return (
    <Box mb={2}>
      <Typography variant="h6" component="div">
        Slack Integration Status:
        {status === null ? (
          <CircularProgress size={18} sx={{ ml: 1 }} />
        ) : status ? (
          <span style={{ color: '#2eb67d', marginLeft: 8 }}><CheckCircle fontSize="small" /> Connected</span>
        ) : (
          <span style={{ color: '#e01e5a', marginLeft: 8 }}><ErrorIcon fontSize="small" /> Not Connected</span>
        )}
      </Typography>
    </Box>
  );
}

function SuccessPage() {
  const query = useQuery();
  const status = query.get('status');
  const team = query.get('team');
  const msg = query.get('msg');

  return (
    <Container maxWidth="sm" sx={{ mt: 8 }}>
      {status === 'ok' ? (
        <Alert icon={<CheckCircle fontSize="inherit" />} severity="success" sx={{ mb: 2 }}>
          <Typography variant="h5">Slack Integration Successful!</Typography>
          {team && <Typography>Connected to: <b>{team}</b></Typography>}
        </Alert>
      ) : (
        <Alert icon={<ErrorIcon fontSize="inherit" />} severity="error" sx={{ mb: 2 }}>
          <Typography variant="h5">Slack Integration Failed</Typography>
          <Typography>{msg}</Typography>
        </Alert>
      )}
    </Container>
  );
}

function TeamsList({ enterpriseConfig }) {
  const [teams, setTeams] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch('/api/slack/teams')
      .then(res => {
        if (!res.ok) throw new Error('API error');
        return res.json();
      })
      .then(data => setTeams(data))
      .catch(err => setError('Failed to fetch teams'));
  }, []);

  if (error) return <Alert severity="error">{error}</Alert>;

  return (
    <Box mt={3} width="100%">
      <Typography variant="subtitle1" color="primary" fontWeight={600} mb={1}>
        Connected Slack Workspaces
      </Typography>
      <List>
        {teams.map(team => (
          <ListItem key={team.teamId} sx={{ bgcolor: '#f6f6fa', borderRadius: 1, mb: 1 }}>
            <ListItemText
              primary={<span style={{ fontWeight: 500 }}>{team.teamName}</span>}
              secondary={<span style={{ color: '#aaa' }}>({team.teamId})</span>}
            />
          </ListItem>
        ))}
      </List>
    </Box>
  );
}

function IntegrationsList({ enterpriseConfig }) {
  return (
    <Box>
      <SlackIntegration enterpriseConfig={enterpriseConfig} />
      <JiraIntegration enterpriseConfig={enterpriseConfig} />
      <GitHubIntegration enterpriseConfig={enterpriseConfig} />
      <GoogleCalendarIntegration enterpriseConfig={enterpriseConfig} />
    </Box>
  );
}

function LoginPage({ onLogin }) {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async () => {
    setLoading(true);
    try {
      // Simulate login delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      onLogin();
      navigate('/integrations');
    } catch (error) {
      console.error('Login failed:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 8 }}>
      <Card sx={{ p: 4, textAlign: 'center' }}>
        <Typography variant="h4" gutterBottom>
          Welcome to Onboarding App
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          Please sign in to continue
        </Typography>
        <Button
          variant="contained"
          color="primary"
          size="large"
          onClick={handleLogin}
          disabled={loading}
          startIcon={loading ? <CircularProgress size={20} /> : <SecurityIcon />}
        >
          {loading ? 'Signing in...' : 'Sign in with SSO'}
        </Button>
      </Card>
    </Container>
  );
}

function IntegrationsDashboard({ integrations, onConnect, onDisconnect }) {
  const [error, setError] = useState(null);

  const handleConnect = async (integration) => {
    try {
      setError(null);
      await integrationApi[integration].connect();
      onConnect(integration);
    } catch (err) {
      setError(`Failed to connect ${integration}: ${err.message}`);
    }
  };

  const handleDisconnect = async (integration) => {
    try {
      setError(null);
      await integrationApi[integration].disconnect();
      onDisconnect(integration);
    } catch (err) {
      setError(`Failed to disconnect ${integration}: ${err.message}`);
    }
  };

  const getIntegrationIcon = (integration) => {
    switch (integration) {
      case 'slack': return <SlackIcon />;
      case 'jira': return <JiraIcon />;
      case 'github': return <GitHubIcon />;
      case 'google': return <CalendarIcon />;
      default: return null;
    }
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        Integrations Dashboard
      </Typography>
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      <Grid container spacing={3}>
        {Object.entries(integrations).map(([key, value]) => (
          <Grid item xs={12} md={6} key={key}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" mb={2}>
                  {getIntegrationIcon(key)}
                  <Typography variant="h6" sx={{ ml: 1 }}>
                    {key.charAt(0).toUpperCase() + key.slice(1)}
                  </Typography>
                </Box>
                <Box display="flex" alignItems="center" mb={2}>
                  <Typography variant="body2" sx={{ mr: 1 }}>
                    Status:
                  </Typography>
                  {value.loading ? (
                    <CircularProgress size={16} />
                  ) : value.connected ? (
                    <Chip
                      size="small"
                      label="Connected"
                      color="success"
                      icon={<CheckCircle fontSize="small" />}
                    />
                  ) : (
                    <Chip
                      size="small"
                      label="Not Connected"
                      color="default"
                      icon={<ErrorIcon fontSize="small" />}
                    />
                  )}
                </Box>
                {value.connected ? (
                  <Button
                    variant="outlined"
                    color="error"
                    onClick={() => handleDisconnect(key)}
                    disabled={value.loading}
                  >
                    {value.loading ? 'Disconnecting...' : 'Disconnect'}
                  </Button>
                ) : (
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => handleConnect(key)}
                    disabled={value.loading}
                  >
                    {value.loading ? 'Connecting...' : 'Connect'}
                  </Button>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
}

// Mock data for teams
const MOCK_TEAMS = [
  {
    id: 1,
    name: 'Engineering Team',
    memberCount: 12,
    createdAt: '2024-03-15',
    status: 'active',
    integrations: ['slack', 'jira', 'github']
  },
  {
    id: 2,
    name: 'Product Team',
    memberCount: 8,
    createdAt: '2024-03-10',
    status: 'active',
    integrations: ['slack', 'jira']
  },
  {
    id: 3,
    name: 'Design Team',
    memberCount: 6,
    createdAt: '2024-03-05',
    status: 'active',
    integrations: ['slack', 'google']
  },
  {
    id: 4,
    name: 'Marketing Team',
    memberCount: 5,
    createdAt: '2024-03-01',
    status: 'active',
    integrations: ['slack']
  }
];

// Mock data for service health
const MOCK_SERVICE_HEALTH = {
  apiLayer: {
    service: 'apiLayer',
    name: 'API Layer',
    status: 'healthy',
    details: { version: '1.0.0', uptime: '2h 30m' },
    lastChecked: new Date().toISOString()
  },
  slack: {
    service: 'slack',
    name: 'Slack Integration',
    status: 'healthy',
    details: { connected: true, workspace: 'Demo Workspace' },
    lastChecked: new Date().toISOString()
  },
  jira: {
    service: 'jira',
    name: 'Jira Integration',
    status: 'healthy',
    details: { connected: true, instance: 'demo.atlassian.net' },
    lastChecked: new Date().toISOString()
  },
  github: {
    service: 'github',
    name: 'GitHub Integration',
    status: 'healthy',
    details: { connected: true, organization: 'demo-org' },
    lastChecked: new Date().toISOString()
  },
  google: {
    service: 'google',
    name: 'Google Calendar Integration',
    status: 'healthy',
    details: { connected: true, calendar: 'primary' },
    lastChecked: new Date().toISOString()
  }
};

const HealthCheckPage = () => {
  const [services, setServices] = useState({
    api: { status: 'unknown', error: null },
    slack: { status: 'unknown', error: null },
    jira: { status: 'unknown', error: null },
    github: { status: 'unknown', error: null },
    google: { status: 'unknown', error: null }
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastChecked, setLastChecked] = useState(null);

  const checkHealth = async () => {
    setLoading(true);
    setError(null);
    try {
      // Call health endpoints via nginx (port 8080)
      const serviceChecks = await Promise.allSettled([
        fetch('/health/api').then(res => res.json()),
        fetch('/health/slack').then(res => res.json()),
        fetch('/health/jira').then(res => res.json()),
        fetch('/health/github').then(res => res.json()),
        fetch('/health/google').then(res => res.json())
      ]);

      const newServices = {
        api: { status: 'unknown', error: null },
        slack: { status: 'unknown', error: null },
        jira: { status: 'unknown', error: null },
        github: { status: 'unknown', error: null },
        google: { status: 'unknown', error: null }
      };

      serviceChecks.forEach((result, index) => {
        const serviceName = ['api', 'slack', 'jira', 'github', 'google'][index];
        if (result.status === 'fulfilled') {
          newServices[serviceName] = {
            status: result.value.status === 'UP' ? 'healthy' : 'unhealthy',
            error: null,
            details: {
              service: serviceName,
              timestamp: new Date().toISOString()
            }
          };
        } else {
          newServices[serviceName] = {
            status: 'unhealthy',
            error: 'Service unavailable',
            details: null
          };
        }
      });

      setServices(newServices);
      setLastChecked(new Date());
    } catch (err) {
      setError('Failed to check service health');
      console.error('Health check error:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkHealth();
    const interval = setInterval(checkHealth, 30000); // Check every 30 seconds
    return () => clearInterval(interval);
  }, []);

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Service Health Status
        </Typography>
        <Box>
          <Button
            variant="contained"
            onClick={checkHealth}
            disabled={loading}
            startIcon={<RefreshIcon />}
          >
            Refresh
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {lastChecked && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Last checked: {lastChecked.toLocaleString()}
        </Typography>
      )}

      <Grid container spacing={3}>
        {Object.entries(services).map(([service, data]) => (
          <Grid item xs={12} sm={6} md={4} key={service}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                    {service.charAt(0).toUpperCase() + service.slice(1)} Service
                  </Typography>
                  <Chip
                    label={data.status}
                    color={data.status === 'healthy' ? 'success' : 'error'}
                    size="small"
                  />
                </Box>
                {data.error && (
                  <Typography color="error" variant="body2">
                    {data.error}
                  </Typography>
                )}
                {data.details && (
                  <Box sx={{ mt: 1 }}>
                    <Typography variant="body2" color="text.secondary">
                      Service: {data.details.service}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Last Updated: {new Date(data.details.timestamp).toLocaleString()}
                    </Typography>
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
};

function Home({ enterpriseConfig, onSSOLogin }) {
  if (!enterpriseConfig) {
    return <LoginPage onLogin={onSSOLogin} />;
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#f4f1f8', display: 'flex', alignItems: 'center', justifyContent: 'center', py: 6 }}>
      <Container maxWidth="md">
        <Card sx={{ p: { xs: 3, sm: 6 }, borderRadius: 4, boxShadow: 8, bgcolor: '#fff', minWidth: 360 }}>
          <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, alignItems: 'center', mb: 4 }}>
            <Box sx={{ flex: 1, display: 'flex', justifyContent: 'center', mb: { xs: 2, md: 0 } }}>
              <img
                src="https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=facearea&w=256&q=80&facepad=2"
                alt="Professional onboarding buddy"
                style={{ borderRadius: 16, width: 120, height: 120, objectFit: 'cover', boxShadow: '0 4px 24px rgba(0,0,0,0.08)' }}
              />
            </Box>
            <Box sx={{ flex: 3 }}>
              <Typography variant="h4" fontWeight={700} color="primary" align="center" gutterBottom>
                Onboarding Integrations
              </Typography>
              <Typography variant="subtitle1" color="text.secondary" align="center" sx={{ mb: 2 }}>
                Connect your favorite tools to automate and enhance your onboarding process.
              </Typography>
            </Box>
          </Box>
          <IntegrationsList enterpriseConfig={enterpriseConfig} />
        </Card>
      </Container>
    </Box>
  );
}

function TeamsPage({ teams, integrations }) {
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [localTeams, setLocalTeams] = useState([]);

  useEffect(() => {
    const loadTeams = async () => {
      try {
        setLoading(true);
        setError(null);
        // Use mock data for now
        setLocalTeams(MOCK_TEAMS);
      } catch (err) {
        setError('Failed to load teams. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    loadTeams();
  }, []);

  const handleViewTeam = async (teamId) => {
    try {
      setError(null);
      setLoading(true);
      // TODO: Implement team view logic
      console.log('View team:', teamId);
    } catch (err) {
      setError(`Failed to load team: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleEditTeam = async (teamId) => {
    try {
      setError(null);
      setLoading(true);
      // TODO: Implement team edit logic
      console.log('Edit team:', teamId);
    } catch (err) {
      setError(`Failed to load team: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, textAlign: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        Teams
      </Typography>
      <Grid container spacing={3}>
        {localTeams.map((team) => (
          <Grid item xs={12} md={6} key={team.id}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {team.name}
                </Typography>
                <Box display="flex" alignItems="center" mb={2}>
                  <Typography variant="body2" sx={{ mr: 1 }}>
                    Members:
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {team.memberCount}
                  </Typography>
                </Box>
                <Box display="flex" alignItems="center" mb={2}>
                  <Typography variant="body2" sx={{ mr: 1 }}>
                    Created:
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {new Date(team.createdAt).toLocaleDateString()}
                  </Typography>
                </Box>
                <Box display="flex" alignItems="center" mb={2}>
                  <Typography variant="body2" sx={{ mr: 1 }}>
                    Status:
                  </Typography>
                  <Chip
                    size="small"
                    label={team.status}
                    color={team.status === 'active' ? 'success' : 'default'}
                  />
                </Box>
                <Box display="flex" alignItems="center" mb={2}>
                  <Typography variant="body2" sx={{ mr: 1 }}>
                    Integrations:
                  </Typography>
                  <Box>
                    {team.integrations.map((integration) => (
                      <Chip
                        key={integration}
                        size="small"
                        label={integration}
                        color={integrations[integration]?.connected ? 'primary' : 'default'}
                        sx={{ mr: 1, mb: 1 }}
                      />
                    ))}
                  </Box>
                </Box>
                <Box display="flex" gap={1}>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() => handleViewTeam(team.id)}
                    disabled={loading}
                  >
                    View Team
                  </Button>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() => handleEditTeam(team.id)}
                    disabled={loading}
                  >
                    Edit Team
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
}

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [enterpriseConfig, setEnterpriseConfig] = useState({
    enterpriseName: 'Onboarding App',
    logoUrl: 'https://via.placeholder.com/150',
    primaryColor: '#1976d2',
    secondaryColor: '#dc004e'
  });
  const [configDialogOpen, setConfigDialogOpen] = useState(false);
  const [teams, setTeams] = useState([]);
  const [integrations, setIntegrations] = useState(() => {
    const savedIntegrations = localStorage.getItem('integrations');
    return savedIntegrations ? JSON.parse(savedIntegrations) : {
      slack: { connected: false, loading: false },
      jira: { connected: false, loading: false },
      github: { connected: false, loading: false },
      google: { connected: false, loading: false }
    };
  });
  const [error, setError] = useState(null);

  // Load teams data
  useEffect(() => {
    const loadTeams = async () => {
      try {
        const data = await teamsApi.getTeams();
        setTeams(data);
      } catch (err) {
        setError('Failed to load teams. Please try again later.');
      }
    };

    if (isAuthenticated) {
      loadTeams();
    }
  }, [isAuthenticated]);

  // Load integration statuses
  useEffect(() => {
    const loadIntegrationStatuses = async () => {
      try {
        const statuses = await Promise.all(
          Object.keys(integrations).map(async (integration) => {
            const status = await integrationApi[integration].getStatus();
            return [integration, status];
          })
        );

        const newIntegrations = statuses.reduce((acc, [integration, status]) => ({
          ...acc,
          [integration]: { connected: status.connected, loading: false }
        }), {});

        setIntegrations(newIntegrations);
      } catch (err) {
        setError('Failed to load integration statuses. Please try again later.');
      }
    };

    if (isAuthenticated) {
      loadIntegrationStatuses();
    }
  }, [isAuthenticated]);

  // Save integration statuses to localStorage
  useEffect(() => {
    localStorage.setItem('integrations', JSON.stringify(integrations));
  }, [integrations]);

  const handleConnect = async (integration) => {
    setIntegrations(prev => ({
      ...prev,
      [integration]: { ...prev[integration], loading: true }
    }));

    try {
      await integrationApi[integration].connect();
      setIntegrations(prev => ({
        ...prev,
        [integration]: { connected: true, loading: false }
      }));
    } catch (err) {
      setError(`Failed to connect ${integration}: ${err.message}`);
      setIntegrations(prev => ({
        ...prev,
        [integration]: { ...prev[integration], loading: false }
      }));
    }
  };

  const handleDisconnect = async (integration) => {
    setIntegrations(prev => ({
      ...prev,
      [integration]: { ...prev[integration], loading: true }
    }));

    try {
      await integrationApi[integration].disconnect();
      setIntegrations(prev => ({
        ...prev,
        [integration]: { connected: false, loading: false }
      }));
    } catch (err) {
      setError(`Failed to disconnect ${integration}: ${err.message}`);
      setIntegrations(prev => ({
        ...prev,
        [integration]: { ...prev[integration], loading: false }
      }));
    }
  };

  const handleLogin = () => {
    setIsAuthenticated(true);
    // Mock enterprise config
    setEnterpriseConfig({
      enterpriseId: 'mock-enterprise-123',
      enterpriseName: 'Onboarding App',
      logoUrl: 'https://via.placeholder.com/150',
      primaryColor: '#1976d2',
      secondaryColor: '#dc004e'
    });
  };

  const handleConfigSave = (config) => {
    setEnterpriseConfig(config);
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setEnterpriseConfig(null);
  };

  return (
    <Router>
      <ThemeProvider theme={createTheme()}>
        <CssBaseline />
        {isAuthenticated && (
          <AppBar position="static">
            <Toolbar>
              <Typography variant="h6" sx={{ flexGrow: 1 }}>
                {enterpriseConfig.enterpriseName}
              </Typography>
              <Button 
                color="inherit" 
                component={RouterLink} 
                to="/integrations"
                sx={{ mx: 1 }}
              >
                Integrations
              </Button>
              <Button 
                color="inherit" 
                component={RouterLink} 
                to="/teams"
                sx={{ mx: 1 }}
              >
                Teams
              </Button>
              <Button 
                color="inherit" 
                component={RouterLink} 
                to="/health"
                sx={{ mx: 1 }}
              >
                Health Check
              </Button>
              <Button 
                color="inherit" 
                onClick={() => setIsAuthenticated(false)}
              >
                Logout
              </Button>
            </Toolbar>
          </AppBar>
        )}
        <Routes>
          <Route 
            path="/login" 
            element={
              isAuthenticated ? 
                <Navigate to="/integrations" /> : 
                <LoginPage onLogin={() => setIsAuthenticated(true)} />
            } 
          />
          <Route
            path="/integrations"
            element={
              isAuthenticated ? 
                <IntegrationsDashboard 
                  integrations={integrations}
                  onConnect={handleConnect}
                  onDisconnect={handleDisconnect}
                /> : 
                <Navigate to="/login" />
            }
          />
          <Route
            path="/teams"
            element={
              isAuthenticated ? 
                <TeamsPage teams={teams} integrations={integrations} /> : 
                <Navigate to="/login" />
            }
          />
          <Route
            path="/health"
            element={
              isAuthenticated ? 
                <HealthCheckPage /> : 
                <Navigate to="/login" />
            }
          />
          <Route 
            path="/" 
            element={
              isAuthenticated ? 
                <Navigate to="/integrations" /> : 
                <Navigate to="/login" />
            } 
          />
        </Routes>
        <EnterpriseConfigDialog
          open={configDialogOpen}
          onClose={() => setConfigDialogOpen(false)}
          onSave={handleConfigSave}
          config={enterpriseConfig}
        />
      </ThemeProvider>
    </Router>
  );
}

export default App;

