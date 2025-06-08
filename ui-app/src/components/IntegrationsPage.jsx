import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { 
  Container, 
  Typography, 
  Box, 
  Card, 
  CardContent, 
  Button, 
  Grid, 
  Chip, 
  CircularProgress, 
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions
} from '@mui/material';
import { FaSlack, FaGithub, FaJira, FaGoogle, FaCheck, FaTimes, FaArrowLeft } from 'react-icons/fa';

const IntegrationsPage = () => {
  const navigate = useNavigate();
  const { type } = useParams();
  const [loadingStates, setLoadingStates] = useState({});
  const [error, setError] = useState(null);
  const [disconnectDialog, setDisconnectDialog] = useState({ open: false, integration: null });
  const [isCheckingStatus, setIsCheckingStatus] = useState(false);
  const [lastStatusCheck, setLastStatusCheck] = useState(0);
  const STATUS_CHECK_INTERVAL = 300000; // 5 minutes in milliseconds
  const [integrations, setIntegrations] = useState([
    {
      id: 'slack',
      name: 'Slack',
      icon: <FaSlack />,
      status: 'disconnected',
      color: '#4A154B',
      description: 'Connect your Slack workspace to enable team communication and notifications.',
      features: ['Team Channels', 'Direct Messages', 'Notifications', 'Status Updates']
    },
    {
      id: 'github',
      name: 'GitHub',
      icon: <FaGithub />,
      status: 'disconnected',
      color: '#24292e',
      description: 'Connect your GitHub account to manage repositories and access control.',
      features: ['Repository Access', 'Team Management', 'Code Reviews', 'Issue Tracking']
    },
    {
      id: 'jira',
      name: 'Jira',
      icon: <FaJira />,
      status: 'disconnected',
      color: '#0052CC',
      description: 'Connect your Jira instance to manage projects and track issues.',
      features: ['Project Management', 'Issue Tracking', 'Sprint Planning', 'Workflow Automation']
    },
    {
      id: 'google',
      name: 'Google Calendar',
      icon: <FaGoogle />,
      status: 'disconnected',
      color: '#4285F4',
      description: 'Connect your Google Calendar to manage team schedules and meetings.',
      features: ['Meeting Scheduling', 'Calendar Sync', 'Availability Management', 'Event Notifications']
    }
  ]);

  const setLoading = (integrationId, isLoading) => {
    setLoadingStates(prev => ({
      ...prev,
      [integrationId]: isLoading
    }));
  };

  const checkStatuses = useCallback(async () => {
    // Prevent multiple simultaneous status checks
    if (isCheckingStatus) {
      return;
    }

    // Prevent too frequent status checks
    const now = Date.now();
    if (now - lastStatusCheck < STATUS_CHECK_INTERVAL) {
      return;
    }

    setIsCheckingStatus(true);
    setLastStatusCheck(now);
    setError(null);

    try {
      const statusPromises = integrations.map(async (integration) => {
        try {
          const response = await fetch(`http://localhost:8080/api/${integration.id}/status?enterpriseId=default`, {
            credentials: 'include',
            headers: {
              'Accept': 'application/json'
            }
          });
          
          if (!response.ok) {
            console.error(`Failed to fetch status for ${integration.id}: ${response.status}`);
            return integration; // Keep existing status on error
          }

          const data = await response.json();
          return {
            ...integration,
            status: data.data ? 'connected' : 'disconnected'
          };
        } catch (error) {
          console.error(`Failed to fetch status for ${integration.id}:`, error);
          return integration; // Keep existing status on error
        }
      });

      const statuses = await Promise.all(statusPromises);
      setIntegrations(statuses);
    } catch (error) {
      console.error('Failed to fetch integration statuses:', error);
      setError({
        severity: 'error',
        message: 'Failed to fetch integration statuses. Please try again.'
      });
    } finally {
      setIsCheckingStatus(false);
    }
  }, [integrations, isCheckingStatus, lastStatusCheck]);

  // Initial status check on mount
  useEffect(() => {
    checkStatuses();
  }, [checkStatuses]); // Add checkStatuses to dependencies

  // Periodic status check
  useEffect(() => {
    const interval = setInterval(checkStatuses, STATUS_CHECK_INTERVAL);
    return () => clearInterval(interval);
  }, [checkStatuses]);

  const handleConnect = async (integration) => {
    setError(null);
    try {
      setLoading(integration.id, true);
      console.log(`Fetching OAuth URL for ${integration.id}...`);
      
      const response = await fetch(`http://localhost:8080/api/${integration.id}/oauth/url?enterpriseId=default`, {
        credentials: 'include',
        headers: {
          'Accept': 'application/json'
        }
      });
      
      console.log('OAuth URL response:', response);
      
      if (!response.ok) {
        if (response.status === 401) {
          // Redirect to login if not authenticated
          window.location.href = '/login';
          return;
        }
        throw new Error(`Failed to get OAuth URL: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      console.log('OAuth URL data:', data);

      if (!data.data) {
        throw new Error('No OAuth URL received in response');
      }

      // Store the current integration in sessionStorage before redirecting
      sessionStorage.setItem('connectingIntegration', integration.id);
      
      // Redirect to the OAuth URL
      console.log('Redirecting to:', data.data);
      window.location.href = data.data;
    } catch (error) {
      console.error('Failed to initiate connection:', error);
      setError({
        severity: 'error',
        message: `Failed to connect to ${integration.name}: ${error.message}`
      });
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
      const response = await fetch(`http://localhost:8080/api/${integration.id}/disconnect`, { 
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify({ enterpriseId: 'default' })
      });
      
      if (!response.ok) {
        if (response.status === 401) {
          // Redirect to login if not authenticated
          window.location.href = '/login';
          return;
        }
        throw new Error('Failed to disconnect');
      }

      // Update the integration status
      setIntegrations(prev => 
        prev.map(i => i.id === integration.id ? { ...i, status: 'disconnected' } : i)
      );

      // Show success message
      setError({
        severity: 'success',
        message: `Successfully disconnected from ${integration.name}`
      });
    } catch (error) {
      console.error('Failed to disconnect:', error);
      setError({
        severity: 'error',
        message: `Failed to disconnect from ${integration.name}. Please try again.`
      });
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
    const checkCallback = async () => {
      const connectingIntegration = sessionStorage.getItem('connectingIntegration');
      if (connectingIntegration) {
        try {
          const response = await fetch(`http://localhost:8080/api/${connectingIntegration}/status?enterpriseId=default`, {
            credentials: 'include',
            headers: {
              'Accept': 'application/json'
            }
          });
          
          if (response.ok) {
            const data = await response.json();
            if (data.data) {
              setIntegrations(prev => 
                prev.map(i => i.id === connectingIntegration ? { ...i, status: 'connected' } : i)
              );
              setError({
                severity: 'success',
                message: `Successfully connected to ${connectingIntegration}`
              });
            }
          } else if (response.status === 401) {
            // Redirect to login if not authenticated
            window.location.href = '/login';
            return;
          }
        } catch (error) {
          console.error('Failed to check connection status:', error);
          setError({
            severity: 'error',
            message: `Failed to verify connection to ${connectingIntegration}`
          });
        } finally {
          sessionStorage.removeItem('connectingIntegration');
        }
      }
    };

    checkCallback();
  }, []);

  // If a specific integration type is provided, show only that integration
  const filteredIntegrations = type 
    ? integrations.filter(integration => integration.id === type)
    : integrations;

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Button
        startIcon={<FaArrowLeft />}
        onClick={() => navigate('/dashboard')}
        sx={{ mb: 4 }}
      >
        Back to Dashboard
      </Button>

      <Typography variant="h4" component="h1" gutterBottom>
        {type ? `${filteredIntegrations[0]?.name || 'Integration'} Configuration` : 'Integrations'}
      </Typography>
      <Typography variant="subtitle1" color="text.secondary" sx={{ mb: 4 }}>
        {type 
          ? 'Configure and manage your integration settings'
          : 'Connect your favorite tools to streamline your onboarding process'
        }
      </Typography>

      {error && (
        <Alert 
          severity={error.severity || 'error'} 
          sx={{ mb: 4 }}
          onClose={() => setError(null)}
        >
          {error.message || error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {filteredIntegrations.map((integration) => (
          <Grid item xs={12} md={type ? 12 : 6} key={integration.id}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                  <Box
                    sx={{
                      width: 64,
                      height: 64,
                      borderRadius: '16px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      bgcolor: `${integration.color}15`,
                      color: integration.color,
                      mr: 3
                    }}
                  >
                    {integration.icon}
                  </Box>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="h5" component="div">
                      {integration.name}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                      <Chip
                        icon={integration.status === 'connected' ? <FaCheck /> : <FaTimes />}
                        label={integration.status}
                        color={integration.status === 'connected' ? 'success' : 'default'}
                        size="small"
                      />
                    </Box>
                  </Box>
                </Box>

                <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                  {integration.description}
                </Typography>

                <Box sx={{ mb: 3 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Features:
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {integration.features.map((feature, index) => (
                      <Chip
                        key={index}
                        label={feature}
                        size="small"
                        variant="outlined"
                      />
                    ))}
                  </Box>
                </Box>

                <Button
                  variant={integration.status === 'connected' ? 'outlined' : 'contained'}
                  color={integration.status === 'connected' ? 'error' : 'primary'}
                  fullWidth
                  disabled={loadingStates[integration.id]}
                  onClick={() => 
                    integration.status === 'connected' 
                      ? handleDisconnectClick(integration)
                      : handleConnect(integration)
                  }
                  startIcon={loadingStates[integration.id] ? <CircularProgress size={20} /> : null}
                >
                  {integration.status === 'connected' ? 'Disconnect' : 'Connect'}
                </Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Dialog
        open={disconnectDialog.open}
        onClose={handleDisconnectCancel}
        aria-labelledby="disconnect-dialog-title"
        aria-describedby="disconnect-dialog-description"
      >
        <DialogTitle id="disconnect-dialog-title">
          Disconnect {disconnectDialog.integration?.name}
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="disconnect-dialog-description">
            Are you sure you want to disconnect from {disconnectDialog.integration?.name}? 
            This will remove all access and you'll need to reconnect to use this integration again.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDisconnectCancel} color="primary">
            Cancel
          </Button>
          <Button 
            onClick={handleDisconnectConfirm} 
            color="error" 
            variant="contained"
            disabled={loadingStates[disconnectDialog.integration?.id]}
          >
            {loadingStates[disconnectDialog.integration?.id] ? (
              <>
                <CircularProgress size={20} sx={{ mr: 1 }} />
                Disconnecting...
              </>
            ) : (
              'Disconnect'
            )}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default IntegrationsPage; 