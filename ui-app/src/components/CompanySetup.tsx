import React, { useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  TextField,
  Typography,
  Stepper,
  Step,
  StepLabel,
  Alert,
} from '@mui/material';

const steps = ['Company Details', 'GitHub Integration', 'Complete'];

export const CompanySetup: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [companyName, setCompanyName] = useState('');
  const [companyDomain, setCompanyDomain] = useState('');
  const [githubClientId, setGithubClientId] = useState('');
  const [githubClientSecret, setGithubClientSecret] = useState('');
  const [error, setError] = useState<string | null>(null);

  const handleNext = async () => {
    try {
      if (activeStep === 0) {
        // Create company
        const response = await fetch('/api/companies', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: companyName, domain: companyDomain }),
        });
        if (!response.ok) throw new Error('Failed to create company');
      } else if (activeStep === 1) {
        // Configure GitHub OAuth
        const response = await fetch('/api/github/oauth/config', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            clientId: githubClientId,
            clientSecret: githubClientSecret,
          }),
        });
        if (!response.ok) throw new Error('Failed to configure GitHub OAuth');
      }
      setActiveStep((prev) => prev + 1);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleBack = () => {
    setActiveStep((prev) => prev - 1);
  };

  const renderStepContent = (step: number) => {
    switch (step) {
      case 0:
        return (
          <Box sx={{ mt: 2 }}>
            <TextField
              fullWidth
              label="Company Name"
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Company Domain"
              value={companyDomain}
              onChange={(e) => setCompanyDomain(e.target.value)}
              margin="normal"
            />
          </Box>
        );
      case 1:
        return (
          <Box sx={{ mt: 2 }}>
            <Typography variant="body1" gutterBottom>
              To set up GitHub integration, you need to:
            </Typography>
            <ol>
              <li>Go to GitHub Developer Settings</li>
              <li>Create a new OAuth App</li>
              <li>Set the callback URL to: {window.location.origin}/api/github/oauth/callback</li>
              <li>Enter the Client ID and Secret below</li>
            </ol>
            <TextField
              fullWidth
              label="GitHub Client ID"
              value={githubClientId}
              onChange={(e) => setGithubClientId(e.target.value)}
              margin="normal"
            />
            <TextField
              fullWidth
              label="GitHub Client Secret"
              type="password"
              value={githubClientSecret}
              onChange={(e) => setGithubClientSecret(e.target.value)}
              margin="normal"
            />
          </Box>
        );
      case 2:
        return (
          <Box sx={{ mt: 2 }}>
            <Typography variant="h6" gutterBottom>
              Setup Complete!
            </Typography>
            <Typography variant="body1">
              Your company has been set up with GitHub integration. You can now:
            </Typography>
            <ul>
              <li>Connect repositories</li>
              <li>Set up webhooks</li>
              <li>Configure automation rules</li>
            </ul>
          </Box>
        );
      default:
        return null;
    }
  };

  return (
    <Card sx={{ maxWidth: 600, mx: 'auto', mt: 4 }}>
      <CardContent>
        <Typography variant="h5" gutterBottom>
          Company Setup
        </Typography>
        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        {renderStepContent(activeStep)}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
          <Button
            disabled={activeStep === 0}
            onClick={handleBack}
          >
            Back
          </Button>
          <Button
            variant="contained"
            onClick={handleNext}
            disabled={activeStep === steps.length - 1}
          >
            {activeStep === steps.length - 1 ? 'Finish' : 'Next'}
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
}; 