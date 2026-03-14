# Multi-Container-Application
[Multi-Container Application Project](https://roadmap.sh/projects/multi-container-service)

A simple full-stack Todo List application using **Docker Compose** with a **Node.js + Express + Mongoose** backend and **MongoDB** database.
PART-2
# 🚀 Todo App with Docker & Nginx  
**My Computer Science Portfolio Project**

A fun, fully containerized Todo REST API I built to learn real DevOps practices!  
It uses multiple Docker containers, a reverse proxy, and clean code — exactly what I wanted to show in my portfolio.

Perfect for anyone who wants to see how a simple app can be made modern and clean using Docker.

## ✨ What I Built
- REST API for managing todos (create, read, update, delete)
- MongoDB for persistent storage
- Docker + Docker Compose for easy multi-container setup
- Nginx as a reverse proxy (clean URLs + extra security practice)
- Swagger UI for interactive API testing right in the browser

## 🛠 Tech Stack

| Technology       | Purpose                     |
|------------------|-----------------------------|
| Node.js + Express| Backend API                 |
| MongoDB + Mongoose | Database & modeling      |
| Docker           | Containerization            |
| Docker Compose   | Multi-container orchestration |
| Nginx            | Reverse proxy               |
| Swagger          | Interactive API documentation |

## 🏗 Architecture (Super Simple)
Browser
↓
Nginx (Reverse Proxy on port 80)
↓
Node.js API (Express)
↓
MongoDB
## 📡 API Endpoints

| Method | Endpoint          | What it does          |
|--------|-------------------|-----------------------|
| GET    | `/todos`          | Get all todos         |
| POST   | `/todos`          | Create a new todo     |
| GET    | `/todos/:id`      | Get one todo          |
| PUT    | `/todos/:id`      | Update a todo         |
| DELETE | `/todos/:id`      | Delete a todo         |


📘 API Documentation
Just open this in your browser after running the app:
http://localhost/api-docs
You can test every endpoint directly — no Postman needed!
📁 Project Structure
texttodo-app-docker/
├── src/
│   ├── models/Todo.js
│   ├── routes/todoRoutes.js
│   ├── server.js
│   └── swagger.js
├── nginx/
│   └── nginx.conf
├── docker-compose.yml
├── Dockerfile
├── package.json
└── README.md
⚙️ How to Run Locally (2 minutes!)
Prerequisites:

Docker & Docker Compose (only thing you need!)

Bash# 1. Clone it
git clone git@github.com:darkspywarrior/Multi-Container-Application.git
cd todo-app-docker

# 2. Start everything
docker compose up --build
That's it! The app will be live at:
http://localhost (thanks to Nginx)
Swagger docs: http://localhost/api-docs
Stop everything with:
Bashdocker compose down
🤝 Want to Contribute? (I would love it!)
This is a student project and I'm actively looking for contributions to make my portfolio even stronger!
Super beginner-friendly.
How to help:

Fork the repo
Create a branch: git checkout -b cool-feature
Add something awesome (new feature, bug fix, better styling, tests, etc.)
Commit & push
Open a Pull Request

Good first issues ideas:

Add user registration/login (JWT)
Make it look prettier (React frontend?)
Add dark mode
Write unit tests
Improve error messages

