import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Typography, Box, Button, Grid, Paper } from '@mui/material';
import { FaRocket, FaUsers, FaChartLine, FaClock, FaArrowRight } from 'react-icons/fa';

const LandingPage = () => {
  const navigate = useNavigate();

  const features = [
    {
      icon: <FaUsers size={40} />,
      title: 'Team Management',
      description: 'Easily onboard and manage your team members with automated workflows'
    },
    {
      icon: <FaChartLine size={40} />,
      title: 'Analytics & Insights',
      description: 'Track progress and get valuable insights into your team\'s performance'
    },
    {
      icon: <FaClock size={40} />,
      title: 'Time Saving',
      description: 'Automate repetitive tasks and save hours of manual work'
    }
  ];

  return (
    <Box sx={{ 
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #1a237e 0%, #0d47a1 100%)',
      color: 'white',
      display: 'flex',
      flexDirection: 'column'
    }}>
      <Container maxWidth="lg" sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        {/* Hero Section */}
        <Box sx={{ 
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          py: { xs: 6, md: 8 }
        }}>
          <Box sx={{ 
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            mb: 4
          }}>
            <FaRocket size={48} style={{ marginRight: '16px' }} />
            <Typography variant="h2" component="h1" sx={{ 
              fontWeight: 'bold',
              background: 'linear-gradient(45deg, #fff 30%, #e3f2fd 90%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              textAlign: 'center'
            }}>
              Onboarding App
            </Typography>
          </Box>
          <Typography variant="h5" sx={{ 
            mb: 4,
            color: 'rgba(255, 255, 255, 0.9)',
            maxWidth: '800px',
            textAlign: 'center'
          }}>
            Streamline your team onboarding process with our powerful automation platform
          </Typography>
          <Button
            variant="contained"
            size="large"
            onClick={() => navigate('/login')}
            sx={{
              bgcolor: 'white',
              color: '#1a237e',
              '&:hover': {
                bgcolor: 'rgba(255, 255, 255, 0.9)',
              },
              px: 4,
              py: 1.5,
              borderRadius: '30px',
              fontSize: '1.1rem'
            }}
          >
            Get Started <FaArrowRight style={{ marginLeft: '8px' }} />
          </Button>
        </Box>

        {/* Features Section */}
        <Box sx={{ py: { xs: 4, md: 6 } }}>
          <Grid container spacing={3} justifyContent="center">
            {features.map((feature, index) => (
              <Grid item xs={12} sm={6} md={4} key={index}>
                <Paper
                  elevation={0}
                  sx={{
                    p: 4,
                    height: '100%',
                    background: 'rgba(255, 255, 255, 0.1)',
                    backdropFilter: 'blur(10px)',
                    borderRadius: '16px',
                    border: '1px solid rgba(255, 255, 255, 0.2)',
                    transition: 'transform 0.3s ease-in-out',
                    '&:hover': {
                      transform: 'translateY(-8px)'
                    }
                  }}
                >
                  <Box sx={{ 
                    color: 'white',
                    mb: 2,
                    display: 'flex',
                    justifyContent: 'center'
                  }}>
                    {feature.icon}
                  </Box>
                  <Typography variant="h5" component="h3" sx={{ 
                    mb: 2,
                    textAlign: 'center',
                    fontWeight: 'bold'
                  }}>
                    {feature.title}
                  </Typography>
                  <Typography variant="body1" sx={{ 
                    color: 'rgba(255, 255, 255, 0.9)',
                    textAlign: 'center'
                  }}>
                    {feature.description}
                  </Typography>
                </Paper>
              </Grid>
            ))}
          </Grid>
        </Box>

        {/* CTA Section */}
        <Box sx={{ 
          py: { xs: 4, md: 6 },
          textAlign: 'center',
          mt: 'auto'
        }}>
          <Typography variant="h4" sx={{ mb: 3 }}>
            Ready to transform your onboarding process?
          </Typography>
          <Button
            variant="outlined"
            size="large"
            onClick={() => navigate('/login')}
            sx={{
              color: 'white',
              borderColor: 'white',
              '&:hover': {
                borderColor: 'white',
                bgcolor: 'rgba(255, 255, 255, 0.1)',
              },
              px: 4,
              py: 1.5,
              borderRadius: '30px',
              fontSize: '1.1rem'
            }}
          >
            Start Free Trial <FaArrowRight style={{ marginLeft: '8px' }} />
          </Button>
        </Box>
      </Container>
    </Box>
  );
};

export default LandingPage; 