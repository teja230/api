import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Button, Container, Typography, Box, CircularProgress, Alert, Paper } from '@mui/material';
import { FaBuilding } from 'react-icons/fa';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { API_BASE_URL } from '../services/api';

const Login = ({ onLogin }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  // Helper to get Basic Auth header for test credentials
  const getAuthHeader = () => {
    const creds = btoa('testuser:testpass');
    return { 'Authorization': `Basic ${creds}` };
  };

  // Check for error parameters in URL and authentication status
  useEffect(() => {
    const errorParam = searchParams.get('error');
    if (errorParam) {
      setError(`Authentication failed: ${errorParam}`);
    } else {
      // Check if we're authenticated after SSO callback
      const checkAuth = async () => {
        try {
          const response = await fetch(`${API_BASE_URL}/sso/user`, {
            credentials: 'include',
            headers: {
              'Accept': 'application/json',
              ...getAuthHeader()
            }
          });
          
          if (response.ok) {
            onLogin();
            navigate('/dashboard');
          }
        } catch (error) {
          console.error('Failed to check auth status:', error);
        }
      };
      
      checkAuth();
    }
  }, [searchParams, onLogin, navigate]);

  const handleSSO = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`${API_BASE_URL}/sso/initiate`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Accept': 'application/json',
          ...getAuthHeader()
        }
      });
      
      if (!response.ok) {
        throw new Error(`SSO initiation failed: ${response.status} ${response.statusText}`);
      }
      
      const data = await response.json();
      if (data.url) {
        window.location.href = data.url;
      } else {
        throw new Error('No SSO URL received');
      }
    } catch (err) {
      console.error('SSO error:', err);
      setError(err.message || 'Failed to initiate SSO');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm" sx={{ py: 8 }}>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <Paper
          elevation={3}
          sx={{
            p: 4,
            borderRadius: 2,
            bgcolor: 'background.paper',
          }}
        >
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
            }}
          >
            <Typography variant="h4" component="h1" gutterBottom>
              Welcome Back
            </Typography>
            <Typography variant="subtitle1" color="text.secondary" sx={{ mb: 4, textAlign: 'center' }}>
              Sign in with your company account to access the onboarding dashboard
            </Typography>

            {error && (
              <Alert severity="error" sx={{ mb: 4, width: '100%' }}>
                {error}
              </Alert>
            )}

            <Box sx={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Button
                variant="contained"
                size="large"
                startIcon={<FaBuilding />}
                onClick={handleSSO}
                disabled={loading}
                sx={{ 
                  py: 1.5,
                  backgroundColor: '#1976d2',
                  '&:hover': {
                    backgroundColor: '#1565c0'
                  },
                  boxShadow: 2
                }}
              >
                {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign in with Company SSO'}
              </Button>
            </Box>

            <Typography variant="body2" color="text.secondary" sx={{ mt: 4, textAlign: 'center' }}>
              By continuing, you agree to our Terms of Service and Privacy Policy
            </Typography>
          </Box>
        </Paper>
      </motion.div>
    </Container>
  );
};

export default Login;
