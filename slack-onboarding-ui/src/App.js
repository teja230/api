import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes, useLocation, Link } from 'react-router-dom';
import './modern.css';
import {
  AppBar, Toolbar, Typography, Button, Card, Box, CircularProgress, Alert, List, ListItem, ListItemText, Container, Stack,
  IconButton, useTheme, ThemeProvider, createTheme, CssBaseline, Tooltip, Chip, Fade, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, Select, MenuItem, FormControl, InputLabel, Grid, Avatar, Divider
} from '@mui/material';
import {
  CheckCircle, Error as ErrorIcon, Home as HomeIcon, Group as GroupIcon, InsertComment as SlackIcon,
  Assignment as JiraIcon, Brightness4, Brightness7, GitHub as GitHubIcon, CalendarMonth as CalendarIcon,
  Security as SecurityIcon, Cloud as CloudIcon, Email as EmailIcon, Storage as StorageIcon,
  Business as BusinessIcon, Settings as SettingsIcon
} from '@mui/icons-material';
import SlackIntegration from './integrations/SlackIntegration';
import JiraIntegration from './integrations/JiraIntegration';
import GitHubIntegration from './integrations/GitHubIntegration';
import GoogleCalendarIntegration from './integrations/GoogleCalendarIntegration';

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

function MenuBar({ enterpriseConfig, onConfigClick }) {
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
        <IconButton onClick={onConfigClick} color="inherit" sx={{ ml: 1 }}>
          <SettingsIcon />
        </IconButton>
        <IconButton onClick={toggleDarkMode} color="inherit" sx={{ ml: 1 }}>
          {darkMode ? <Brightness7 /> : <Brightness4 />}
        </IconButton>
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

function Home({ enterpriseConfig }) {
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

function TeamsPage({ enterpriseConfig }) {
  return (
    <Container maxWidth="sm" sx={{ mt: 6 }}>
      <Card sx={{ p: 4, width: '100%', boxShadow: 6, borderRadius: 3 }}>
        <Typography variant="h5" fontWeight={700} color="primary" gutterBottom>
          Slack Teams
        </Typography>
        <TeamsList enterpriseConfig={enterpriseConfig} />
      </Card>
    </Container>
  );
}

function App() {
  const [darkMode, setDarkMode] = useState(false);
  const [enterpriseConfig, setEnterpriseConfig] = useState(null);
  const [configDialogOpen, setConfigDialogOpen] = useState(false);

  const theme = createTheme({
    palette: {
      mode: darkMode ? 'dark' : 'light',
      primary: {
        main: enterpriseConfig?.primaryColor || '#4A154B',
      },
      secondary: {
        main: enterpriseConfig?.secondaryColor || '#2eb67d',
      },
      background: {
        default: darkMode ? '#121212' : '#f4f1f8',
        paper: darkMode ? '#1e1e1e' : '#ffffff',
      },
    },
    typography: {
      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    },
    components: {
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: 16,
          },
        },
      },
    },
  });

  const handleConfigSave = (config) => {
    setEnterpriseConfig(config);
    // Here you would typically save the config to your backend
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <MenuBar 
          enterpriseConfig={enterpriseConfig} 
          onConfigClick={() => setConfigDialogOpen(true)} 
        />
        <Routes>
          <Route path="/" element={<Home enterpriseConfig={enterpriseConfig} />} />
          <Route path="/teams" element={<TeamsPage enterpriseConfig={enterpriseConfig} />} />
          <Route path="/success" element={<SuccessPage />} />
        </Routes>
        <EnterpriseConfigDialog
          open={configDialogOpen}
          onClose={() => setConfigDialogOpen(false)}
          onSave={handleConfigSave}
          config={enterpriseConfig}
        />
      </Router>
    </ThemeProvider>
  );
}

export default App;

