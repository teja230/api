import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Paper,
  TextField,
  Button,
  Grid,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Autocomplete,
  Divider,
  Alert
} from '@mui/material';
import { FaUsers, FaPlus } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';

const NewTeamPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    department: '',
    members: [],
    integrations: []
  });
  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState(false);

  // Sample data for dropdowns
  const departments = [
    'Engineering',
    'Product',
    'Design',
    'Marketing',
    'Sales',
    'HR',
    'Operations'
  ];

  const availableIntegrations = [
    { id: 'slack', name: 'Slack', icon: '💬' },
    { id: 'github', name: 'GitHub', icon: '📦' },
    { id: 'jira', name: 'Jira', icon: '📋' },
    { id: 'google', name: 'Google Calendar', icon: '📅' }
  ];

  const handleInputChange = (field) => (event) => {
    setFormData({
      ...formData,
      [field]: event.target.value
    });
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors({
        ...errors,
        [field]: null
      });
    }
  };

  const handleMemberAdd = (newMember) => {
    if (newMember && !formData.members.includes(newMember)) {
      setFormData({
        ...formData,
        members: [...formData.members, newMember]
      });
    }
  };

  const handleMemberRemove = (memberToRemove) => {
    setFormData({
      ...formData,
      members: formData.members.filter(member => member !== memberToRemove)
    });
  };

  const handleIntegrationToggle = (integration) => {
    setFormData({
      ...formData,
      integrations: formData.integrations.includes(integration)
        ? formData.integrations.filter(i => i !== integration)
        : [...formData.integrations, integration]
    });
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.name.trim()) {
      newErrors.name = 'Team name is required';
    }
    if (!formData.department) {
      newErrors.department = 'Department is required';
    }
    if (formData.members.length === 0) {
      newErrors.members = 'At least one team member is required';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (validateForm()) {
      try {
        // TODO: Implement API call to create team
        console.log('Creating team:', formData);
        setSuccess(true);
        setTimeout(() => {
          navigate('/teams');
        }, 2000);
      } catch (error) {
        setErrors({
          submit: 'Failed to create team. Please try again.'
        });
      }
    }
  };

  const handleCancel = () => {
    // Use browser history to go back
    navigate(-1);
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Create New Team
        </Typography>
        <Typography variant="subtitle1" color="text.secondary" sx={{ mb: 2 }}>
          Set up a new team with members and integrations
        </Typography>
      </Box>

      {success && (
        <Alert severity="success" sx={{ mb: 4 }}>
          Team created successfully! Redirecting to teams page...
        </Alert>
      )}

      {errors.submit && (
        <Alert severity="error" sx={{ mb: 4 }}>
          {errors.submit}
        </Alert>
      )}

      <Paper elevation={3} sx={{ p: 4 }}>
        <form onSubmit={handleSubmit}>
          <Grid container spacing={3}>
            {/* Team Basic Information */}
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Team Information
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Team Name"
                value={formData.name}
                onChange={handleInputChange('name')}
                error={!!errors.name}
                helperText={errors.name}
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth error={!!errors.department} required>
                <InputLabel>Department</InputLabel>
                <Select
                  value={formData.department}
                  onChange={handleInputChange('department')}
                  label="Department"
                >
                  {departments.map((dept) => (
                    <MenuItem key={dept} value={dept}>
                      {dept}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Team Description"
                value={formData.description}
                onChange={handleInputChange('description')}
                multiline
                rows={3}
              />
            </Grid>

            <Grid item xs={12}>
              <Divider sx={{ my: 2 }} />
            </Grid>

            {/* Team Members */}
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Team Members
              </Typography>
              <Autocomplete
                freeSolo
                options={[]}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Add Team Member"
                    placeholder="Enter email address"
                    error={!!errors.members}
                    helperText={errors.members}
                  />
                )}
                onChange={(event, newValue) => handleMemberAdd(newValue)}
                sx={{ mb: 2 }}
              />
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {formData.members.map((member) => (
                  <Chip
                    key={member}
                    label={member}
                    onDelete={() => handleMemberRemove(member)}
                    icon={<FaUsers />}
                  />
                ))}
              </Box>
            </Grid>

            <Grid item xs={12}>
              <Divider sx={{ my: 2 }} />
            </Grid>

            {/* Integrations */}
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Integrations
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Select the tools your team will use
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {availableIntegrations.map((integration) => (
                  <Chip
                    key={integration.id}
                    label={`${integration.icon} ${integration.name}`}
                    onClick={() => handleIntegrationToggle(integration.id)}
                    color={formData.integrations.includes(integration.id) ? 'primary' : 'default'}
                    variant={formData.integrations.includes(integration.id) ? 'filled' : 'outlined'}
                    sx={{ m: 0.5 }}
                  />
                ))}
              </Box>
            </Grid>

            {/* Submit Button */}
            <Grid item xs={12}>
              <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 2 }}>
                <Button
                  variant="outlined"
                  onClick={handleCancel}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  startIcon={<FaPlus />}
                  sx={{ boxShadow: 2 }}
                >
                  Create Team
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Container>
  );
};

export default NewTeamPage; 