import React, { useState, useEffect, useCallback } from 'react';
import { 
  Box, 
  Card, 
  CardContent, 
  Typography,
  Grid, 
  Chip, 
  Button,
  CircularProgress, 
  Alert,
  Container,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions
} from '@mui/material';
import { FaSlack, FaGithub, FaJira, FaGoogle } from 'react-icons/fa';
import { integrationApi } from '../services/api';

const IntegrationsPage = () => {
  const [integrations, setIntegrations] = useState([
    {
      id: 'slack',
      name: 'Slack',
      icon: <FaSlack />,
      status: 'disconnected',
      color: '#4A154B',
      description: 'Connect with Slack to receive notifications and manage team communications.',
      features: ['Team Notifications', 'Channel Integration', 'Message Sync']
    },
    {
      id: 'github',
      name: 'GitHub',
      icon: <FaGithub />,
      status: 'disconnected',
      color: '#24292e',
      description: 'Integrate with GitHub to track repositories and manage code-related tasks.',
      features: ['Repository Access', 'Issue Tracking', 'Pull Request Management']
    },
    {
      id: 'jira',
      name: 'Jira',
      icon: <FaJira />,
      status: 'disconnected',
      color: '#0052CC',
      description: 'Connect with Jira to manage projects and track issues across your team.',
      features: ['Project Management', 'Issue Tracking', 'Sprint Planning']
    },
    {
      id: 'google',
      name: 'Google Calendar',
      icon: <FaGoogle />,
      status: 'disconnected',
      color: '#4285F4',
      description: 'Sync with Google Calendar to manage team schedules and meetings.',
      features: ['Calendar Sync', 'Meeting Management', 'Availability Tracking']
    }
  ]);
  const [loadingStates, setLoadingStates] = useState({});
  const [error, setError] = useState(null);
  const [disconnectDialog, setDisconnectDialog] = useState({
    open: false,
    integration: null
  });
  const [isCheckingStatus, setIsCheckingStatus] = useState(false);
  const [lastStatusCheck, setLastStatusCheck] = useState(0);
  const STATUS_CHECK_INTERVAL = 300000; // 5 minutes in milliseconds

  const setLoading = (integrationId, isLoading) => {
    setLoadingStates(prev => ({
      ...prev,
      [integrationId]: isLoading
    }));
  };

  const checkStatuses = useCallback(async () => {
    if (isCheckingStatus) return;
    const now = Date.now();
    if (now - lastStatusCheck < STATUS_CHECK_INTERVAL) return;

    setIsCheckingStatus(true);
    setLastStatusCheck(now);
    setError(null);

    try {
      const statusPromises = integrations.map(async (integration) => {
        try {
          const status = await integrationApi[integration.id].getStatus();
          return {
            ...integration,
            status: status.connected ? 'connected' : 'disconnected'
          };
        } catch (error) {
          console.error(`Failed to fetch status for ${integration.id}:`, error);
          return integration;
        }
      });

      const statuses = await Promise.all(statusPromises);
      setIntegrations(statuses);
    } catch (error) {
      console.error('Failed to fetch integration statuses:', error);
      setError('Failed to fetch integration statuses. Please try again.');
    } finally {
      setIsCheckingStatus(false);
    }
  }, [integrations, isCheckingStatus, lastStatusCheck]);

  useEffect(() => {
    checkStatuses();
  }, [checkStatuses]);

  useEffect(() => {
    const interval = setInterval(checkStatuses, STATUS_CHECK_INTERVAL);
    return () => clearInterval(interval);
  }, [checkStatuses]);

  const handleConnect = async (integration) => {
    setError(null);
    try {
      setLoading(integration.id, true);
      const response = await integrationApi[integration.id].connect();
      
      if (response && response.url) {
        // Store the current integration in sessionStorage before redirecting
        sessionStorage.setItem('connectingIntegration', integration.id);
        // Redirect to the OAuth URL
        window.location.href = response.url;
      } else {
        throw new Error('No OAuth URL received in response');
      }
    } catch (error) {
      console.error('Failed to initiate connection:', error);
      setError(`Failed to connect to ${integration.name}: ${error.message}`);
      setLoading(integration.id, false);
    }
  };

  const handleDisconnectClick = (integration) => {
    setDisconnectDialog({
      open: true,
      integration
    });
  };

  const handleDisconnectConfirm = async () => {
    const integration = disconnectDialog.integration;
    setError(null);
    try {
      setLoading(integration.id, true);
      await integrationApi[integration.id].disconnect();

      // Update the integration status
      setIntegrations(prev => 
        prev.map(i => i.id === integration.id ? { ...i, status: 'disconnected' } : i)
      );

      // Show success message
      setError('Successfully disconnected from ' + integration.name);
    } catch (error) {
      console.error('Failed to disconnect:', error);
      setError(`Failed to disconnect from ${integration.name}. Please try again.`);
    } finally {
      setLoading(integration.id, false);
      setDisconnectDialog({ open: false, integration: null });
    }
  };

  const handleDisconnectCancel = () => {
    setDisconnectDialog({ open: false, integration: null });
  };

  // Check status after OAuth callback
  useEffect(() => {
      const connectingIntegration = sessionStorage.getItem('connectingIntegration');
      if (connectingIntegration) {
          sessionStorage.removeItem('connectingIntegration');
      checkStatuses();
    }
  }, [checkStatuses]);

  const renderIntegrationCard = (integration) => (
    <Grid item xs={12} sm={6} md={6} key={integration.id}>
      <Paper elevation={3} sx={{ borderRadius: 4, overflow: 'hidden' }}>
        <Card sx={{ borderRadius: 4, boxShadow: 'none' }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
              <Box
                sx={{
                  width: 48,
                  height: 48,
                  borderRadius: 3,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  bgcolor: integration.color,
                  color: 'white',
                  mr: 2,
                  boxShadow: 2
                }}
              >
                {integration.icon}
              </Box>
              <Box sx={{ flex: 1 }}>
                <Typography variant="h6" component="div">
                  {integration.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {integration.status === 'connected' ? 'Connected' : 'Not Connected'}
                </Typography>
              </Box>
              {loadingStates[integration.id] ? (
                <CircularProgress size={24} />
              ) : (
                <Button
                  variant={integration.status === 'connected' ? 'outlined' : 'contained'}
                  color={integration.status === 'connected' ? 'error' : 'primary'}
                  onClick={() => 
                    integration.status === 'connected' 
                      ? handleDisconnectClick(integration)
                      : handleConnect(integration)
                  }
                  sx={{ minWidth: 120 }}
                >
                  {integration.status === 'connected' ? 'Disconnect' : 'Connect'}
                </Button>
              )}
            </Box>
            <Typography variant="body1" sx={{ mb: 3 }}>
              {integration.description}
            </Typography>
            <Box>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                Features
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {integration.features.map((feature) => (
                  <Chip
                    key={feature}
                    label={feature}
                    size="small"
                    color="primary"
                    variant="outlined"
                  />
                ))}
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Paper>
    </Grid>
  );

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Integrations
        </Typography>
        <Typography variant="subtitle1" color="text.secondary" sx={{ mb: 2 }}>
          Connect and manage your team's tools and services
        </Typography>
      </Box>

      {error && (
        <Alert 
          severity={error.includes('Successfully') ? 'success' : 'error'} 
          sx={{ mb: 3 }}
          onClose={() => setError(null)}
        >
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {integrations.map(renderIntegrationCard)}
      </Grid>

      <Dialog
        open={disconnectDialog.open}
        onClose={handleDisconnectCancel}
      >
        <DialogTitle>Disconnect {disconnectDialog.integration?.name}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to disconnect from {disconnectDialog.integration?.name}? 
            This will remove all associated permissions and data.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDisconnectCancel}>Cancel</Button>
          <Button 
            onClick={handleDisconnectConfirm} 
            color="error" 
            variant="contained"
          >
            Disconnect
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default IntegrationsPage;
