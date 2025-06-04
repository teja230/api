import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes, useLocation, Link } from 'react-router-dom';
import './modern.css';
import {
  AppBar, Toolbar, Typography, Button, Card, Box, CircularProgress, Alert, List, ListItem, ListItemText, Container, Stack
} from '@mui/material';
import { CheckCircle, Error as ErrorIcon, Home as HomeIcon, Group as GroupIcon, InsertComment as SlackIcon, Assignment as JiraIcon } from '@mui/icons-material';

function useQuery() {
  return new URLSearchParams(useLocation().search);
}

function MenuBar() {
  // Print React version for debugging
  console.log('React version:', React.version);
  return (
    <AppBar position="static" color="default" elevation={2} sx={{ mb: 4 }}>
      <Toolbar>
        <SlackIcon sx={{ color: '#4A154B', mr: 1 }} />
        <Typography variant="h6" color="primary" sx={{ flexGrow: 1, fontWeight: 700 }}>
          Onboarding Buddy
        </Typography>
        <Button component={Link} to="/" color="primary" startIcon={<HomeIcon />} sx={{ fontWeight: 600 }}>
          Home
        </Button>
        <Button component={Link} to="/teams" color="primary" startIcon={<GroupIcon />} sx={{ fontWeight: 600 }}>
          Teams
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

function TeamsList() {
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

function IntegrationsList() {
  // Example integrations, can be extended
  const integrations = [
    {
      name: 'Slack',
      description: 'Connect your Slack workspace for onboarding notifications.',
      icon: <SlackIcon sx={{ color: '#4A154B', fontSize: 40 }} />,
      image: 'https://a.slack-edge.com/80588/marketing/img/icons/icon_slack_hash_colored.png',
      connected: true, // Replace with real status if available
      connectUrl: '/api/slack/oauth/url',
    },
    {
      name: 'JIRA',
      description: 'Integrate with JIRA to sync onboarding tasks.',
      icon: <JiraIcon sx={{ color: '#0052CC', fontSize: 40 }} />,
      image: 'https://wac-cdn.atlassian.com/dam/jcr:813202b6-6b8e-4b7e-8e6e-6b3b3b3b3b3b/jira.png',
      connected: false, // Replace with real status if available
      connectUrl: '#', // Replace with real URL
    },
    {
      name: 'Workday',
      description: 'Automate HR onboarding workflows with Workday integration.',
      icon: null,
      image: 'https://upload.wikimedia.org/wikipedia/commons/4/4d/Workday_Logo.png',
      connected: false,
      connectUrl: '#',
    },
    {
      name: 'BambooHR',
      description: 'Sync employee data and onboarding tasks with BambooHR.',
      icon: null,
      image: 'https://cdn.bamboohr.com/images/icons/favicon-96x96.png',
      connected: false,
      connectUrl: '#',
    },
    {
      name: 'Google Workspace',
      description: 'Provision accounts and manage onboarding with Google Workspace.',
      icon: null,
      image: 'https://ssl.gstatic.com/docs/doclist/images/mediatype/icon_1_document_x32.png',
      connected: false,
      connectUrl: '#',
    },
    {
      name: 'Okta',
      description: 'Automate user provisioning and SSO onboarding with Okta.',
      icon: null,
      image: 'https://www.okta.com/sites/default/files/Okta_Logo_BrightBlue_Medium-thumbnail.png',
      connected: false,
      connectUrl: '#',
    },
  ];

  return (
    <Stack spacing={3}>
      <Box sx={{
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
        gap: 3,
      }}>
        {integrations.map((integration) => (
          <Card
            key={integration.name}
            sx={{
              display: 'flex',
              alignItems: 'center',
              p: 2.5,
              borderRadius: 3,
              boxShadow: 3,
              bgcolor: '#faf9fb',
              minHeight: 120,
              transition: 'transform 0.18s, box-shadow 0.18s',
              cursor: 'pointer',
              '&:hover': {
                transform: 'translateY(-4px) scale(1.025)',
                boxShadow: 8,
                bgcolor: '#f5f0fa',
              },
            }}
          >
            <Box sx={{ mr: 2, minWidth: 56, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              {integration.image ? (
                <img src={integration.image} alt={integration.name} style={{ width: 48, height: 48, objectFit: 'contain', borderRadius: 8, background: '#fff' }} />
              ) : (
                integration.icon
              )}
            </Box>
            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="h6" fontWeight={700} color="primary.main">{integration.name}</Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>{integration.description}</Typography>
              {integration.connected ? (
                <Alert icon={<CheckCircle fontSize="inherit" />} severity="success" sx={{ width: 'fit-content', p: 0.5, fontSize: 14 }}>Connected</Alert>
              ) : (
                <Button variant="contained" size="small" href={integration.connectUrl} sx={{ mt: 1, fontWeight: 600 }}>Connect</Button>
              )}
            </Box>
          </Card>
        ))}
      </Box>
    </Stack>
  );
}

function Home() {
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
          <IntegrationsList />
        </Card>
      </Container>
    </Box>
  );
}

function TeamsPage() {
  return (
    <Container maxWidth="sm" sx={{ mt: 6 }}>
      <Card sx={{ p: 4, width: '100%', boxShadow: 6, borderRadius: 3 }}>
        <Typography variant="h5" fontWeight={700} color="primary" gutterBottom>
          Slack Teams
        </Typography>
        <TeamsList />
      </Card>
    </Container>
  );
}

function App() {
  return (
    <Router>
      <MenuBar />
      <Routes>
        <Route path="/success" element={<SuccessPage />} />
        <Route path="/teams" element={<TeamsPage />} />
        <Route path="/" element={<Home />} />
      </Routes>
    </Router>
  );
}

export default App;

