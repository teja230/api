import React, { useState, useEffect } from 'react';
import {
  Card, CardContent, Typography, Box, Button, List, ListItem, ListItemText,
  ListItemIcon, Chip, IconButton, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, Alert, Tooltip, Grid, FormControl, InputLabel,
  Select, MenuItem, LinearProgress, Tabs, Tab, Paper
} from '@mui/material';
import {
  CalendarMonth as CalendarIcon,
  Add as AddIcon,
  Settings as SettingsIcon,
  Refresh as RefreshIcon,
  Event as EventIcon,
  Group as GroupIcon,
  AccessTime as TimeIcon
} from '@mui/icons-material';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider, DateTimePicker } from '@mui/x-date-pickers';

const GoogleCalendarIntegration = ({ enterpriseConfig }) => {
  const [status, setStatus] = useState('disconnected');
  const [calendars, setCalendars] = useState([]);
  const [events, setEvents] = useState([]);
  const [error, setError] = useState(null);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [createEventOpen, setCreateEventOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState(0);
  const [settings, setSettings] = useState({
    defaultCalendar: '',
    workingHours: {
      start: '09:00',
      end: '17:00'
    },
    timezone: 'UTC',
    autoAccept: true,
    notifyBefore: 30
  });
  const [newEvent, setNewEvent] = useState({
    summary: '',
    description: '',
    calendar: '',
    start: new Date(),
    end: new Date(new Date().setHours(new Date().getHours() + 1)),
    attendees: [],
    location: '',
    reminders: []
  });

  useEffect(() => {
    checkStatus();
    if (status === 'connected') {
      fetchData();
    }
  }, [status]);

  const checkStatus = async () => {
    try {
      const response = await fetch(`/api/google/status?enterpriseId=${enterpriseConfig?.enterpriseId}`);
      const data = await response.json();
      setStatus(data.connected ? 'connected' : 'disconnected');
    } catch (err) {
      setError('Failed to check Google Calendar connection status');
    }
  };

  const fetchData = async () => {
    try {
      const [calendarsResponse, eventsResponse] = await Promise.all([
        fetch(`/api/google/calendars?enterpriseId=${enterpriseConfig?.enterpriseId}`),
        fetch(`/api/google/events?enterpriseId=${enterpriseConfig?.enterpriseId}`)
      ]);
      const calendarsData = await calendarsResponse.json();
      const eventsData = await eventsResponse.json();
      setCalendars(calendarsData.calendars);
      setEvents(eventsData.events);
    } catch (err) {
      setError('Failed to fetch Google Calendar data');
    }
  };

  const handleConnect = () => {
    window.location.href = `/api/google/oauth/url?enterpriseId=${enterpriseConfig?.enterpriseId}`;
  };

  const handleDisconnect = async () => {
    try {
      await fetch(`/api/google/disconnect?enterpriseId=${enterpriseConfig?.enterpriseId}`, {
        method: 'POST'
      });
      setStatus('disconnected');
      setCalendars([]);
      setEvents([]);
    } catch (err) {
      setError('Failed to disconnect Google Calendar');
    }
  };

  const handleSettingsSave = async () => {
    try {
      await fetch(`/api/google/settings?enterpriseId=${enterpriseConfig?.enterpriseId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(settings)
      });
      setSettingsOpen(false);
    } catch (err) {
      setError('Failed to save Google Calendar settings');
    }
  };

  const handleCreateEvent = async () => {
    try {
      setLoading(true);
      await fetch(`/api/google/events?enterpriseId=${enterpriseConfig?.enterpriseId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(newEvent)
      });
      setCreateEventOpen(false);
      setNewEvent({
        summary: '',
        description: '',
        calendar: '',
        start: new Date(),
        end: new Date(new Date().setHours(new Date().getHours() + 1)),
        attendees: [],
        location: '',
        reminders: []
      });
      fetchData();
    } catch (err) {
      setError('Failed to create event');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <CalendarIcon sx={{ color: '#4285F4', fontSize: 40, mr: 2 }} />
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h6" component="div">
              Google Calendar Integration
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Manage schedules and meetings
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
                startIcon={<CalendarIcon />}
              >
                Connect Calendar
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
              <Tab icon={<EventIcon />} label="Events" />
              <Tab icon={<CalendarIcon />} label="Calendars" />
              <Tab icon={<TimeIcon />} label="Schedule" />
            </Tabs>

            {activeTab === 0 && (
              <>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle1" gutterBottom>
                    Upcoming Events
                  </Typography>
                  <List>
                    {events.map((event) => (
                      <ListItem key={event.id}>
                        <ListItemIcon>
                          <EventIcon />
                        </ListItemIcon>
                        <ListItemText
                          primary={event.summary}
                          secondary={`${new Date(event.start).toLocaleString()} - ${new Date(event.end).toLocaleString()}`}
                        />
                        <Chip
                          label={event.status}
                          size="small"
                          color={event.status === 'confirmed' ? 'success' : 'default'}
                        />
                      </ListItem>
                    ))}
                  </List>
                </Box>

                <Box sx={{ display: 'flex', gap: 2 }}>
                  <Button
                    variant="outlined"
                    startIcon={<AddIcon />}
                    onClick={() => setCreateEventOpen(true)}
                  >
                    Create Event
                  </Button>
                  <Button
                    variant="outlined"
                    onClick={() => {/* Handle import events */}}
                  >
                    Import Events
                  </Button>
                </Box>
              </>
            )}

            {activeTab === 1 && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Connected Calendars
                </Typography>
                <List>
                  {calendars.map((calendar) => (
                    <ListItem key={calendar.id}>
                      <ListItemIcon>
                        <CalendarIcon />
                      </ListItemIcon>
                      <ListItemText
                        primary={calendar.name}
                        secondary={`${calendar.eventCount} events`}
                      />
                      <Chip
                        label={calendar.access}
                        size="small"
                        color={calendar.access === 'owner' ? 'primary' : 'default'}
                      />
                    </ListItem>
                  ))}
                </List>
                <Button
                  variant="outlined"
                  startIcon={<AddIcon />}
                  onClick={() => {/* Handle add calendar */}}
                  sx={{ mt: 2 }}
                >
                  Add Calendar
                </Button>
              </Box>
            )}

            {activeTab === 2 && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Schedule Management
                </Typography>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Paper sx={{ p: 2 }}>
                      <Typography variant="h6" gutterBottom>
                        Working Hours
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                        <FormControl fullWidth>
                          <InputLabel>Start Time</InputLabel>
                          <Select
                            value={settings.workingHours.start}
                            onChange={(e) => setSettings({
                              ...settings,
                              workingHours: { ...settings.workingHours, start: e.target.value }
                            })}
                          >
                            {Array.from({ length: 24 }, (_, i) => (
                              <MenuItem key={i} value={`${i.toString().padStart(2, '0')}:00`}>
                                {`${i.toString().padStart(2, '0')}:00`}
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                        <FormControl fullWidth>
                          <InputLabel>End Time</InputLabel>
                          <Select
                            value={settings.workingHours.end}
                            onChange={(e) => setSettings({
                              ...settings,
                              workingHours: { ...settings.workingHours, end: e.target.value }
                            })}
                          >
                            {Array.from({ length: 24 }, (_, i) => (
                              <MenuItem key={i} value={`${i.toString().padStart(2, '0')}:00`}>
                                {`${i.toString().padStart(2, '0')}:00`}
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      </Box>
                      <FormControl fullWidth>
                        <InputLabel>Timezone</InputLabel>
                        <Select
                          value={settings.timezone}
                          onChange={(e) => setSettings({ ...settings, timezone: e.target.value })}
                        >
                          <MenuItem value="UTC">UTC</MenuItem>
                          <MenuItem value="America/New_York">Eastern Time</MenuItem>
                          <MenuItem value="America/Chicago">Central Time</MenuItem>
                          <MenuItem value="America/Denver">Mountain Time</MenuItem>
                          <MenuItem value="America/Los_Angeles">Pacific Time</MenuItem>
                        </Select>
                      </FormControl>
                    </Paper>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Paper sx={{ p: 2 }}>
                      <Typography variant="h6" gutterBottom>
                        Availability
                      </Typography>
                      <List>
                        {['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'].map((day) => (
                          <ListItem key={day}>
                            <ListItemText
                              primary={day}
                              secondary={`${settings.workingHours.start} - ${settings.workingHours.end}`}
                            />
                            <Chip
                              label="Available"
                              size="small"
                              color="success"
                            />
                          </ListItem>
                        ))}
                      </List>
                    </Paper>
                  </Grid>
                </Grid>
              </Box>
            )}
          </>
        )}
      </CardContent>

      <Dialog open={settingsOpen} onClose={() => setSettingsOpen(false)}>
        <DialogTitle>Google Calendar Settings</DialogTitle>
        <DialogContent>
          <FormControl fullWidth margin="normal">
            <InputLabel>Default Calendar</InputLabel>
            <Select
              value={settings.defaultCalendar}
              onChange={(e) => setSettings({ ...settings, defaultCalendar: e.target.value })}
            >
              {calendars.map((calendar) => (
                <MenuItem key={calendar.id} value={calendar.id}>
                  {calendar.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Box sx={{ mt: 2 }}>
            <Typography variant="subtitle2" gutterBottom>
              Automation Settings
            </Typography>
            <Button
              variant={settings.autoAccept ? 'contained' : 'outlined'}
              onClick={() => setSettings({ ...settings, autoAccept: !settings.autoAccept })}
              sx={{ mr: 1 }}
            >
              Auto Accept
            </Button>
            <FormControl sx={{ minWidth: 120 }}>
              <InputLabel>Notify Before</InputLabel>
              <Select
                value={settings.notifyBefore}
                onChange={(e) => setSettings({ ...settings, notifyBefore: e.target.value })}
              >
                <MenuItem value={15}>15 minutes</MenuItem>
                <MenuItem value={30}>30 minutes</MenuItem>
                <MenuItem value={60}>1 hour</MenuItem>
                <MenuItem value={120}>2 hours</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSettingsOpen(false)}>Cancel</Button>
          <Button onClick={handleSettingsSave} variant="contained">
            Save Settings
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={createEventOpen} onClose={() => setCreateEventOpen(false)}>
        <DialogTitle>Create New Event</DialogTitle>
        <DialogContent>
          {loading && <LinearProgress sx={{ mb: 2 }} />}
          <TextField
            fullWidth
            label="Event Title"
            value={newEvent.summary}
            onChange={(e) => setNewEvent({ ...newEvent, summary: e.target.value })}
            margin="normal"
          />
          <TextField
            fullWidth
            label="Description"
            value={newEvent.description}
            onChange={(e) => setNewEvent({ ...newEvent, description: e.target.value })}
            margin="normal"
            multiline
            rows={2}
          />
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} md={6}>
              <LocalizationProvider dateAdapter={AdapterDateFns}>
                <DateTimePicker
                  label="Start Time"
                  value={newEvent.start}
                  onChange={(date) => setNewEvent({ ...newEvent, start: date })}
                  renderInput={(params) => <TextField {...params} fullWidth margin="normal" />}
                />
              </LocalizationProvider>
            </Grid>
            <Grid item xs={12} md={6}>
              <LocalizationProvider dateAdapter={AdapterDateFns}>
                <DateTimePicker
                  label="End Time"
                  value={newEvent.end}
                  onChange={(date) => setNewEvent({ ...newEvent, end: date })}
                  renderInput={(params) => <TextField {...params} fullWidth margin="normal" />}
                />
              </LocalizationProvider>
            </Grid>
          </Grid>
          <FormControl fullWidth margin="normal">
            <InputLabel>Calendar</InputLabel>
            <Select
              value={newEvent.calendar}
              onChange={(e) => setNewEvent({ ...newEvent, calendar: e.target.value })}
            >
              {calendars.map((calendar) => (
                <MenuItem key={calendar.id} value={calendar.id}>
                  {calendar.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            fullWidth
            label="Location"
            value={newEvent.location}
            onChange={(e) => setNewEvent({ ...newEvent, location: e.target.value })}
            margin="normal"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateEventOpen(false)}>Cancel</Button>
          <Button onClick={handleCreateEvent} variant="contained" disabled={loading}>
            Create Event
          </Button>
        </DialogActions>
      </Dialog>
    </Card>
  );
};

export default GoogleCalendarIntegration; 