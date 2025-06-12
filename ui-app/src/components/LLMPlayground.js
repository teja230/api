import React, { useState, useRef } from 'react';
import { sendPrompt } from '../services/OpenAIService';
import { Box, Button, TextField, Typography, Paper, Select, MenuItem } from '@mui/material';

const DEFAULT_API_KEY = '';

const userTiers = [
  { value: 'free', label: 'Free' },
  { value: 'pro', label: 'Pro' },
];

export default function LLMPlayground() {
  const [prompt, setPrompt] = useState('');
  const [apiKey, setApiKey] = useState(DEFAULT_API_KEY);
  const [history, setHistory] = useState([]); // {role, content}
  const [response, setResponse] = useState('');
  const [streaming, setStreaming] = useState(false);
  const [userTier, setUserTier] = useState('free');
  const responseRef = useRef();

  const handleSend = async () => {
    setStreaming(true);
    setResponse('');
    const context = [...history];
    await sendPrompt({
      prompt,
      apiKey,
      context,
      userTier,
      onStream: (text) => {
        setResponse(text);
        responseRef.current.scrollTop = responseRef.current.scrollHeight;
      },
    }).then((finalText) => {
      setHistory([...context, { role: 'user', content: prompt }, { role: 'assistant', content: finalText }]);
      setStreaming(false);
      setPrompt('');
    });
  };

  return (
    <Box sx={{ maxWidth: 600, mx: 'auto', mt: 4 }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Typography variant="h6">LLM Playground</Typography>
        <TextField
          label="OpenAI API Key"
          value={apiKey}
          onChange={e => setApiKey(e.target.value)}
          fullWidth
          margin="normal"
          type="password"
        />
        <Select
          value={userTier}
          onChange={e => setUserTier(e.target.value)}
          sx={{ mb: 2 }}
        >
          {userTiers.map(tier => (
            <MenuItem key={tier.value} value={tier.value}>{tier.label}</MenuItem>
          ))}
        </Select>
        <TextField
          label="Your prompt"
          value={prompt}
          onChange={e => setPrompt(e.target.value)}
          fullWidth
          multiline
          minRows={2}
          disabled={streaming}
        />
        <Button
          variant="contained"
          sx={{ mt: 2 }}
          onClick={handleSend}
          disabled={!prompt || !apiKey || streaming}
        >
          Send
        </Button>
      </Paper>
      <Paper sx={{ p: 2, mb: 2, minHeight: 120, maxHeight: 200, overflowY: 'auto' }} ref={responseRef}>
        <Typography variant="subtitle2">Assistant Response:</Typography>
        <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>{response}</Typography>
      </Paper>
      <Paper sx={{ p: 2, maxHeight: 200, overflowY: 'auto' }}>
        <Typography variant="subtitle2">Conversation History:</Typography>
        {history.map((msg, idx) => (
          <Typography key={idx} variant="body2" color={msg.role === 'user' ? 'primary' : 'secondary'}>
            <b>{msg.role}:</b> {msg.content}
          </Typography>
        ))}
      </Paper>
    </Box>
  );
}

