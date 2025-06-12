import React from 'react';
import { AppBar, Toolbar, Typography, Button, IconButton, Box } from '@mui/material';
import { FaSignOutAlt, FaHome, FaUsers, FaPlug, FaHeartbeat, FaRocket } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';

const Header = ({ onLogout }) => {
  const navigate = useNavigate();

  const handleNavigation = (path) => {
    navigate(path);
  };

  return (
    <AppBar position="static" color="default" elevation={1}>
      <Toolbar>
        {/* Logo and Title */}
        <Box sx={{ display: 'flex', alignItems: 'center', flexGrow: 1, cursor: 'pointer', '&:hover': { opacity: 0.9 } }} onClick={() => handleNavigation('/')}>
          {/* Upgraded mascot-style SVG icon for Onboarding Buddy */}
          <Box sx={{ mr: 1, width: 40, height: 40, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <svg width="38" height="38" viewBox="0 0 38 38" fill="none" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <radialGradient id="buddyGradient" cx="50%" cy="50%" r="50%">
                  <stop offset="0%" stopColor="#90caf9"/>
                  <stop offset="100%" stopColor="#1976d2"/>
                </radialGradient>
              </defs>
              <circle cx="19" cy="19" r="18" fill="url(#buddyGradient)" stroke="#1976d2" strokeWidth="2"/>
              <ellipse cx="19" cy="16" rx="8" ry="7" fill="#fff"/>
              <ellipse cx="19" cy="16" rx="5" ry="4.5" fill="#e3f2fd"/>
              <ellipse cx="15.5" cy="16" rx="1.3" ry="1.7" fill="#1976d2"/>
              <ellipse cx="22.5" cy="16" rx="1.3" ry="1.7" fill="#1976d2"/>
              <path d="M15 23c1.5 2 6.5 2 8 0" stroke="#1976d2" strokeWidth="1.5" strokeLinecap="round"/>
              <circle cx="12" cy="12" r="2" fill="#fff" stroke="#1976d2" strokeWidth="1"/>
              <circle cx="26" cy="12" r="2" fill="#fff" stroke="#1976d2" strokeWidth="1"/>
              <ellipse cx="12" cy="12" rx="0.5" ry="0.7" fill="#1976d2"/>
              <ellipse cx="26" cy="12" rx="0.5" ry="0.7" fill="#1976d2"/>
            </svg>
          </Box>
          <Typography
            variant="h5"
            sx={{
              fontWeight: 900,
              letterSpacing: 1.5,
              background: 'linear-gradient(90deg, #1976d2 30%, #90caf9 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              display: 'inline',
              pr: 1
            }}
          >
          Onboarding App
          </Typography>
        </Box>
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
            startIcon={
              <svg width="22" height="22" viewBox="0 0 38 38" fill="none" xmlns="http://www.w3.org/2000/svg" style={{ verticalAlign: 'middle' }}>
                <defs>
                  <radialGradient id="buddyMiniGradient" cx="50%" cy="50%" r="50%">
                    <stop offset="0%" stopColor="#90caf9"/>
                    <stop offset="100%" stopColor="#1976d2"/>
                  </radialGradient>
                </defs>
                <circle cx="19" cy="19" r="18" fill="url(#buddyMiniGradient)" stroke="#1976d2" strokeWidth="2"/>
                <ellipse cx="19" cy="16" rx="8" ry="7" fill="#fff"/>
                <ellipse cx="19" cy="16" rx="5" ry="4.5" fill="#e3f2fd"/>
                <ellipse cx="15.5" cy="16" rx="1.3" ry="1.7" fill="#1976d2"/>
                <ellipse cx="22.5" cy="16" rx="1.3" ry="1.7" fill="#1976d2"/>
                <path d="M15 23c1.5 2 6.5 2 8 0" stroke="#1976d2" strokeWidth="1.5" strokeLinecap="round"/>
              </svg>
            }
            onClick={() => handleNavigation('/onboarding-agent')}
            sx={{ fontWeight: 600 }}
          >
            Onboarding Buddy
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
