import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Chip,
  IconButton,
  CircularProgress,
  Alert,
  Container,
  Paper,
  Tooltip
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import { API_BASE_URL } from '../services/api';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import GitHubIcon from '@mui/icons-material/GitHub';
import { FaSlack } from 'react-icons/fa';
import GoogleIcon from '@mui/icons-material/Google';
import BugReportIcon from '@mui/icons-material/BugReport';
import StorageIcon from '@mui/icons-material/Storage';
import { Avatar } from '@mui/material';

const getServiceIcon = (serviceId) => {
  const normalizedId = serviceId.toLowerCase().replace(/-/g, '');
  if (normalizedId.includes('github')) return <GitHubIcon style={{ fontSize: 32 }} />;
  if (normalizedId.includes('slack')) return <FaSlack style={{ fontSize: 32 }} />;
  if (normalizedId.includes('google')) return <GoogleIcon style={{ fontSize: 32 }} />;
  if (normalizedId.includes('jira')) return <BugReportIcon style={{ fontSize: 32 }} />;
  if (normalizedId.includes('api')) return <StorageIcon style={{ fontSize: 32 }} />;
  return <StorageIcon style={{ fontSize: 32 }} />;
};

const getServiceColor = (serviceId) => {
  const normalizedId = serviceId.toLowerCase().replace(/-/g, '');
  if (normalizedId.includes('github')) return '#24292e';
  if (normalizedId.includes('slack')) return '#4A154B';
  if (normalizedId.includes('google')) return '#4285F4';
  if (normalizedId.includes('jira')) return '#0052CC';
  if (normalizedId.includes('api')) return '#1976d2';
  return 'primary.main';
};

const HealthPage = () => {
  const [health, setHealth] = useState({ status: '', details: {} });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);

  const fetchHealthData = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await fetch(`${API_BASE_URL}/api/health/aggregate`, { credentials: 'include' });
      const data = await response.json();
      if (data && typeof data === 'object' && data.details) {
        setHealth(data);
        setLastUpdated(Date.now());
      } else {
        setError('Invalid health data format received');
      }
    } catch (err) {
      setError('Failed to fetch health data. Please try again.');
      console.error('Health check error:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealthData();
    // Refresh every 5 minutes
    const interval = setInterval(fetchHealthData, 300000);
    return () => clearInterval(interval);
  }, []);

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'up':
        return 'success';
      case 'down':
        return 'error';
      default:
        return 'warning';
    }
  };

  const getStatusIcon = (status) => {
    switch (status?.toLowerCase()) {
      case 'up':
        return <CheckCircleIcon color="success" />;
      case 'down':
        return <ErrorIcon color="error" />;
      default:
        return <ErrorIcon color="warning" />;
    }
  };

  const formatLastChecked = (timestamp) => {
    if (!timestamp) return 'Never';
    const date = new Date(timestamp);
    return date.toLocaleString();
  };

  if (loading && Object.keys(health.details).length === 0) {
    return (
      <Container maxWidth="lg">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            System Health
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Monitor the status of all system services and integrations
          </Typography>
          <Box sx={{ mt: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
            {getStatusIcon(health.status)}
            <Chip 
              label={`Overall: ${health.status || 'UNKNOWN'}`} 
              color={getStatusColor(health.status)} 
              size="medium"
              sx={{ 
                fontWeight: 'bold',
                '& .MuiChip-label': {
                  px: 2
                }
              }}
            />
          </Box>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {lastUpdated && (
            <Typography variant="body2" color="text.secondary">
              Last updated: {formatLastChecked(lastUpdated)}
            </Typography>
          )}
          <Tooltip title="Refresh">
            <IconButton 
              onClick={fetchHealthData} 
              disabled={loading}
              color="primary"
              sx={{ 
                bgcolor: 'background.paper',
                boxShadow: 1,
                '&:hover': { bgcolor: 'action.hover' }
              }}
            >
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {error && (
        <Alert 
          severity="error" 
          sx={{ mb: 3 }}
          action={
            <IconButton
              aria-label="retry"
              color="inherit"
              size="small"
              onClick={fetchHealthData}
            >
              <RefreshIcon />
            </IconButton>
          }
        >
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {Object.entries(health.details).map(([serviceId, data]) => (
          <Grid item xs={12} sm={6} md={4} key={serviceId}>
            <Paper 
              elevation={3}
              sx={{
                transition: 'transform 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)'
                }
              }}
            >
              <Card>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                    <Typography variant="h6" component="h2" sx={{ fontWeight: 'bold' }}>
                      {serviceId.charAt(0).toUpperCase() + serviceId.slice(1)}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {getStatusIcon(data.status)}
                      <Chip
                        label={data.status || 'UNKNOWN'}
                        color={getStatusColor(data.status)}
                        size="small"
                        sx={{ 
                          fontWeight: 'bold',
                          '& .MuiChip-label': {
                            px: 2
                          }
                        }}
                      />
                    </Box>
                  </Box>
                  {data.error && (
                    <Alert severity="error" sx={{ mt: 1 }}>
                      {data.error}
                    </Alert>
                  )}
                  {data.details && (
                    <Box sx={{ mt: 2 }}>
                      <Typography variant="body2" color="text.secondary">
                        Last checked: {formatLastChecked(data.lastChecked)}
                      </Typography>
                    </Box>
                  )}
                </CardContent>
              </Card>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
};

export default HealthPage;
