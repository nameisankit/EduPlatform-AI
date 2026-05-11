# 🚀 EduPlatform AI — Spring Boot Backend

AI-powered educational backend platform built using **Spring Boot 3**, **Java 21**, and external AI APIs using **HTTP Client integration**.

---

# ✨ Features

* 🤖 AI-powered quiz generation
* 📚 Learning session management
* 📈 User progress tracking
* 💬 Chat-based educational interactions
* 🧠 Multi-agent style architecture
* 🗄️ Persistent session storage
* ⚡ External AI API integration using HTTP Client
* 🌐 RESTful APIs
* 🐳 Docker support
* ☁️ Cloud deployment ready

---

# 🏗️ Project Structure

```bash
eduplatform-springboot/
│
├── .github/                     # GitHub workflows
├── data/                        # Application data
│
├── src/
│   └── main/
│       ├── java/com/eduplatform/
│       │
│       │   ├── controller/      # REST Controllers
│       │   ├── service/         # Business Logic
│       │   ├── repository/      # JPA Repositories
│       │   ├── model/           # Entity Classes / DTOs
│       │   ├── util/            # Utility Classes
│       │   ├── config/          # Configurations
│       │   │
│       │   └── EduPlatformApplication.java
│       │
│       └── resources/
│           └── application.properties
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── render.yaml
└── README.md
```

---

# 🛠️ Tech Stack

| Technology      | Usage                 |
| --------------- | --------------------- |
| Java 21         | Backend Development   |
| Spring Boot 3   | Backend Framework     |
| Spring Data JPA | Database Layer        |
| Hibernate       | ORM                   |
| Maven           | Dependency Management |
| HTTP Client     | External AI API Calls |
| Docker          | Containerization      |

---

# 🤖 AI Integration

This project does not use Spring AI.

AI functionality is implemented using:

* Java HTTP Client / REST calls
* External AI model APIs
* Custom service-layer integration

---

# 🚀 Getting Started

## 1️⃣ Clone Repository

```bash
git clone https://github.com/nameisankit/EduPlatform-AI.git
cd EduPlatform-AI
```

---

## 2️⃣ Configure Application

Update `application.properties`

```properties
server.port=8080
```

Add your external AI API configuration if needed.

---

## 3️⃣ Run Application

```bash
mvn clean install
mvn spring-boot:run
```

Application runs at:

```bash
http://localhost:8080
```

---

# 🐳 Docker Setup

## Build Docker Image

```bash
docker build -t eduplatform-ai .
```

## Run Container

```bash
docker run -p 8080:8080 eduplatform-ai
```

---

# ☁️ Deployment

Supported deployment platforms:

* Render
* Railway
* VPS
* Docker
* AWS

Using:

```bash
render.yaml
```

---

# 📡 Architecture

```text
Client
   ↓
REST Controllers
   ↓
Service Layer
   ↓
AI API Integration
   ↓
Repositories
   ↓
Database
```

---

# 🔥 Future Improvements

* 🎤 Voice-based AI learning
* 📄 PDF support
* 📹 Video summarization
* 🌍 Multi-language support
* 📱 Mobile integration

---

# 👨‍💻 Author

## Ankit Parmar

* Java Full Stack Developer
* Spring Boot Developer
* Backend Developer

GitHub:
https://github.com/nameisankit

---

# ⭐ Support

If you like this project:

* ⭐ Star the repository
* 🍴 Fork the project
* 🛠️ Contribute

---
