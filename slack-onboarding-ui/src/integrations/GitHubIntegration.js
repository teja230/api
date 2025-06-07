import React, { useState, useEffect } from 'react';
import {
  Card, CardContent, Typography, Box, Button, List, ListItem, ListItemText,
  ListItemIcon, Chip, IconButton, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, Alert, Tooltip, Grid, FormControl, InputLabel,
  Select, MenuItem, LinearProgress, Tabs, Tab, Avatar
} from '@mui/material';
import {
  GitHub as GitHubIcon,
  Add as AddIcon,
  Settings as SettingsIcon,
  Refresh as RefreshIcon,
  Group as GroupIcon,
  Storage as RepoIcon,
  Security as SecurityIcon,
  Code as CodeIcon
} from '@mui/icons-material';

const GitHubIntegration = ({ enterpriseConfig }) => {
  const [status, setStatus] = useState('disconnected');
  const [repos, setRepos] = useState([]);
  const [teams, setTeams] = useState([]);
  const [error, setError] = useState(null);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [createRepoOpen, setCreateRepoOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState(0);
  const [settings, setSettings] = useState({
    defaultOrg: '',
    defaultBranch: 'main',
    autoInvite: true,
    requireReview: true,
    templateRepo: ''
  });
  const [newRepo, setNewRepo] = useState({
    name: '',
    description: '',
    visibility: 'private',
    template: '',
    autoInit: true
  });

  useEffect(() => {
    checkStatus();
    if (status === 'connected') {
      fetchData();
    }
  }, [status]);

  const checkStatus = async () => {
    try {
      const response = await fetch(`/api/github/status?enterpriseId=${enterpriseConfig?.enterpriseId}`);
      const data = await response.json();
      setStatus(data.connected ? 'connected' : 'disconnected');
    } catch (err) {
      setError('Failed to check GitHub connection status');
    }
  };

  const fetchData = async () => {
    try {
      const [reposResponse, teamsResponse] = await Promise.all([
        fetch(`/api/github/repos?enterpriseId=${enterpriseConfig?.enterpriseId}`),
        fetch(`/api/github/teams?enterpriseId=${enterpriseConfig?.enterpriseId}`)
      ]);
      const reposData = await reposResponse.json();
      const teamsData = await teamsResponse.json();
      setRepos(reposData.repos);
      setTeams(teamsData.teams);
    } catch (err) {
      setError('Failed to fetch GitHub data');
    }
  };

  const handleConnect = () => {
    window.location.href = `/api/github/oauth/url?enterpriseId=${enterpriseConfig?.enterpriseId}`;
  };

  const handleDisconnect = async () => {
    try {
      await fetch(`/api/github/disconnect?enterpriseId=${enterpriseConfig?.enterpriseId}`, {
        method: 'POST'
      });
      setStatus('disconnected');
      setRepos([]);
      setTeams([]);
    } catch (err) {
      setError('Failed to disconnect GitHub');
    }
  };

  const handleSettingsSave = async () => {
    try {
      await fetch(`/api/github/settings?enterpriseId=${enterpriseConfig?.enterpriseId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(settings)
      });
      setSettingsOpen(false);
    } catch (err) {
      setError('Failed to save GitHub settings');
    }
  };

  const handleCreateRepo = async () => {
    try {
      setLoading(true);
      await fetch(`/api/github/repos?enterpriseId=${enterpriseConfig?.enterpriseId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(newRepo)
      });
      setCreateRepoOpen(false);
      setNewRepo({
        name: '',
        description: '',
        visibility: 'private',
        template: '',
        autoInit: true
      });
      fetchData();
    } catch (err) {
      setError('Failed to create repository');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <GitHubIcon sx={{ color: '#24292e', fontSize: 40, mr: 2 }} />
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h6" component="div">
              GitHub Integration
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Manage repositories and team access
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
                  <IconButton onClick={fetchData}>
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
                startIcon={<GitHubIcon />}
              >
                Connect GitHub
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
            <Tabs value={activeTab} onChange={(e, newValue) => setActiveTab(newValue)} sx={{ mb: 2 }}>
              <Tab icon={<RepoIcon />} label="Repositories" />
              <Tab icon={<GroupIcon />} label="Teams" />
              <Tab icon={<SecurityIcon />} label="Access" />
            </Tabs>

            {activeTab === 0 && (
              <>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle1" gutterBottom>
                    Repositories
                  </Typography>
                  <List>
                    {repos.map((repo) => (
                      <ListItem key={repo.id}>
                        <ListItemIcon>
                          <RepoIcon />
                        </ListItemIcon>
                        <ListItemText
                          primary={repo.name}
                          secondary={`${repo.stars} stars • ${repo.forks} forks`}
                        />
                        <Chip
                          label={repo.visibility}
                          size="small"
                          color={repo.visibility === 'public' ? 'primary' : 'default'}
                        />
                      </ListItem>
                    ))}
                  </List>
                </Box>

                <Box sx={{ display: 'flex', gap: 2 }}>
                  <Button
                    variant="outlined"
                    startIcon={<AddIcon />}
                    onClick={() => setCreateRepoOpen(true)}
                  >
                    Create Repository
                  </Button>
                  <Button
                    variant="outlined"
                    onClick={() => {/* Handle import repo */}}
                  >
                    Import Repository
                  </Button>
                </Box>
              </>
            )}

            {activeTab === 1 && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Teams
                </Typography>
                <List>
                  {teams.map((team) => (
                    <ListItem key={team.id}>
                      <ListItemIcon>
                        <GroupIcon />
                      </ListItemIcon>
                      <ListItemText
                        primary={team.name}
                        secondary={`${team.memberCount} members`}
                      />
                      <Chip
                        label={team.permission}
                        size="small"
                        color="primary"
                      />
                    </ListItem>
                  ))}
                </List>
                <Button
                  variant="outlined"
                  startIcon={<AddIcon />}
                  onClick={() => {/* Handle create team */}}
                  sx={{ mt: 2 }}
                >
                  Create Team
                </Button>
              </Box>
            )}

            {activeTab === 2 && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Access Management
                </Typography>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          Repository Access
                        </Typography>
                        <List>
                          {repos.slice(0, 3).map((repo) => (
                            <ListItem key={repo.id}>
                              <ListItemIcon>
                                <RepoIcon />
                              </ListItemIcon>
                              <ListItemText
                                primary={repo.name}
                                secondary={`${repo.accessCount} users`}
                              />
                            </ListItem>
                          ))}
                        </List>
                        <Button
                          variant="text"
                          onClick={() => {/* Handle manage access */}}
                        >
                          Manage Access
                        </Button>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          Team Permissions
                        </Typography>
                        <List>
                          {teams.slice(0, 3).map((team) => (
                            <ListItem key={team.id}>
                              <ListItemIcon>
                                <GroupIcon />
                              </ListItemIcon>
                              <ListItemText
                                primary={team.name}
                                secondary={team.permission}
                              />
                            </ListItem>
                          ))}
                        </List>
                        <Button
                          variant="text"
                          onClick={() => {/* Handle manage permissions */}}
                        >
                          Manage Permissions
                        </Button>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>
              </Box>
            )}
          </>
        )}
      </CardContent>

      <Dialog open={settingsOpen} onClose={() => setSettingsOpen(false)}>
        <DialogTitle>GitHub Integration Settings</DialogTitle>
        <DialogContent>
          <FormControl fullWidth margin="normal">
            <InputLabel>Default Organization</InputLabel>
            <Select
              value={settings.defaultOrg}
              onChange={(e) => setSettings({ ...settings, defaultOrg: e.target.value })}
            >
              {teams.map((team) => (
                <MenuItem key={team.id} value={team.id}>
                  {team.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl fullWidth margin="normal">
            <InputLabel>Default Branch</InputLabel>
            <Select
              value={settings.defaultBranch}
              onChange={(e) => setSettings({ ...settings, defaultBranch: e.target.value })}
            >
              <MenuItem value="main">main</MenuItem>
              <MenuItem value="master">master</MenuItem>
              <MenuItem value="develop">develop</MenuItem>
            </Select>
          </FormControl>
          <Box sx={{ mt: 2 }}>
            <Typography variant="subtitle2" gutterBottom>
              Automation Settings
            </Typography>
            <Button
              variant={settings.autoInvite ? 'contained' : 'outlined'}
              onClick={() => setSettings({ ...settings, autoInvite: !settings.autoInvite })}
              sx={{ mr: 1 }}
            >
              Auto Invite
            </Button>
            <Button
              variant={settings.requireReview ? 'contained' : 'outlined'}
              onClick={() => setSettings({ ...settings, requireReview: !settings.requireReview })}
            >
              Require Review
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

      <Dialog open={createRepoOpen} onClose={() => setCreateRepoOpen(false)}>
        <DialogTitle>Create New Repository</DialogTitle>
        <DialogContent>
          {loading && <LinearProgress sx={{ mb: 2 }} />}
          <TextField
            fullWidth
            label="Repository Name"
            value={newRepo.name}
            onChange={(e) => setNewRepo({ ...newRepo, name: e.target.value })}
            margin="normal"
          />
          <TextField
            fullWidth
            label="Description"
            value={newRepo.description}
            onChange={(e) => setNewRepo({ ...newRepo, description: e.target.value })}
            margin="normal"
            multiline
            rows={2}
          />
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth margin="normal">
                <InputLabel>Visibility</InputLabel>
                <Select
                  value={newRepo.visibility}
                  onChange={(e) => setNewRepo({ ...newRepo, visibility: e.target.value })}
                >
                  <MenuItem value="private">Private</MenuItem>
                  <MenuItem value="public">Public</MenuItem>
                  <MenuItem value="internal">Internal</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth margin="normal">
                <InputLabel>Template</InputLabel>
                <Select
                  value={newRepo.template}
                  onChange={(e) => setNewRepo({ ...newRepo, template: e.target.value })}
                >
                  <MenuItem value="">None</MenuItem>
                  {repos.map((repo) => (
                    <MenuItem key={repo.id} value={repo.id}>
                      {repo.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateRepoOpen(false)}>Cancel</Button>
          <Button onClick={handleCreateRepo} variant="contained" disabled={loading}>
            Create Repository
          </Button>
        </DialogActions>
      </Dialog>
    </Card>
  );
};

export default GitHubIntegration;
