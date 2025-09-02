# 📱 PichSnap (PitchSnap) — AI Pitch Deck Summarizer
> Summarize any pitch deck in 60 seconds — on mobile.

[![Android](https://img.shields.io/badge/Android-Java-green.svg)](https://developer.android.com)
[![Built with Android Studio](https://img.shields.io/badge/Android%20Studio-Java-blue.svg)](https://developer.android.com/studio)
[![OpenAI](https://img.shields.io/badge/AI-OpenAI-black.svg)](https://platform.openai.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](#license)

PichSnap is a mobile-first tool for investors and founders to **import a pitch deck (PDF)** and instantly get a **10-bullet summary**, **scorecard** (team, market, traction, clarity), **risks & questions**, and a ready-to-send **draft reply**.

---

## ✨ Features
- **Upload Deck (PDF/Drive)** via Android Storage Access Framework
- **Instant Summary** (bullets) + **Scorecard** (0–10 for team/market/traction/clarity)
- **Risks & Questions** you should ask
- **Draft Reply** (email/LinkedIn) for fast follow-ups
- **Share & Save** to Gmail, Slack, WhatsApp
- **History** with offline cache (Room DB)
- Optional **Firebase Auth** & synced history

---

## 🧭 Demo Flow
1. Tap **Upload Deck** → pick PDF  
2. Get **Summary, Score, Risks, Questions**  
3. Tap **Draft Reply** → email/LI message appears  
4. **Share** to Gmail/Slack/WhatsApp

---

## 🛠️ Architecture (High level)
**Android (Java)**  
- ViewModel/Lifecycle, Room, WorkManager  
- Retrofit/OkHttp for API  
- SAF file picker (no keys on device)

**Backend (Node.js _or_ FastAPI)**  
- Extracts text from PDF (pdfminer/pypdf)  
- Calls OpenAI for summary/score/action items  
- Returns **structured JSON** to the app

**Storage/Analytics (optional)**  
- Firebase Auth, Firestore for history, Crashlytics  
- Local cache via Room DB

---
![Braydon's GitHub Banner](./alazarheader.jpg)
---
## 📂 Project Structure (Android)

