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
  Alert
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import { getSystemHealth } from '../services/api';

const HealthPage = () => {
  const [services, setServices] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchHealthData = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getSystemHealth();
      if (response && response.services) {
        setServices(response.services);
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
    // Refresh every 30 seconds
    const interval = setInterval(fetchHealthData, 30000);
    return () => clearInterval(interval);
  }, []);

  const getServiceName = (serviceId) => {
    const names = {
      'api-layer': 'API Service',
      'github': 'GitHub Integration',
      'slack': 'Slack Integration',
      'jira': 'Jira Integration',
      'google-calendar': 'Google Calendar Integration'
    };
    return names[serviceId] || serviceId;
  };

  const formatLastChecked = (timestamp) => {
    if (!timestamp) return 'Never';
    const date = new Date(timestamp);
    return date.toLocaleString();
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'up':
        return 'success';
      default:
        return 'error';
    }
  };

  if (loading && Object.keys(services).length === 0) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1">
          System Health
        </Typography>
        <IconButton onClick={fetchHealthData} disabled={loading}>
          <RefreshIcon />
        </IconButton>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {Object.entries(services).map(([serviceId, data]) => (
          <Grid item xs={12} sm={6} md={4} key={serviceId}>
            <Card>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                  <Typography variant="h6" component="h2">
                    {getServiceName(serviceId)}
                  </Typography>
                  <Chip
                    label={data.status || 'UNKNOWN'}
                    color={getStatusColor(data.status)}
                    size="small"
                  />
                </Box>
                <Typography color="textSecondary" gutterBottom>
                  Last Checked: {formatLastChecked(data.lastChecked)}
                </Typography>
                {data.error && (
                  <Typography color="error" variant="body2">
                    Error: {data.error}
                  </Typography>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default HealthPage; 