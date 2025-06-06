import React, { useState, useEffect } from 'react';
import {
  Card, CardContent, Typography, Box, Button, List, ListItem, ListItemText,
  ListItemIcon, Chip, IconButton, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, Alert, Tooltip, Grid, FormControl, InputLabel,
  Select, MenuItem, LinearProgress
} from '@mui/material';
import {
  Assignment as JiraIcon,
  Add as AddIcon,
  Settings as SettingsIcon,
  Refresh as RefreshIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon
} from '@mui/icons-material';

const JiraIntegration = ({ enterpriseConfig }) => {
  const [status, setStatus] = useState('disconnected');
  const [projects, setProjects] = useState([]);
  const [error, setError] = useState(null);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [createIssueOpen, setCreateIssueOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [settings, setSettings] = useState({
    defaultProject: '',
    issueType: 'Task',
    priority: 'Medium',
    autoAssign: true,
    templateId: ''
  });
  const [newIssue, setNewIssue] = useState({
    summary: '',
    description: '',
    project: '',
    type: 'Task',
    priority: 'Medium',
    assignee: ''
  });

  useEffect(() => {
    checkStatus();
    if (status === 'connected') {
      fetchProjects();
    }
  }, [status]);

  const checkStatus = async () => {
    try {
      const response = await fetch(`/api/jira/status?enterpriseId=${enterpriseConfig?.enterpriseId}`);
      const data = await response.json();
      setStatus(data.connected ? 'connected' : 'disconnected');
    } catch (err) {
      setError('Failed to check JIRA connection status');
    }
  };

  const fetchProjects = async () => {
    try {
      const response = await fetch(`/api/jira/projects?enterpriseId=${enterpriseConfig?.enterpriseId}`);
      const data = await response.json();
      setProjects(data.projects);
    } catch (err) {
      setError('Failed to fetch JIRA projects');
    }
  };

  const handleConnect = () => {
    window.location.href = `/api/jira/oauth/url?enterpriseId=${enterpriseConfig?.enterpriseId}`;
  };

  const handleDisconnect = async () => {
    try {
      await fetch(`/api/jira/disconnect?enterpriseId=${enterpriseConfig?.enterpriseId}`, {
        method: 'POST'
      });
      setStatus('disconnected');
      setProjects([]);
    } catch (err) {
      setError('Failed to disconnect JIRA');
    }
  };

  const handleSettingsSave = async () => {
    try {
      await fetch(`/api/jira/settings?enterpriseId=${enterpriseConfig?.enterpriseId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(settings)
      });
      setSettingsOpen(false);
    } catch (err) {
      setError('Failed to save JIRA settings');
    }
  };

  const handleCreateIssue = async () => {
    try {
      setLoading(true);
      await fetch(`/api/jira/issues?enterpriseId=${enterpriseConfig?.enterpriseId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(newIssue)
      });
      setCreateIssueOpen(false);
      setNewIssue({
        summary: '',
        description: '',
        project: '',
        type: 'Task',
        priority: 'Medium',
        assignee: ''
      });
    } catch (err) {
      setError('Failed to create JIRA issue');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <JiraIcon sx={{ color: '#0052CC', fontSize: 40, mr: 2 }} />
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h6" component="div">
              JIRA Integration
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Manage projects and track onboarding tasks
            </Typography>
          </Box>
          <Box>
            {status === 'connected' ? (
              <>
                <Tooltip title="Settings">
                  <IconButton onClick={() => setSettingsOpen(true)}>
                    <SettingsIcon />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Refresh">
                  <IconButton onClick={fetchProjects}>
                    <RefreshIcon />
                  </IconButton>
                </Tooltip>
                <Button
                  variant="outlined"
                  color="error"
                  onClick={handleDisconnect}
                  sx={{ ml: 1 }}
                >
                  Disconnect
                </Button>
              </>
            ) : (
              <Button
                variant="contained"
                color="primary"
                onClick={handleConnect}
                startIcon={<JiraIcon />}
              >
                Connect JIRA
              </Button>
            )}
          </Box>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {status === 'connected' && (
          <>
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle1" gutterBottom>
                Connected Projects
              </Typography>
              <List>
                {projects.map((project) => (
                  <ListItem key={project.id}>
                    <ListItemIcon>
                      <JiraIcon />
                    </ListItemIcon>
                    <ListItemText
                      primary={project.name}
                      secondary={`${project.issueCount} issues`}
                    />
                    <Chip
                      label={project.type}
                      size="small"
                      color="primary"
                    />
                  </ListItem>
                ))}
              </List>
            </Box>

            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button
                variant="outlined"
                startIcon={<AddIcon />}
                onClick={() => setCreateIssueOpen(true)}
              >
                Create Issue
              </Button>
              <Button
                variant="outlined"
                onClick={() => {/* Handle view dashboard */}}
              >
                View Dashboard
              </Button>
            </Box>
          </>
        )}
      </CardContent>

      <Dialog open={settingsOpen} onClose={() => setSettingsOpen(false)}>
        <DialogTitle>JIRA Integration Settings</DialogTitle>
        <DialogContent>
          <FormControl fullWidth margin="normal">
            <InputLabel>Default Project</InputLabel>
            <Select
              value={settings.defaultProject}
              onChange={(e) => setSettings({ ...settings, defaultProject: e.target.value })}
            >
              {projects.map((project) => (
                <MenuItem key={project.id} value={project.id}>
                  {project.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl fullWidth margin="normal">
            <InputLabel>Default Issue Type</InputLabel>
            <Select
              value={settings.issueType}
              onChange={(e) => setSettings({ ...settings, issueType: e.target.value })}
            >
              <MenuItem value="Task">Task</MenuItem>
              <MenuItem value="Story">Story</MenuItem>
              <MenuItem value="Bug">Bug</MenuItem>
            </Select>
          </FormControl>
          <FormControl fullWidth margin="normal">
            <InputLabel>Default Priority</InputLabel>
            <Select
              value={settings.priority}
              onChange={(e) => setSettings({ ...settings, priority: e.target.value })}
            >
              <MenuItem value="High">High</MenuItem>
              <MenuItem value="Medium">Medium</MenuItem>
              <MenuItem value="Low">Low</MenuItem>
            </Select>
          </FormControl>
          <Box sx={{ mt: 2 }}>
            <Typography variant="subtitle2" gutterBottom>
              Automation Settings
            </Typography>
            <Button
              variant={settings.autoAssign ? 'contained' : 'outlined'}
              onClick={() => setSettings({ ...settings, autoAssign: !settings.autoAssign })}
            >
              Auto Assign
            </Button>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSettingsOpen(false)}>Cancel</Button>
          <Button onClick={handleSettingsSave} variant="contained">
            Save Settings
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={createIssueOpen} onClose={() => setCreateIssueOpen(false)}>
        <DialogTitle>Create New Issue</DialogTitle>
        <DialogContent>
          {loading && <LinearProgress sx={{ mb: 2 }} />}
          <TextField
            fullWidth
            label="Summary"
            value={newIssue.summary}
            onChange={(e) => setNewIssue({ ...newIssue, summary: e.target.value })}
            margin="normal"
          />
          <TextField
            fullWidth
            label="Description"
            value={newIssue.description}
            onChange={(e) => setNewIssue({ ...newIssue, description: e.target.value })}
            margin="normal"
            multiline
            rows={4}
          />
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth margin="normal">
                <InputLabel>Project</InputLabel>
                <Select
                  value={newIssue.project}
                  onChange={(e) => setNewIssue({ ...newIssue, project: e.target.value })}
                >
                  {projects.map((project) => (
                    <MenuItem key={project.id} value={project.id}>
                      {project.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth margin="normal">
                <InputLabel>Type</InputLabel>
                <Select
                  value={newIssue.type}
                  onChange={(e) => setNewIssue({ ...newIssue, type: e.target.value })}
                >
                  <MenuItem value="Task">Task</MenuItem>
                  <MenuItem value="Story">Story</MenuItem>
                  <MenuItem value="Bug">Bug</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateIssueOpen(false)}>Cancel</Button>
          <Button onClick={handleCreateIssue} variant="contained" disabled={loading}>
            Create Issue
          </Button>
        </DialogActions>
      </Dialog>
    </Card>
  );
};

export default JiraIntegration; 