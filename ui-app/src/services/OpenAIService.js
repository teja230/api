// OpenAIService.js
// Service to interact with OpenAI API (supports streaming, context, and tier-based access)
import axios from 'axios';

const OPENAI_API_URL = 'https://api.openai.com/v1/chat/completions';

export async function sendPrompt({ prompt, apiKey, context = [], onStream, model = 'gpt-3.5-turbo', userTier = 'free' }) {
  // Build messages array for context memory
  const messages = [
    ...context.map((item) => ({ role: item.role, content: item.content })),
    { role: 'user', content: prompt },
  ];

  // Tier-based feature gating
  let maxTokens = 512;
  let temperature = 0.7;
  if (userTier === 'pro') {
    maxTokens = 2048;
    temperature = 0.5;
  }

  // Streaming support
  const response = await fetch(OPENAI_API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${apiKey}`,
    },
    body: JSON.stringify({
      model,
      messages,
      max_tokens: maxTokens,
      temperature,
      stream: !!onStream,
    }),
  });

  if (onStream) {
    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let done = false;
    let fullText = '';
    while (!done) {
      const { value, done: doneReading } = await reader.read();
      done = doneReading;
      const chunk = decoder.decode(value);
      // OpenAI streams data as lines starting with 'data: '
      chunk.split('\n').forEach(line => {
        if (line.startsWith('data: ')) {
          const data = line.replace('data: ', '').trim();
          if (data && data !== '[DONE]') {
            try {
              const parsed = JSON.parse(data);
              const text = parsed.choices[0].delta.content || '';
              fullText += text;
              onStream(fullText);
            } catch {}
          }
        }
      });
    }
    return fullText;
  } else {
    const data = await response.json();
    return data.choices[0].message.content;
  }
}

