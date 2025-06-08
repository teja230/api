import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import Dashboard from './components/Dashboard';
import LandingPage from './components/LandingPage';
import Login from './components/Login';
import TeamsPage from './components/TeamsPage';
import IntegrationsPage from './components/IntegrationsPage';
import HealthPage from './components/HealthPage';
import Header from './components/Header';
import { Container, Typography } from '@mui/material';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

// Placeholder components for new routes
const NewTeamPage = () => (
  <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
    <Typography variant="h4">Add New Team</Typography>
    {/* Add team form will go here */}
  </Container>
);

const TeamProgressPage = () => (
  <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
    <Typography variant="h4">Team Progress</Typography>
    {/* Team progress view will go here */}
  </Container>
);

const ReportsPage = () => (
  <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
    <Typography variant="h4">Reports</Typography>
    {/* Reports view will go here */}
  </Container>
);

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  const checkAuthStatus = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/sso/user', {
        credentials: 'include',
        headers: {
          'Accept': 'application/json'
        }
      });
      
      if (response.ok) {
        setIsAuthenticated(true);
      } else {
        setIsAuthenticated(false);
      }
    } catch (error) {
      console.error('Failed to check auth status:', error);
      setIsAuthenticated(false);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const handleLogin = () => {
    setIsAuthenticated(true);
  };

  const handleLogout = async () => {
    try {
      await fetch('http://localhost:8080/api/sso/logout', {
        method: 'POST',
        credentials: 'include'
      });
    } catch (error) {
      console.error('Failed to logout:', error);
    }
    setIsAuthenticated(false);
  };

  const ProtectedRoute = ({ children }) => {
    if (loading) {
      return <div>Loading...</div>;
    }
    return isAuthenticated ? (
      <>
        <Header onLogout={handleLogout} />
        {children}
      </>
    ) : (
      <Navigate to="/login" replace />
    );
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route 
            path="/login" 
            element={
              isAuthenticated ? 
                <Navigate to="/dashboard" /> : 
                <Login onLogin={handleLogin} />
            } 
          />
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute>
                <Dashboard onLogout={handleLogout} />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/teams" 
            element={
              <ProtectedRoute>
                <TeamsPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/integrations" 
            element={
              <ProtectedRoute>
                <IntegrationsPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/integrations/:type" 
            element={
              <ProtectedRoute>
                <IntegrationsPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/health" 
            element={
              <ProtectedRoute>
                <HealthPage />
              </ProtectedRoute>
            } 
          />
          <Route
            path="/teams/new"
            element={
              <ProtectedRoute>
                <NewTeamPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/teams/progress"
            element={
              <ProtectedRoute>
                <TeamProgressPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reports"
            element={
              <ProtectedRoute>
                <ReportsPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;

