import React from 'react';
import { AppBar, Toolbar, Typography, Button, IconButton, Box } from '@mui/material';
import { FaSignOutAlt, FaHome, FaUsers, FaPlug, FaHeartbeat } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';

const Header = ({ onLogout }) => {
  const navigate = useNavigate();

  const handleNavigation = (path) => {
    navigate(path);
  };

  return (
    <AppBar position="static" color="default" elevation={1}>
      <Toolbar>
        <Typography 
          variant="h6" 
          color="primary" 
          sx={{ 
            flexGrow: 1,
            cursor: 'pointer',
            '&:hover': {
              opacity: 0.8
            }
          }}
          onClick={() => handleNavigation('/')}
        >
          Onboarding App
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            color="inherit"
            startIcon={<FaHome />}
            onClick={() => handleNavigation('/dashboard')}
          >
            Dashboard
          </Button>
          <Button
            color="inherit"
            startIcon={<FaUsers />}
            onClick={() => handleNavigation('/teams')}
          >
            Teams
          </Button>
          <Button
            color="inherit"
            startIcon={<FaPlug />}
            onClick={() => handleNavigation('/integrations')}
          >
            Integrations
          </Button>
          <Button
            color="inherit"
            startIcon={<FaHeartbeat />}
            onClick={() => handleNavigation('/health')}
          >
            Health
          </Button>
          <IconButton color="inherit" onClick={onLogout}>
            <FaSignOutAlt />
          </IconButton>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header; 