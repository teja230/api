import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Typography, Box, Button, Grid, Paper, Card, CardContent } from '@mui/material';
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
      background: 'linear-gradient(135deg, #f8fafc 0%, #e3e8ee 100%)', // even lighter background
      color: '#222',
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
              background: 'linear-gradient(45deg, #1976d2 30%, #4285F4 90%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              textAlign: 'center'
            }}>
              Onboarding App
            </Typography>
          </Box>
          <Typography variant="h5" sx={{
            mb: 4,
            color: '#222',
            maxWidth: '800px',
            textAlign: 'center',
            fontWeight: 400
          }}>
            Elevate team onboarding with seamless automation and integrations
          </Typography>
        </Box>

        {/* Features Section */}
        <Box sx={{ py: { xs: 4, md: 6 } }}>
          <Typography variant="h4" component="h2" sx={{ 
            mb: 4, 
            textAlign: 'center',
            fontWeight: 'bold',
            color: '#222'
          }}>
            Key Features
          </Typography>
          <Grid container spacing={3} justifyContent="center">
            {features.map((feature, index) => (
              <Grid item xs={12} sm={6} md={4} key={index}>
                <Paper
                  elevation={3}
                  sx={{
                    p: 4,
                    height: '100%',
                    background: '#fff',
                    borderRadius: '16px',
                    border: '1px solid #e0e0e0',
                    color: '#222',
                    transition: 'transform 0.3s, box-shadow 0.3s',
                    '&:hover': {
                      transform: 'translateY(-8px)',
                      boxShadow: 6
                    }
                  }}
                >
                  <Box sx={{ 
                    color: '#1976d2',
                    mb: 2,
                    display: 'flex',
                    justifyContent: 'center'
                  }}>
                    {feature.icon}
                  </Box>
                  <Typography variant="h5" component="h3" sx={{ 
                    mb: 2,
                    textAlign: 'center',
                    fontWeight: 'bold',
                    color: '#222'
                  }}>
                    {feature.title}
                  </Typography>
                  <Typography variant="body1" sx={{ 
                    color: '#444',
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
          <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold', color: '#222' }}>
            Ready to transform your onboarding process?
          </Typography>
          <Button
            variant="outlined"
            size="large"
            onClick={() => navigate('/login')}
            sx={{
              color: '#1976d2',
              borderColor: '#1976d2',
              '&:hover': {
                borderColor: '#4285F4',
                color: '#4285F4',
                bgcolor: '#e3e8ee',
              },
              px: 4,
              py: 1.5,
              borderRadius: '30px',
              fontSize: '1.1rem',
              boxShadow: 1
            }}
          >
            Start Free Trial <FaArrowRight style={{ marginLeft: '8px' }} />
          </Button>

          {/* Pricing Model Section */}
          <Box sx={{ mt: 5 }}>
            <Grid container spacing={3} justifyContent="center" alignItems="stretch">
              {[
                {
                  title: 'Free',
                  price: '$0',
                  features: [
                    'Unified onboarding for Slack, Jira, GitHub, Google, and more',
                    'Coordinate employee onboarding with ease',
                    'Access to our helpful community',
                    'Up to 5 users included',
                  ],
                  color: '#1976d2',
                },
                {
                  title: 'Pro',
                  price: '$19',
                  features: [
                    'Everything in Free',
                    'Automate multi-step onboarding tasks',
                    'Escalate tasks to humans when needed',
                    'Unlimited users & integrations',
                    'Priority email support',
                    'Procurement & vendor approval workflows',
                  ],
                  color: '#4A154B',
                },
                {
                  title: 'Enterprise',
                  price: 'Contact Us',
                  features: [
                    'All Pro features',
                    'Custom onboarding solutions',
                    'Dedicated account manager',
                    '24/7 premium support',
                    'Advanced security & compliance',
                  ],
                  color: '#D32F2F',
                }
              ].map((plan, index) => (
                <Grid item xs={12} sm={6} md={4} key={index} sx={{ display: 'flex' }}>
                  <Card
                    sx={{
                      borderRadius: '16px',
                      overflow: 'hidden',
                      boxShadow: 3,
                      border: `1px solid ${plan.color}`,
                      transition: 'transform 0.3s',
                      display: 'flex',
                      flexDirection: 'column',
                      flex: 1,
                      minHeight: 480,
                      height: '100%',
                      '&:hover': {
                        transform: 'translateY(-4px)',
                        boxShadow: 6
                      }
                    }}
                  >
                    <CardContent sx={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      textAlign: 'center',
                      p: 4,
                      bgcolor: '#fff',
                      flex: 1
                    }}>
                      <Typography variant="h5" component="div" sx={{
                        mb: 2,
                        fontWeight: 'bold',
                        color: plan.color
                      }}>
                        {plan.title}
                      </Typography>
                      <Typography variant="h6" sx={{
                        mb: 3,
                        fontWeight: 'medium',
                        color: '#333'
                      }}>
                        {plan.price}
                      </Typography>
                      <Box sx={{ mb: 2, width: '100%', flex: 1 }}>
                        <ul style={{ paddingLeft: 20, margin: 0 }}>
                          {plan.features.map((feature, idx) => (
                            <li key={idx} style={{ marginBottom: 8, fontSize: 16, color: '#333', lineHeight: 1.6 }}>{feature}</li>
                          ))}
                        </ul>
                      </Box>
                      <Button
                        variant="contained"
                        size="large"
                        onClick={() => navigate('/signup')}
                        sx={{
                          bgcolor: plan.color,
                          color: '#fff',
                          borderRadius: '30px',
                          px: 4,
                          py: 2,
                          fontSize: '1rem',
                          boxShadow: 2,
                          mt: 'auto',
                          '&:hover': {
                            bgcolor: `${plan.color}Dark`,
                            boxShadow: 4
                          }
                        }}
                      >
                        {plan.title === 'Free' ? 'Get Started Free' : 'Choose Plan'}
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </Box>
        </Box>
      </Container>
    </Box>
  );
};

export default LandingPage;
