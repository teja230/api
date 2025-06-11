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
import ErrorIcon from '@mui/icons-material/Error';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { getSystemHealth } from '../services/api';

const HealthPage = () => {
  const [services, setServices] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);

  const fetchHealthData = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getSystemHealth();
      if (response && response.services) {
        setServices(response.services);
        setLastUpdated(response.timestamp);
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

  if (loading && Object.keys(services).length === 0) {
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
        {Object.entries(services).map(([serviceId, data]) => (
          <Grid item xs={12} sm={6} md={4} key={serviceId}>
            <Paper elevation={3}>
              <Card>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                    <Typography variant="h6" component="h2">
                      {data.name}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {getStatusIcon(data.status)}
                      <Chip
                        label={data.status || 'UNKNOWN'}
                        color={getStatusColor(data.status)}
                        size="small"
                      />
                    </Box>
                  </Box>
                  <Typography color="textSecondary" gutterBottom>
                    Last Checked: {formatLastChecked(data.lastChecked)}
                  </Typography>
                  {data.error && (
                    <Typography color="error" variant="body2" sx={{ mt: 1 }}>
                      Error: {data.error}
                    </Typography>
                  )}
                  {data.details && data.details.details && (
                    <Box sx={{ mt: 2 }}>
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        Details:
                      </Typography>
                      {Object.entries(data.details.details).map(([key, value]) => (
                        <Typography key={key} variant="body2" color="text.secondary">
                          {key}: {typeof value === 'object' ? JSON.stringify(value) : value}
                        </Typography>
                      ))}
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