import express from 'express';
import cors from 'cors';
import multer from 'multer';
import rateLimit from 'express-rate-limit';
import dotenv from 'dotenv';
import pdfParse from 'pdf-parse';
import fetch from 'node-fetch';   // 👈 add this

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

// Simple bearer auth
function requireAuth(req, res, next) {
  const auth = req.header('Authorization') || '';
  const expected = 'Bearer ' + (process.env.AUTH_TOKEN || 'CHANGE_ME_DEV_TOKEN');
  if (auth !== expected) return res.status(401).json({ error: 'unauthorized' });
  next();
}

// Hugging Face model endpoint
const HF_MODEL = "mistralai/Mistral-7B-Instruct-v0.2";
  // pick any instruct model
const HF_TOKEN = process.env.HF_TOKEN;

async function queryHuggingFace(prompt) {
  const resp = await fetch(`https://api-inference.huggingface.co/models/${HF_MODEL}`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${HF_TOKEN}`,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ inputs: prompt, parameters: { max_new_tokens: 512, temperature: 0.3 } })
  });

  if (!resp.ok) {
    throw new Error(`HF API error: ${resp.status} ${await resp.text()}`);
  }

  const data = await resp.json();
  // HF sometimes returns [{generated_text: "..."}]
  return data[0]?.generated_text || JSON.stringify(data);
}

app.post('/summarize', requireAuth, upload.single('file'), async (req, res) => {
  try {
    if (!req.file) return res.status(400).json({ error: 'file required' });

    const pdfBuffer = req.file.buffer;
    const parsed = await pdfParse(pdfBuffer);
    let text = (parsed.text || '').replace(/\s+\n/g, '\n').trim();

    if (!text || text.length < 50) {
      return res.status(400).json({ error: 'Could not extract text from PDF' });
    }

    const MAX_CHARS = 3000;
    if (text.length > MAX_CHARS) text = text.slice(0, MAX_CHARS) + '\n\n...[truncated]';

    // call Hugging Face summarizer
    const resp = await fetch(`https://api-inference.huggingface.co/models/facebook/bart-large-cnn`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${HF_TOKEN}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ inputs: text })
    });

    if (!resp.ok) {
      throw new Error(`HF API error: ${resp.status} ${await resp.text()}`);
    }

    const data = await resp.json();
    const summaryText = data[0]?.summary_text || "No summary generated.";

    // 🔑 Wrap response in your schema
    const formattedResponse = {
      title: "PitchSnap Summary",
      bullets: [summaryText],
      scorecard: { team: 0, market: 0, traction: 0, clarity: 0 },
      risks: [],
      questions: [],
      drafts: { email: "", linkedin: "" }
    };

    res.json(formattedResponse);

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
    const m = s.match(/\{[\s\S]*\}/);
    if (m) {
      try { return JSON.parse(m[0]); } catch (_) {}
    }
    return null;
  }
}

const port = process.env.PORT || 3000;
app.listen(port, "0.0.0.0", () =>
  console.log("PitchSnap API on :" + port)
);
