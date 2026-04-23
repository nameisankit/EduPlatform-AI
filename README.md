# 🎓 EduPlatform AI

### 🚀 Multimodal Learning • 🤖 Multi-Agent AI • ☁️ Cloud Deployed

> **Three powerful systems combined into one intelligent education platform**
> GenAI + Agentic AI + DevOps = **Production-ready AI learning system**

---

## 🌐 Live Demo

👉 Your deployed app:

```
http://13.49.145.251:8501
```

---

## 🧠 What This Project Does

EduPlatform AI automatically generates:

* 📋 Structured learning roadmap
* 📖 Full lesson explanation
* 🖼️ Visual learning content (AI-generated images)
* ❓ Quiz questions
* ✅ Feedback & evaluation

👉 All powered by a **multi-agent AI pipeline**

---

## 🏗️ Architecture Overview

```
User Input (Topic + Level)
          │
          ▼
🤖 GenAI Layer (Groq LLM+ Image AI)
          │
          ▼
🧠 Multi-Agent Pipeline
(Curriculum → Tutor → Quiz → Feedback)
          │
          ▼
💻 Streamlit UI (Interactive Learning)
          │
          ▼
☁️ Docker + AWS EC2 Deployment
```

---

## 🧩 Source Integration

This project integrates:

* 🤖 GenAI → Content + Image Generation
* 🤝 Agentic AI → Intelligent workflow system
* 🚀 DevOps → Docker + CI/CD + AWS

👉 Fully end-to-end system

---

##  📁 Project Structure

```
edu-platform/
├── app.py                     # 🎯 Main Streamlit app (entry point)
├── requirements.txt           # 📦 Python dependencies
├── Dockerfile                 # 🐳 Docker build config
├── docker-compose.yml         # ⚙️ Multi-container setup
├── run.bat                    # 🪟 Windows one-click run
├── run.sh                     # 🐧 Mac/Linux run script
├── .env                       # 🔐 Environment variables (not pushed)
├── .gitignore
│
├── core/                      # 🧠 GenAI Layer
│   ├── llm.py                 # Groq LLM integration (text generation)
│   └── image_gen.py           # Pollinations image generation (no API key)
│
├── agents/                    # 🤖 Multi-Agent System
│   ├── base.py                # Base agent class
│   ├── education_agents.py    # Curriculum, Tutor, Quiz, Feedback agents
│   └── pipeline.py            # Agent orchestration pipeline
│
├── .github/
│   └── workflows/
│       └── cicd.yml           # 🚀 CI/CD pipeline (build + deploy)
│
├── venv/                      # ⚠️ Local virtual environment 
```


## ⚡ Quick Start (Local)

```bash
# Clone repo
git clone <your-repo-link>
cd edu-platform

# Install dependencies
pip install -r requirements.txt

# Setup env
cp .env.example .env
# Add your keys:
# GROQ_API_KEY=your_key_here

# Run app
streamlit run app.py
```

👉 Open:

```
http://localhost:8501
```

---

## 🐳 Run with Docker

```bash
docker build -t edu-platform .
docker run -d -p 8501:8501 \
-e GROQ_API_KEY=your_key_here \
edu-platform
```

---

## ☁️ Deploy on AWS EC2

```bash
# Install Docker
sudo apt update
sudo apt install docker.io -y

# Run container
docker run -d -p 8501:8501 \
-e GROQ_API_KEY=your_key_here \
nameisankit07/edu-platform:latest
```

---

## 🔐 Environment Variables

| Variable       | Required    | Purpose            |
| -------------- | ----------- | ------------------ |
| GROQ_API_KEY | ✅ Yes       | AI text generation |


---

## 🤖 AI Agents

### 📋 Curriculum Agent

Creates structured roadmap

### 📖 Tutor Agent

Generates detailed lesson

### ❓ Quiz Agent

Creates questions

### ✅ Feedback Agent

Evaluates answers

---

## 🛠️ Tech Stack

* 🧠 LLM: Groq 
* 🎨 Image AI: Pollinations.ai
* 💻 UI: Streamlit
* 🐳 Docker
* ☁️ AWS EC2
* 🔄 GitHub Actions

---

## 🎯 Use Cases

* AI learning platform
* EdTech startup prototype
* Smart tutoring system
* Content generation system

---

## 🚀 Future Improvements

* 🔐 User authentication
* 📊 Progress tracking
* 🎤 Voice tutor
* 📄 PDF learning (RAG)
* ☸️ Kubernetes scaling

---

## 👨‍💻 Author

**Ankit Parmar**
DevOps + GenAI + Full Stack

---

## ⭐ If you like this project

Give it a star ⭐ on GitHub
