import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Container, Grid, Card, CardContent, Typography, Box, Button, Chip, CircularProgress } from '@mui/material';
import {
  FaUsers, 
  FaRocket, 
  FaClock, 
  FaChartLine,
  FaUserPlus,
  FaChartBar,
  FaCog,
  FaClipboardList
} from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import { API_BASE_URL } from '../services/api';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';

const Dashboard = () => {
  const navigate = useNavigate();

  const quickActions = [
    {
      title: 'Add New Team',
      icon: <FaUserPlus />,
      path: '/teams/new',
      variant: 'contained'
    },
    {
      title: 'View Progress',
      icon: <FaClipboardList />,
      path: '/teams/progress',
      variant: 'outlined'
    },
    {
      title: 'Manage Integrations',
      icon: <FaCog />,
      path: '/integrations',
      variant: 'outlined'
    },
    {
      title: 'View Reports',
      icon: <FaChartBar />,
      path: '/reports',
      variant: 'outlined'
    }
  ];

  const handleQuickActionClick = (action) => {
    navigate(action.path);
  };

  const stats = [
    {
      title: 'Active Teams',
      value: '12',
      icon: <FaUsers />,
      color: '#1976d2',
      change: '+2 this week'
    },
    {
      title: 'Onboarding Progress',
      value: '85%',
      icon: <FaRocket />,
      color: '#2e7d32',
      change: '+5% this week'
    },
    {
      title: 'Time Saved',
      value: '42h',
      icon: <FaClock />,
      color: '#ed6c02',
      change: 'This month'
    },
    {
      title: 'Productivity',
      value: '92%',
      icon: <FaChartLine />,
      color: '#9c27b0',
      change: '+8% this month'
    }
  ];

  const [healthSummary, setHealthSummary] = useState({ loading: true, allUp: true, details: {} });

  useEffect(() => {
    const fetchHealth = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/health/aggregate`, { credentials: 'include' });
        const data = await res.json();
        const allUp = Object.values(data).every(s => s.status && s.status.toLowerCase() === 'up');
        setHealthSummary({ loading: false, allUp, details: data });
      } catch {
        setHealthSummary({ loading: false, allUp: false, details: {} });
      }
    };
    fetchHealth();
  }, []);

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Welcome to Onboarding Dashboard
      </Typography>
      <Typography variant="subtitle1" color="text.secondary" sx={{ mb: 4 }}>
        Track your team's onboarding progress and integration status
      </Typography>

      {/* Stats Grid */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {stats.map((stat, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
            >
              <Card sx={{ height: '100%', borderRadius: 3, boxShadow: 3 }}>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Box
                      sx={{
                        width: 48,
                        height: 48,
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        bgcolor: stat.color,
                        color: 'white',
                        mr: 2,
                        boxShadow: 2
                      }}
                    >
                      {stat.icon}
                    </Box>
                    <Box>
                      <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
                        {stat.value}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {stat.title}
                      </Typography>
                    </Box>
                  </Box>
                  <Typography variant="body2" color="success.main" sx={{ fontWeight: 500 }}>
                    {stat.change}
                  </Typography>
                </CardContent>
              </Card>
            </motion.div>
          </Grid>
        ))}
      </Grid>

      {/* Quick Actions */}
      <Box sx={{ mt: 4 }}>
        <Typography variant="h5" gutterBottom>
          Quick Actions
        </Typography>
        <Grid container spacing={2}>
          {quickActions.map((action, index) => (
            <Grid item xs={12} sm={6} md={3} key={index}>
              <Button
                variant={action.variant}
                fullWidth
                sx={{ py: 1.5 }}
                startIcon={action.icon}
                onClick={() => handleQuickActionClick(action)}
              >
                {action.title}
              </Button>
            </Grid>
          ))}
        </Grid>
      </Box>
    </Container>
  );
};

export default Dashboard;

