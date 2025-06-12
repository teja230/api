import React, { useState } from 'react';
import { Box, Paper, Typography, TextField, IconButton, List, ListItem, ListItemText, Chip, Divider, Autocomplete, CircularProgress, Container, Tooltip } from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import Header from './Header';

const SUGGESTIONS = [
  'Google',
  'JIRA',
  'Slack',
  'Google Calendar'
];

const SAMPLE_ANSWERS = {
  'how do i set up email':
    'To set up your email, go to Google Workspace, click on Gmail, and follow the setup wizard. If you need access, type /integrate Google.',
  'how do i access jira':
    'To access Jira, visit jira.company.com and log in with your company credentials. If you haven\'t integrated yet, type /integrate JIRA.',
  'how do i join slack':
    'To join Slack, download the Slack app and sign in with your company email. You can also type /integrate Slack to get started.',
  'how do i add my calendar':
    'To add your calendar, type /integrate Google Calendar and follow the prompts to sync your events.'
};

function getLLMResponse(input) {
  const lower = input.toLowerCase();
  if (lower.startsWith('/integrate')) {
    // Suggest integrations
    return {
      type: 'suggestions',
      suggestions: SUGGESTIONS.filter(s => s.toLowerCase().includes(lower.replace('/integrate', '').trim()))
    };
  }
  // Simulate LLM onboarding answers
  for (const [q, a] of Object.entries(SAMPLE_ANSWERS)) {
    if (lower.includes(q)) return { type: 'answer', answer: a };
  }
  // Default fallback
  return {
    type: 'answer',
    answer: 'I am your onboarding buddy! You can ask me about setting up tools, company policies, or type /integrate to connect your apps.'
  };
}

