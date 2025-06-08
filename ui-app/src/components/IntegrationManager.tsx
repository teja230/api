import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  Grid,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from '@mui/material';
import {
  GitHub as GitHubIcon,
  BugReport as JiraIcon,
  Cloud as AzureIcon,
  Chat as SlackIcon,
  Google as GoogleIcon,
} from '@mui/icons-material';

interface Integration {
  type: string;
  displayName: string;
  icon: React.ReactNode;
  isConnected: boolean;
  isLoading: boolean;
}

export const IntegrationManager: React.FC = () => {
  const [integrations, setIntegrations] = useState<Integration[]>([
    { type: 'GITHUB', displayName: 'GitHub', icon: <GitHubIcon />, isConnected: false, isLoading: true },
    { type: 'JIRA', displayName: 'Jira', icon: <JiraIcon />, isConnected: false, isLoading: true },
    { type: 'AZURE', displayName: 'Azure DevOps', icon: <AzureIcon />, isConnected: false, isLoading: true },
    { type: 'SLACK', displayName: 'Slack', icon: <SlackIcon />, isConnected: false, isLoading: true },
    { type: 'GOOGLE', displayName: 'Google', icon: <GoogleIcon />, isConnected: false, isLoading: true },
  ]);

  const [selectedIntegration, setSelectedIntegration] = useState<Integration | null>(null);
  const [showConfigDialog, setShowConfigDialog] = useState(false);
  const [clientId, setClientId] = useState('');
  const [clientSecret, setClientSecret] = useState('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    checkAllConnections();
  }, []);

  const checkAllConnections = async () => {
    const updatedIntegrations = await Promise.all(
      integrations.map(async (integration) => {
        try {
          const response = await fetch(`/api/${integration.type.toLowerCase()}/oauth/status`);
          const data = await response.json();
          return { ...integration, isConnected: data.connected, isLoading: false };
        } catch (err) {
          return { ...integration, isConnected: false, isLoading: false };
        }
      })
    );
    setIntegrations(updatedIntegrations);
  };

  const handleConnect = async (integration: Integration) => {
    try {
      const response = await fetch(`/api/${integration.type.toLowerCase()}/oauth/url`);
      const data = await response.json();
      window.location.href = data.url;
    } catch (err) {
      setError(`Failed to connect to ${integration.displayName}`);
    }
  };

  const handleDisconnect = async (integration: Integration) => {
    try {
      await fetch(`/api/${integration.type.toLowerCase()}/oauth/disconnect`, {
        method: 'POST',
      });
      checkAllConnections();
    } catch (err) {
      setError(`Failed to disconnect from ${integration.displayName}`);
    }
  };

  const handleConfigure = (integration: Integration) => {
    setSelectedIntegration(integration);
    setShowConfigDialog(true);
  };

  const handleSaveConfig = async () => {
    if (!selectedIntegration) return;

    try {
      const response = await fetch(`/api/${selectedIntegration.type.toLowerCase()}/oauth/config`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ clientId, clientSecret }),
      });

      if (!response.ok) throw new Error('Failed to save configuration');
      
      setShowConfigDialog(false);
      setClientId('');
      setClientSecret('');
      checkAllConnections();
    } catch (err) {
      setError('Failed to save configuration');
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Integrations
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {integrations.map((integration) => (
          <Grid item xs={12} sm={6} md={4} key={integration.type}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" mb={2}>
                  {integration.icon}
                  <Typography variant="h6" sx={{ ml: 1 }}>
                    {integration.displayName}
                  </Typography>
                </Box>

                {integration.isLoading ? (
                  <CircularProgress size={24} />
                ) : integration.isConnected ? (
                  <>
                    <Alert severity="success" sx={{ mb: 2 }}>
                      Connected
                    </Alert>
                    <Box display="flex" gap={1}>
                      <Button
                        variant="outlined"
                        color="error"
                        onClick={() => handleDisconnect(integration)}
                      >
                        Disconnect
                      </Button>
                      <Button
                        variant="outlined"
                        onClick={() => handleConfigure(integration)}
                      >
                        Configure
                      </Button>
                    </Box>
                  </>
                ) : (
                  <>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Not connected
                    </Typography>
                    <Box display="flex" gap={1}>
                      <Button
                        variant="contained"
                        onClick={() => handleConnect(integration)}
                      >
                        Connect
                      </Button>
                      <Button
                        variant="outlined"
                        onClick={() => handleConfigure(integration)}
                      >
                        Configure
                      </Button>
                    </Box>
                  </>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Dialog open={showConfigDialog} onClose={() => setShowConfigDialog(false)}>
        <DialogTitle>
          Configure {selectedIntegration?.displayName} Integration
        </DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <TextField
              fullWidth
              label="Client ID"
              value={clientId}
              onChange={(e) => setClientId(e.target.value)}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Client Secret"
              type="password"
              value={clientSecret}
              onChange={(e) => setClientSecret(e.target.value)}
              margin="normal"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowConfigDialog(false)}>Cancel</Button>
          <Button onClick={handleSaveConfig} variant="contained">
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}; 