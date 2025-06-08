import React, { useState } from 'react';
import { Container, Typography, Grid, Card, CardContent, Button, Box, Chip } from '@mui/material';
import { FaUsers, FaPlus } from 'react-icons/fa';

const TeamsPage = () => {
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
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" component="h1">
          Teams
        </Typography>
        <Button
          variant="contained"
          startIcon={<FaPlus />}
          onClick={() => {/* TODO: Implement new team creation */}}
        >
          New Team
        </Button>
      </Box>

      <Grid container spacing={3}>
        {teams.map((team) => (
          <Grid item xs={12} md={6} lg={4} key={team.id}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <FaUsers style={{ marginRight: 8, color: '#1976d2' }} />
                  <Typography variant="h6" component="div">
                    {team.name}
                  </Typography>
                </Box>
                
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Members: {team.memberCount}
                  </Typography>
                </Box>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    Integrations:
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {team.integrations.map((integration) => (
                      <Chip
                        key={integration}
                        label={integration}
                        size="small"
                        color="primary"
                        variant="outlined"
                      />
                    ))}
                  </Box>
                </Box>

                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() => {/* TODO: Implement view team */}}
                  >
                    View Team
                  </Button>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() => {/* TODO: Implement edit team */}}
                  >
                    Edit Team
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
};

export default TeamsPage; 