const OnboardingChatAgent = () => {
  const [messages, setMessages] = useState([
    { from: 'agent', text: 'Welcome to the company! I am your onboarding buddy. Type /integrate to connect your tools, or ask me anything about onboarding.' }
  ]);
  const [input, setInput] = useState('');
  const [autoSuggestions, setAutoSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleInputChange = (e) => {
    const val = e.target.value;
    setInput(val);
    if (val.startsWith('/integrate')) {
      const query = val.replace('/integrate', '').trim().toLowerCase();
      setAutoSuggestions(
        SUGGESTIONS.filter(s => s.toLowerCase().includes(query))
      );
    } else {
      setAutoSuggestions([]);
    }
  };

  const handleSend = async (value) => {
    if (!value.trim()) return;
    setMessages(msgs => [...msgs, { from: 'user', text: value }]);
    setInput('');
    setAutoSuggestions([]);
    if (value.startsWith('/integrate')) {
      const query = value.replace('/integrate', '').trim().toLowerCase();
      setMessages(msgs => [
        ...msgs,
        { from: 'agent', text: 'Which tool would you like to integrate?', suggestions: SUGGESTIONS.filter(s => s.toLowerCase().includes(query)) }
      ]);
      return;
    }
    setLoading(true);
    try {
      // Give a random answer for now
      const randomAnswers = [
        'Great question! Please check the onboarding guide for more details.',
        'You can find this information in the company wiki.',
        'Please reach out to your manager for access.',
        'This is usually handled by the IT department.',
        'Try logging in with your company credentials.',
        'I recommend checking your email for a welcome message.',
        'Let me get back to you on that!',
        'You can use the /integrate command to connect your tools.',
        'For more help, visit the onboarding portal.'
      ];
      const answer = randomAnswers[Math.floor(Math.random() * randomAnswers.length)];
      setTimeout(() => {
        setMessages(msgs => [
          ...msgs,
          { from: 'agent', text: answer }
        ]);
        setLoading(false);
      }, 700);
    } catch (e) {
      setMessages(msgs => [
        ...msgs,
        { from: 'agent', text: 'Sorry, there was a problem connecting to the assistant.' }
      ]);
      setLoading(false);
    }
  };

  const handleSuggestionClick = (suggestion) => {
    handleSend(`/integrate ${suggestion}`);
  };

  return (
    <Box>
      <Header />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box>
            <Typography variant="h4" component="h1" gutterBottom>
              Onboarding Buddy
            </Typography>
            <Typography variant="subtitle1" color="text.secondary">
              Get help with setting up your tools and learning about company policies
            </Typography>
          </Box>
        </Box>

        <Paper 
          elevation={3}
          sx={{
            borderRadius: 4,
            overflow: 'hidden',
            transition: 'transform 0.2s',
            '&:hover': {
              transform: 'translateY(-4px)'
            },
            minHeight: 600,
            display: 'flex',
            flexDirection: 'column',
            background: 'linear-gradient(135deg, #f5f7fa 0%, #e3e8ee 100%)',
            boxShadow: '0 8px 32px 0 rgba(31, 38, 135, 0.12)',
            border: '1px solid #e0e0e0',
          }}
        >
          <Box sx={{ p: 3, borderBottom: '1px solid #e0e0e0' }}>
            <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
              Chat with your onboarding buddy
            </Typography>
          </Box>

          <Box sx={{ flex: 1, overflowY: 'auto', p: 3 }}>
            <List>
              {messages.map((msg, idx) => (
                <ListItem 
                  key={idx} 
                  alignItems={msg.from === 'user' ? 'right' : 'left'}
                  sx={{ 
                    flexDirection: msg.from === 'user' ? 'row-reverse' : 'row',
                    gap: 1
                  }}
                >
                  <ListItemText
                    primary={msg.text}
                    primaryTypographyProps={{
                      align: msg.from === 'user' ? 'right' : 'left',
                      sx: {
                        color: msg.from === 'user' ? '#1976d2' : '#333',
                        fontWeight: msg.from === 'agent' ? 500 : 400,
                        background: msg.from === 'user' ? '#e3e8ee' : '#fff',
                        borderRadius: 2,
                        px: 2,
                        py: 1,
                        display: 'inline-block',
                        mb: 0.5,
                        boxShadow: 1
                      }
                    }}
                  />
                  {msg.suggestions && (
                    <Box sx={{ 
                      mt: 1, 
                      display: 'flex', 
                      gap: 1,
                      flexDirection: msg.from === 'user' ? 'row-reverse' : 'row'
                    }}>
                      {msg.suggestions.map(s => (
                        <Chip 
                          key={s} 
                          label={s} 
                          color="primary" 
                          onClick={() => handleSuggestionClick(s)}
                          sx={{
                            fontWeight: 'bold',
                            '& .MuiChip-label': {
                              px: 2
                            }
                          }}
                        />
                      ))}
                    </Box>
                  )}
                </ListItem>
              ))}
            </List>
          </Box>

          <Box sx={{ p: 3, borderTop: '1px solid #e0e0e0', bgcolor: 'background.paper' }}>
            {autoSuggestions.length > 0 && (
              <Box sx={{ mb: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                {autoSuggestions.map(s => (
                  <Chip 
                    key={s} 
                    label={s} 
                    color="primary" 
                    onClick={() => handleSuggestionClick(s)}
                    sx={{
                      fontWeight: 'bold',
                      '& .MuiChip-label': {
                        px: 2
                      }
                    }}
                  />
                ))}
              </Box>
            )}
            <Box sx={{ display: 'flex', gap: 1 }}>
              <TextField
                fullWidth
                variant="outlined"
                size="small"
                placeholder="Type a question or /integrate ..."
                value={input}
                onChange={handleInputChange}
                onKeyDown={e => {
                  if (e.key === 'Enter' && !loading) handleSend(input);
                }}
                sx={{ 
                  bgcolor: '#fff', 
                  borderRadius: 2,
                  '& .MuiOutlinedInput-root': {
                    borderRadius: 2
                  }
                }}
                disabled={loading}
              />
              <Tooltip title="Send message">
                <IconButton 
                  color="primary" 
                  onClick={() => handleSend(input)} 
                  disabled={loading}
                  sx={{ 
                    bgcolor: 'background.paper',
                    boxShadow: 1,
                    '&:hover': { bgcolor: 'action.hover' }
                  }}
                >
                  {loading ? <CircularProgress size={24} /> : <SendIcon />}
                </IconButton>
              </Tooltip>
            </Box>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
};

export default OnboardingChatAgent;
