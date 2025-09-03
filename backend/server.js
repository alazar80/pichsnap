// backend/server.js
import express from 'express';
import cors from 'cors';
import multer from 'multer';
import rateLimit from 'express-rate-limit';
import dotenv from 'dotenv';
import pdfParse from 'pdf-parse';

import OpenAI from 'openai';

dotenv.config();

const app = express();
const upload = multer({ storage: multer.memoryStorage(), limits: { fileSize: 20 * 1024 * 1024 } }); // 20MB
const limiter = rateLimit({ windowMs: 60_000, max: 60 });

const allow = (process.env.CORS_ORIGIN || '').split(',').map(s => s.trim()).filter(Boolean);
app.use(cors({
  origin: allow.length ? allow : '*',
}));
app.use(limiter);
app.get('/health', (req, res) => res.json({ ok: true }));

// Simple bearer auth for the mobile app
function requireAuth(req, res, next) {
  const auth = req.header('Authorization') || '';
  const expected = 'Bearer ' + (process.env.AUTH_TOKEN || 'CHANGE_ME_DEV_TOKEN');
  if (auth !== expected) return res.status(401).json({ error: 'unauthorized' });
  next();
}

const openai = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });

app.post('/summarize', requireAuth, upload.single('file'), async (req, res) => {
  try {
    if (!req.file) return res.status(400).json({ error: 'file required' });

    // Extract text from PDF
    const pdfBuffer = req.file.buffer;
    const parsed = await pdfParse(pdfBuffer);
    let text = (parsed.text || '').replace(/\s+\n/g, '\n').trim();

    if (!text || text.length < 50) {
      return res.status(400).json({ error: 'Could not extract text from PDF' });
    }

    // Keep prompt within practical bounds
    const MAX_CHARS = 45_000; // safe for gpt-4o-mini
    if (text.length > MAX_CHARS) text = text.slice(0, MAX_CHARS) + '\n\n...[truncated]';

    const system = `You are PitchSnap, an analyst for investors. 
Return ONLY strict JSON with this schema:
{
  "title": string,
  "bullets": string[10], // concise, plain language
  "scorecard": { "team": int, "market": int, "traction": int, "clarity": int }, // 0-10
  "risks": string[3..6],
  "questions": string[5],
  "drafts": { "email": string, "linkedin": string }
}`;

    const user = `Summarize the following pitch deck text. If content seems thin, be honest but useful.

TEXT:
"""${text}"""`;

    const resp = await openai.responses.create({
      model: 'gpt-4o-mini',
      input: [
        { role: 'system', content: system },
        { role: 'user', content: user }
      ],
      temperature: 0.3
    });

    // Prefer the convenience getter if present
    const raw = resp.output_text ?? JSON.stringify(resp, null, 2);

    const json = safeParseJson(raw);
    if (!json || !json.bullets || !json.scorecard) {
      return res.status(502).json({ error: 'Malformed AI response', raw });
    }
    res.json(json);
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: e.message || 'server error' });
  }
});

// Helpers
function safeParseJson(s) {
  try {
    return JSON.parse(s);
  } catch (_) {
    // Try to salvage JSON block
    const m = s.match(/\{[\s\S]*\}/);
    if (m) {
      try { return JSON.parse(m[0]); } catch (_) { /* ignore */ }
    }
    return null;
  }
}

const port = process.env.PORT || 3000;
app.listen(port, () => console.log('PitchSnap API on :' + port));
