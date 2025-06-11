import React from 'react';
import { 
  Box, 
  Typography, 
  Paper, 
  LinearProgress, 
  List, 
  ListItem, 
  ListItemText, 
  Chip,
  Container,
  Grid
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PendingIcon from '@mui/icons-material/Pending';

const ViewProgress = () => {
  const progress = 75; // Sample progress percentage
  const completedTasks = [
    { id: 1, task: 'Account Setup', status: 'Completed' },
    { id: 2, task: 'Profile Configuration', status: 'Completed' },
    { id: 3, task: 'Document Verification', status: 'In Progress' },
  ];

  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Onboarding Progress
        </Typography>
        
        <Paper elevation={3} sx={{ p: 3, mb: 4 }}>
          <Typography variant="h6" gutterBottom>
            Overall Progress
          </Typography>
          <Box sx={{ width: '100%', mb: 1 }}>
            <LinearProgress 
              variant="determinate" 
              value={progress} 
              sx={{ 
                height: 10, 
                borderRadius: 5,
                backgroundColor: '#e0e0e0',
                '& .MuiLinearProgress-bar': {
                  backgroundColor: '#4CAF50',
                }
              }} 
            />
          </Box>
          <Typography variant="body1" color="text.secondary">
            {progress}% Complete
          </Typography>
        </Paper>

        <Paper elevation={3} sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Task Status
          </Typography>
          <List>
            {completedTasks.map(task => (
              <ListItem 
                key={task.id}
                sx={{
                  borderBottom: '1px solid #eee',
                  '&:last-child': {
                    borderBottom: 'none'
                  }
                }}
              >
                <ListItemText 
                  primary={task.task}
                  secondary={
                    <Chip
                      icon={task.status === 'Completed' ? <CheckCircleIcon /> : <PendingIcon />}
                      label={task.status}
                      color={task.status === 'Completed' ? 'success' : 'warning'}
                      size="small"
                      sx={{ mt: 1 }}
                    />
                  }
                />
              </ListItem>
            ))}
          </List>
        </Paper>
      </Box>
    </Container>
  );
};

export default ViewProgress; 