import React, { useState } from 'react';
import { Container, Typography, Grid, Card, CardContent, Button, Box, Chip, Paper } from '@mui/material';
import { FaUsers, FaPlus } from 'react-icons/fa';
import { useNavigate, useLocation } from 'react-router-dom';

const TeamsPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [teams] = useState([
    {
      id: 1,
      name: 'Engineering Team',
      memberCount: 12,
      status: 'active',
      integrations: ['slack', 'github', 'jira']
    },
    {
      id: 2,
      name: 'Product Team',
      memberCount: 8,
      status: 'active',
      integrations: ['slack', 'jira']
    },
    {
      id: 3,
      name: 'Design Team',
      memberCount: 6,
      status: 'active',
      integrations: ['slack', 'figma']
    }
  ]);

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            Teams
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Manage your teams and their integrations
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<FaPlus />}
          onClick={() => navigate('/teams/new', { state: { from: location.pathname } })}
          sx={{ boxShadow: 2, height: 'fit-content' }}
        >
          New Team
        </Button>
      </Box>

      <Grid container spacing={3}>
        {teams.map((team) => (
          <Grid item xs={12} md={6} lg={4} key={team.id}>
            <Paper elevation={3} sx={{ height: '100%' }}>
              <Card sx={{ height: '100%' }}>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                    <Box
                      sx={{
                        width: 48,
                        height: 48,
                        borderRadius: '12px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        bgcolor: 'primary.light',
                        color: 'primary.main',
                        mr: 2
                      }}
                    >
                      <FaUsers size={24} />
                    </Box>
                    <Typography variant="h6" component="div" sx={{ flex: 1 }}>
                      {team.name}
                    </Typography>
                  </Box>
                  
                  <Box sx={{ mb: 3 }}>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                      Members
                    </Typography>
                    <Typography variant="body1">
                      {team.memberCount} members
                    </Typography>
                  </Box>

                  <Box sx={{ mb: 3 }}>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                      Integrations
                    </Typography>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                      {team.integrations.map((integration) => (
                        <Chip
                          key={integration}
                          label={integration}
                          size="small"
                          color="primary"
                          variant="outlined"
                          sx={{ textTransform: 'capitalize' }}
                        />
                      ))}
                    </Box>
                  </Box>

                  <Box sx={{ display: 'flex', gap: 1, mt: 'auto' }}>
                    <Button
                      variant="outlined"
                      size="small"
                      onClick={() => {/* TODO: Implement view team */}}
                      sx={{ flex: 1 }}
                    >
                      View Team
                    </Button>
                    <Button
                      variant="outlined"
                      size="small"
                      onClick={() => {/* TODO: Implement edit team */}}
                      sx={{ flex: 1 }}
                    >
                      Edit Team
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
};

export default TeamsPage; 