import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
} from '@mui/material';
import GitHubIcon from '@mui/icons-material/GitHub';

export const GitHubConnection: React.FC = () => {
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    checkConnectionStatus();
  }, []);

  const checkConnectionStatus = async () => {
    try {
      const response = await fetch('/api/github/oauth/status');
      const data = await response.json();
      setIsConnected(data.connected);
    } catch (err) {
      setError('Failed to check connection status');
    } finally {
      setIsLoading(false);
    }
  };

  const handleConnect = async () => {
    try {
      setIsLoading(true);
      const response = await fetch('/api/github/oauth/url');
      const data = await response.json();
      window.location.href = data.url;
    } catch (err) {
      setError('Failed to initiate GitHub connection');
      setIsLoading(false);
    }
  };

  const handleDisconnect = async () => {
    try {
      setIsLoading(true);
      await fetch('/api/github/oauth/disconnect', { method: 'POST' });
      setIsConnected(false);
    } catch (err) {
      setError('Failed to disconnect from GitHub');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Card sx={{ maxWidth: 600, mx: 'auto', mt: 4 }}>
      <CardContent>
        <Box display="flex" alignItems="center" mb={2}>
          <GitHubIcon sx={{ fontSize: 40, mr: 2 }} />
          <Typography variant="h5">GitHub Integration</Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {isConnected ? (
          <>
            <Alert severity="success" sx={{ mb: 2 }}>
              Successfully connected to GitHub
            </Alert>
            <Button
              variant="outlined"
              color="error"
              onClick={handleDisconnect}
              disabled={isLoading}
            >
              Disconnect from GitHub
            </Button>
          </>
        ) : (
          <>
            <Typography variant="body1" gutterBottom>
              Connect your GitHub account to enable repository integration and automation.
            </Typography>
            <Button
              variant="contained"
              startIcon={<GitHubIcon />}
              onClick={handleConnect}
              disabled={isLoading}
            >
              Connect with GitHub
            </Button>
          </>
        )}
      </CardContent>
    </Card>
  );
}; 