import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { API_BASE_URL } from './services/api';
import Dashboard from './components/Dashboard';
import LandingPage from './components/LandingPage';
import Login from './components/Login';
import TeamsPage from './components/TeamsPage';
import IntegrationsPage from './components/IntegrationsPage';
import HealthPage from './components/HealthPage';
import Header from './components/Header';
import ViewProgress from './components/ViewProgress';
import ViewReports from './components/ViewReports';
import NewTeamPage from './components/NewTeamPage';

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

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  const checkAuthStatus = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/sso/user`, {
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
      await fetch(`${API_BASE_URL}/sso/logout`, {
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

  const PublicRoute = ({ children }) => {
    if (loading) {
      return <div>Loading...</div>;
    }
    return isAuthenticated ? <Navigate to="/dashboard" replace /> : children;
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Routes>
          {/* Public routes */}
          <Route 
            path="/" 
            element={
              <PublicRoute>
                <LandingPage />
              </PublicRoute>
            } 
          />
          <Route 
            path="/login" 
            element={
              <PublicRoute>
                <Login onLogin={handleLogin} />
              </PublicRoute>
            } 
          />

          {/* Protected routes */}
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
                <ViewProgress />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reports"
            element={
              <ProtectedRoute>
                <ViewReports />
              </ProtectedRoute>
            }
          />

          {/* Catch all route */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;

