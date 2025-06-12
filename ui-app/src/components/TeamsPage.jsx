import { Container, Typography, Grid, Card, CardContent, Button, Box, Chip, Paper } from '@mui/material';
import { FaUsers, FaPlus } from 'react-icons/fa';
import { useNavigate, useLocation } from 'react-router-dom';

const TeamsPage = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const teams = [
    {
      name: 'Engineering Team',
      memberCount: 12,
      status: 'active',
      integrations: ['slack', 'github', 'jira']
    },
    {
      name: 'Design Team',
      memberCount: 8,
      status: 'active',
      integrations: ['figma', 'slack']
    },
    {
      name: 'Marketing Team',
      memberCount: 10,
      status: 'inactive',
      integrations: ['mailchimp', 'hubspot']
    }
  ];

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Teams
      </Typography>
      <Button
        variant="contained"
        startIcon={<FaPlus />}
        onClick={() => {/* TODO: Implement create team */}}
        sx={{ mb: 3 }}
      >
        Create Team
      </Button>
      <Grid container spacing={4}>
        {teams.map((team) => (
          <Grid item xs={12} sm={6} md={4} key={team.name}>
            <Paper elevation={3} sx={{ borderRadius: 4, overflow: 'hidden' }}>
              <Card sx={{ borderRadius: 4, boxShadow: 'none' }}>
                <CardContent sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                    <Box
                      sx={{
                        width: 48,
                        height: 48,
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        bgcolor: '#1976d2',
                        color: 'white',
                        mr: 2,
                        boxShadow: 2
                      }}
                    >
                      <FaUsers size={28} style={{ display: 'block' }} />
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
