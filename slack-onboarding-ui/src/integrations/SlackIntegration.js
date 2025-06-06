import React, { useState, useEffect } from 'react';
import {
  Card, CardContent, Typography, Box, Button, List, ListItem, ListItemText,
  ListItemIcon, Chip, IconButton, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, CircularProgress, Alert, Tooltip
} from '@mui/material';
import {
  InsertComment as SlackIcon,
  CheckCircle, Error as ErrorIcon, Add as AddIcon,
  Settings as SettingsIcon, Refresh as RefreshIcon
} from '@mui/icons-material';

const SlackIntegration = ({ enterpriseConfig }) => {
  const [status, setStatus] = useState('disconnected');
  const [channels, setChannels] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [settings, setSettings] = useState({
    defaultChannel: '',
    welcomeMessage: '',
    autoInvite: true,
    notifyOnJoin: true
  });

  useEffect(() => {
    checkStatus();
    if (status === 'connected') {
      fetchChannels();
    }
  }, [status]);

  const checkStatus = async () => {
    try {
      const response = await fetch(`/api/slack/status?enterpriseId=${enterpriseConfig?.enterpriseId}`);
      const data = await response.json();
      setStatus(data.connected ? 'connected' : 'disconnected');
    } catch (err) {
      setError('Failed to check Slack connection status');
    }
  };

  const fetchChannels = async () => {
    try {
      const response = await fetch(`/api/slack/channels?enterpriseId=${enterpriseConfig?.enterpriseId}`);
      const data = await response.json();
      setChannels(data.channels);
    } catch (err) {
      setError('Failed to fetch Slack channels');
    }
  };

  const handleConnect = () => {
    window.location.href = `/api/slack/oauth/url?enterpriseId=${enterpriseConfig?.enterpriseId}`;
  };

  const handleDisconnect = async () => {
    try {
      await fetch(`/api/slack/disconnect?enterpriseId=${enterpriseConfig?.enterpriseId}`, {
        method: 'POST'
      });
      setStatus('disconnected');
      setChannels([]);
    } catch (err) {
      setError('Failed to disconnect Slack');
    }
  };

  const handleSettingsSave = async () => {
    try {
      await fetch(`/api/slack/settings?enterpriseId=${enterpriseConfig?.enterpriseId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(settings)
      });
      setSettingsOpen(false);
    } catch (err) {
      setError('Failed to save Slack settings');
    }
  };

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <SlackIcon sx={{ color: '#4A154B', fontSize: 40, mr: 2 }} />
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h6" component="div">
              Slack Integration
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Manage team communication and notifications
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
                  <IconButton onClick={fetchChannels}>
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
                startIcon={<SlackIcon />}
              >
                Connect Slack
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
                Connected Channels
              </Typography>
              <List>
                {channels.map((channel) => (
                  <ListItem key={channel.id}>
                    <ListItemIcon>
                      <SlackIcon />
                    </ListItemIcon>
                    <ListItemText
                      primary={channel.name}
                      secondary={`${channel.memberCount} members`}
                    />
                    <Chip
                      label={channel.isPrivate ? 'Private' : 'Public'}
                      size="small"
                      color={channel.isPrivate ? 'default' : 'primary'}
                    />
                  </ListItem>
                ))}
              </List>
            </Box>

            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button
                variant="outlined"
                startIcon={<AddIcon />}
                onClick={() => {/* Handle create channel */}}
              >
                Create Channel
              </Button>
              <Button
                variant="outlined"
                onClick={() => {/* Handle invite members */}}
              >
                Invite Members
              </Button>
            </Box>
          </>
        )}
      </CardContent>

      <Dialog open={settingsOpen} onClose={() => setSettingsOpen(false)}>
        <DialogTitle>Slack Integration Settings</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Default Channel"
            value={settings.defaultChannel}
            onChange={(e) => setSettings({ ...settings, defaultChannel: e.target.value })}
            margin="normal"
          />
          <TextField
            fullWidth
            label="Welcome Message"
            value={settings.welcomeMessage}
            onChange={(e) => setSettings({ ...settings, welcomeMessage: e.target.value })}
            margin="normal"
            multiline
            rows={4}
          />
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
              variant={settings.notifyOnJoin ? 'contained' : 'outlined'}
              onClick={() => setSettings({ ...settings, notifyOnJoin: !settings.notifyOnJoin })}
            >
              Notify on Join
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
    </Card>
  );
};

export default SlackIntegration; 