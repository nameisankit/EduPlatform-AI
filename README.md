# 🎓 EduPlatform AI — Spring Boot + Spring AI

### 🚀 Multimodal Learning • 🤖 Multi-Agent AI • ☁️ Cloud Deployed

> **Python/Streamlit + Groq → Spring Boot 3 + Spring AI**
> Same features, same UI, enterprise-grade Java backend.

---

## 🔄 Tech Conversion

| Original (Python) | This Version (Java) |
|---|---|
| Python 3.11 | Java 21 |
| Streamlit | Spring Boot 3.3 + Thymeleaf |
| Groq via `groq` SDK | Spring AI (`spring-ai-openai`) with Groq base URL |
| Agent classes in `agents/` | Spring `@Service` beans |
| `session_state` dict | `HttpSession` |
| `st.spinner` | CSS loading overlay + JS |
| `st.tabs` | CSS/JS tabs |
| `st.chat_input` | AJAX fetch + dynamic DOM |
| Pollinations image gen | Same URL (no change needed) |
| `requirements.txt` | `pom.xml` |

---

## 🧠 Architecture

```
User Input (Topic + Level)
          │
          ▼
🤖 Spring AI (Groq LLM via OpenAI-compatible API)
          │
          ▼
🧠 Multi-Agent Pipeline (Spring @Service)
  ├── CurriculumAgent  — 📋 Builds roadmap
  ├── TutorAgent       — 📖 Delivers lesson + chat
  ├── QuizAgent        — ❓ Creates questions
  └── FeedbackAgent    — ✅ Evaluates answers
          │
          ▼
💻 Thymeleaf UI (same look & feel as Streamlit)
          │
          ▼
☁️ Docker + GitHub Actions + AWS EC2
```

---

## ⚡ Quick Start (Local)

```bash
# Clone
git clone <your-repo>
cd eduplatform-springboot

# Set your Groq API key
export GROQ_API_KEY=your_key_here

# Run
./mvnw spring-boot:run
```

Open: http://localhost:8080

---

## 🐳 Docker

```bash
docker build -t eduplatform-ai .
docker run -d -p 8080:8080 \
  -e GROQ_API_KEY=your_key_here \
  eduplatform-ai
```

---

## ☁️ AWS EC2 Deploy

```bash
docker run -d -p 8080:8080 \
  -e GROQ_API_KEY=your_key_here \
  yourdockerhub/eduplatform-ai-spring:latest
```

---

## 🔐 Environment Variables

| Variable | Required | Purpose |
|---|---|---|
| `GROQ_API_KEY` | ✅ Yes | AI text generation via Groq |

---

## 📁 Project Structure

```
eduplatform-springboot/
├── pom.xml                              # Maven dependencies
├── Dockerfile                           # Docker build
├── docker-compose.yml                   # Docker Compose
│
├── src/main/java/com/eduplatform/
│   ├── EduPlatformApplication.java      # 🎯 Spring Boot entry point
│   ├── controller/
│   │   └── EduController.java           # HTTP routes (replaces Streamlit routing)
│   ├── service/
│   │   ├── EducationPipeline.java       # 🧠 Agent orchestrator
│   │   ├── CurriculumAgent.java         # 📋 Curriculum Agent
│   │   ├── TutorAgent.java              # 📖 Tutor Agent + Chat
│   │   ├── QuizAgent.java               # ❓ Quiz Agent
│   │   └── FeedbackAgent.java           # ✅ Feedback Agent
│   └── model/
│       └── LearningMemory.java          # Data model (replaces Python memory object)
│
├── src/main/resources/
│   ├── application.properties           # Spring AI / Groq config
│   ├── templates/
│   │   └── index.html                   # Thymeleaf UI (mirrors Streamlit app.py UI)
│   └── static/
│       ├── css/style.css                # Same visual style as Streamlit
│       └── js/app.js                    # Tabs, markdown, chat AJAX
│
└── .github/workflows/cicd.yml           # CI/CD (same pipeline as original)
```

---

## 🤖 Spring AI Configuration (Groq)

Groq is OpenAI-compatible. In `application.properties`:

```properties
spring.ai.openai.api-key=${GROQ_API_KEY}
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama3-8b-8192
```

Spring AI's `ChatClient` handles all LLM calls — no manual HTTP needed.

---

## 👨‍💻 Author

**Ankit Parmar** — DevOps + GenAI + Full Stack
