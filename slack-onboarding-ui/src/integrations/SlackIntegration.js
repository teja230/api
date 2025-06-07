import React, { useState, useEffect } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Button,
  Box,
  Alert,
  CircularProgress,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider
} from '@mui/material';
import {
  InsertComment as SlackIcon,
  CheckCircle,
  Error as ErrorIcon,
  Refresh as RefreshIcon
} from '@mui/icons-material';

function SlackIntegration() {
  const [status, setStatus] = useState({ loading: true, connected: false, error: null });
  const [teams, setTeams] = useState([]);
  const [loadingTeams, setLoadingTeams] = useState(false);

  const fetchStatus = async () => {
    try {
      const response = await fetch('/api/slack/status');
      const data = await response.json();
      setStatus({ loading: false, connected: data.connected, error: null });
    } catch (error) {
      setStatus({ loading: false, connected: false, error: 'Failed to fetch Slack status' });
    }
  };

  const fetchTeams = async () => {
    setLoadingTeams(true);
    try {
      const response = await fetch('/api/slack/teams');
      const data = await response.json();
      setTeams(data);
    } catch (error) {
      console.error('Failed to fetch teams:', error);
    } finally {
      setLoadingTeams(false);
    }
  };

  useEffect(() => {
    fetchStatus();
  }, []);

  const handleConnect = async () => {
    try {
      const response = await fetch('/api/slack/oauth/url');
      const data = await response.json();
      window.location.href = data.url;
    } catch (error) {
      setStatus(prev => ({ ...prev, error: 'Failed to initiate Slack connection' }));
    }
  };

  const handleDisconnect = async () => {
    try {
      await fetch('/api/slack/disconnect', { method: 'POST' });
      setStatus({ loading: false, connected: false, error: null });
      setTeams([]);
    } catch (error) {
      setStatus(prev => ({ ...prev, error: 'Failed to disconnect Slack' }));
    }
  };

  const handleRefreshTeams = () => {
    fetchTeams();
  };

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box display="flex" alignItems="center" mb={2}>
          <SlackIcon color="primary" sx={{ mr: 1 }} />
          <Typography variant="h6" component="div">
            Slack Integration
          </Typography>
        </Box>

        {status.error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {status.error}
          </Alert>
        )}

        <Box mb={3}>
          <Typography variant="body1" gutterBottom>
            Status: {status.loading ? (
              <CircularProgress size={16} sx={{ ml: 1 }} />
            ) : status.connected ? (
              <span style={{ color: '#2eb67d' }}>
                <CheckCircle fontSize="small" sx={{ verticalAlign: 'middle', mr: 0.5 }} />
                Connected
              </span>
            ) : (
              <span style={{ color: '#e01e5a' }}>
                <ErrorIcon fontSize="small" sx={{ verticalAlign: 'middle', mr: 0.5 }} />
                Not Connected
              </span>
            )}
          </Typography>
        </Box>

        {status.connected ? (
          <>
            <Button
              variant="outlined"
              color="primary"
              onClick={handleRefreshTeams}
              disabled={loadingTeams}
              startIcon={loadingTeams ? <CircularProgress size={20} /> : <RefreshIcon />}
              sx={{ mb: 2 }}
            >
              Refresh Teams
            </Button>

            {teams.length > 0 && (
              <List>
                {teams.map((team, index) => (
                  <React.Fragment key={team.id}>
                    {index > 0 && <Divider />}
                    <ListItem>
                      <ListItemIcon>
                        <SlackIcon color="primary" />
                      </ListItemIcon>
                      <ListItemText
                        primary={team.name}
                        secondary={`Team ID: ${team.id}`}
                      />
                    </ListItem>
                  </React.Fragment>
                ))}
              </List>
            )}

            <Button
              variant="outlined"
              color="error"
              onClick={handleDisconnect}
              sx={{ mt: 2 }}
            >
              Disconnect Slack
            </Button>
          </>
        ) : (
          <Button
            variant="contained"
            color="primary"
            onClick={handleConnect}
            disabled={status.loading}
          >
            Connect Slack
          </Button>
        )}
      </CardContent>
    </Card>
  );
}

export default SlackIntegration;
