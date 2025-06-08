import React, { useState } from 'react';
import { Container, Typography, Grid, Card, CardContent, Button, Box, Chip } from '@mui/material';
import { FaCheck, FaTimes, FaSync } from 'react-icons/fa';

const HealthPage = () => {
  const [services] = useState([
    {
      id: 'api',
      name: 'API Service',
      status: 'healthy',
      uptime: '99.9%',
      lastChecked: '2 minutes ago'
    },
    {
      id: 'slack',
      name: 'Slack Integration',
      status: 'healthy',
      uptime: '99.8%',
      lastChecked: '1 minute ago'
    },
    {
      id: 'github',
      name: 'GitHub Integration',
      status: 'healthy',
      uptime: '99.9%',
      lastChecked: '2 minutes ago'
    },
    {
      id: 'jira',
      name: 'Jira Integration',
      status: 'degraded',
      uptime: '98.5%',
      lastChecked: '3 minutes ago'
    },
    {
      id: 'google',
      name: 'Google Calendar Integration',
      status: 'healthy',
      uptime: '99.7%',
      lastChecked: '1 minute ago'
    }
  ]);

  const getStatusColor = (status) => {
    switch (status) {
      case 'healthy':
        return 'success';
      case 'degraded':
        return 'warning';
      case 'unhealthy':
        return 'error';
      default:
        return 'default';
    }
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" component="h1">
          System Health
        </Typography>
        <Button
          variant="outlined"
          startIcon={<FaSync />}
          onClick={() => {/* TODO: Implement refresh */}}
        >
          Refresh
        </Button>
      </Box>

      <Grid container spacing={3}>
        {services.map((service) => (
          <Grid item xs={12} md={6} lg={4} key={service.id}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6" component="div">
                    {service.name}
                  </Typography>
                  <Box sx={{ ml: 'auto' }}>
                    <Chip
                      icon={service.status === 'healthy' ? <FaCheck /> : <FaTimes />}
                      label={service.status}
                      color={getStatusColor(service.status)}
                      size="small"
                    />
                  </Box>
                </Box>

                <Box sx={{ mb: 1 }}>
                  <Typography variant="body2" color="text.secondary">
                    Uptime: {service.uptime}
                  </Typography>
                </Box>

                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Last checked: {service.lastChecked}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
};

export default HealthPage; 