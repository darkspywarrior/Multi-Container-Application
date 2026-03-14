# Multi-Container-Application
[Multi-Container Application Project](https://roadmap.sh/projects/multi-container-service)

![Docker](https://img.shields.io/badge/Docker-Containerized-blue)
![Node.js](https://img.shields.io/badge/Node.js-Backend-green)
![MongoDB](https://img.shields.io/badge/MongoDB-Database-green)
![CI/CD](https://img.shields.io/badge/CI/CD-GitHub%20Actions-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

A **production-style Todo API** built with **Node.js, Express, MongoDB, Docker, Nginx, Swagger, Prometheus, and Grafana**.

This project demonstrates **real-world DevOps practices**, including:

* Containerization with Docker
* Multi-container orchestration with Docker Compose
* Reverse proxy configuration with Nginx
* API documentation using Swagger
* Monitoring using Prometheus and Grafana
* CI/CD automation using GitHub Actions

This project is designed as a **DevOps portfolio project** demonstrating how modern applications are built and deployed.

---

# 📦 Tech Stack

| Technology     | Purpose                       |
| -------------- | ----------------------------- |
| Node.js        | Backend runtime               |
| Express        | REST API framework            |
| MongoDB        | NoSQL database                |
| Mongoose       | MongoDB ODM                   |
| Docker         | Containerization              |
| Docker Compose | Multi-container orchestration |
| Nginx          | Reverse proxy                 |
| Swagger        | API documentation             |
| Prometheus     | Metrics collection            |
| Grafana        | Monitoring dashboards         |
| GitHub Actions | CI/CD automation              |

---

# 🏗 System Architecture

```
Client / Browser
        │
        ▼
   Nginx Reverse Proxy
        │
        ▼
   Node.js API (Express)
        │
        ▼
      MongoDB
        │
        ▼
Monitoring Stack
 ├── Prometheus
 └── Grafana
```

CI/CD Pipeline:

```
Developer
   │
   ▼
GitHub Repository
   │
   ▼
GitHub Actions
   │
   ▼
Docker Build & Deployment
```

---

# ✨ Features

* Full **CRUD REST API**
* **MongoDB persistence**
* **Dockerized services**
* **Multi-container architecture**
* **Swagger interactive API documentation**
* **Reverse proxy with Nginx**
* **Monitoring dashboards**
* **CI/CD pipeline**
* **Health check endpoints**

---

# 📡 API Endpoints

| Method | Endpoint      | Description    |
| ------ | ------------- | -------------- |
| GET    | `/todos`      | Get all todos  |
| POST   | `/todos`      | Create a todo  |
| GET    | `/todos/{id}` | Get todo by ID |
| PUT    | `/todos/{id}` | Update todo    |
| DELETE | `/todos/{id}` | Delete todo    |

---

# 🧪 Example API Request

Create a Todo:

```json
{
  "title": "Learn DevOps",
  "completed": false
}
```

---

# 📘 API Documentation (Swagger)

Swagger provides interactive API documentation.

Open:

```
http://localhost:3000/api-docs
```

You can:

* Test API requests
* Send POST/PUT bodies
* View responses
* Explore endpoints

---

# 📊 Monitoring

The monitoring stack includes **Prometheus + Grafana**.

### Prometheus

Metrics collection service.

```
http://localhost:9090
```

### Grafana

Visualization dashboard.

```
http://localhost:3001
```

Default login:

```
username: admin
password: admin
```

---

# 📁 Project Structure

```
Multi-Container-Application
│
├── src
│   ├── models
│   │   └── Todo.js
│   │
│   ├── routes
│   │   └── todoRoutes.js
│   │
│   ├── server.js
│   └── swagger.js
│
├── nginx
│   └── nginx.conf
│
├── docker-compose.yml
├── Dockerfile
├── package.json
└── README.md
```

---

# ⚙️ Running the Project Locally

## 1️⃣ Prerequisites

Install:

* Docker
* Docker Compose

Optional:

* Node.js

---

## 2️⃣ Clone the Repository

```
git clone git@github.com:darkspywarrior/Multi-Container-Application.git
```

```
cd Multi-Container-Application
```

---

## 3️⃣ Start the Application

```
docker compose up --build
```

This starts the following containers:

* API container
* MongoDB container
* Nginx reverse proxy
* Prometheus monitoring
* Grafana dashboard

---

## 4️⃣ Access Services

| Service      | URL                            |
| ------------ | ------------------------------ |
| API          | http://localhost:3000          |
| Swagger Docs | http://localhost:3000/api-docs |
| Prometheus   | http://localhost:9090          |
| Grafana      | http://localhost:3001          |

---

# 🛑 Stop the Application

```
docker compose down
```

---

# 🔁 CI/CD Pipeline

This project includes **GitHub Actions CI/CD**.

Every push to the `main` branch triggers:

1️⃣ Repository checkout
2️⃣ Docker build
3️⃣ Deployment process

Workflow file:

```
.github/workflows/deploy.yml
```

---

# 🌐 Reverse Proxy (Nginx)

Nginx forwards external requests to the Node API.

Request flow:

```
Client
   │
   ▼
Nginx
   │
   ▼
Node API
   │
   ▼
MongoDB
```

Benefits:

* Security
* Clean routing
* SSL support (future)

---

# 🔐 Environment Variables

Create `.env` file:

```
MONGO_URL=mongodb://mongo:27017/todos
PORT=3000
```
---

# 🚀 Future Improvements

Possible extensions:

* JWT authentication
* HTTPS with Let's Encrypt
* Kubernetes deployment
* Terraform infrastructure
* Distributed tracing
* Rate limiting
* Load balancing

---

# 🤝 Contributing

1️⃣ Fork the repository

2️⃣ Create a new branch

```
git checkout -b feature-name
```

3️⃣ Commit changes

```
git commit -m "Add feature"
```

4️⃣ Push changes

```
git push origin feature-name
```

5️⃣ Open a Pull Request

---


Add user registration/login (JWT)
Make it look prettier (React frontend?)
Add dark mode
Write unit tests
Improve error messages

