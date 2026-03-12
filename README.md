# Multi-Container-Application
[Multi-Container Application Project](https://roadmap.sh/projects/multi-container-service)

A simple full-stack Todo List application using **Docker Compose** with a **Node.js + Express + Mongoose** backend and **MongoDB** database.

This project demonstrates:

- Multi-container Docker setup with Docker Compose
- Persistent data storage using Docker volumes
- Local development with hot-reloading (nodemon)
- Production-grade deployment preparation (remote server + CI/CD)
- (Bonus) Reverse proxy setup with Nginx

## Features

- REST API for managing todos
- MongoDB persistence
- Dockerized services
- Ready for cloud deployment (DigitalOcean, AWS, etc.)

## API Endpoints

| Method | Endpoint          | Description                     | Body (JSON)                     |
|--------|-------------------|---------------------------------|---------------------------------|
| GET    | `/todos`          | Get all todos                   | —                               |
| POST   | `/todos`          | Create a new todo               | `{ "title": "Buy milk", "completed": false }` |
| GET    | `/todos/:id`      | Get a single todo by ID         | —                               |
| PUT    | `/todos/:id`      | Update a todo                   | `{ "title": "...", "completed": true }` |
| DELETE | `/todos/:id`      | Delete a todo                   | —                               |

## Project Requirements

### Requirement 1 – Dockerize the Application

- Create a **Node.js + Express** REST API
- Use **Mongoose** to connect to MongoDB
- Use **nodemon** for development
- Write a `Dockerfile` for the API
- Create a `docker-compose.yml` that runs:
  - `api` service (Node.js)
  - `mongo` service (MongoDB)
- Use **named volume** for MongoDB data persistence
- API should be available at: `http://localhost:3000`

### Requirement 2 – Remote Server Setup (Infrastructure as Code)

- Provision a remote server using **Terraform** (DigitalOcean, AWS EC2, etc.)
- Use **Ansible** to:
  - Install Docker & Docker Compose
  - Copy necessary files / environment variables
  - Pull images from Docker Hub (if using)
  - Start the application with `docker-compose up -d`

### Requirement 3 – CI/CD Pipeline

- Push code to **GitHub**
- Create **GitHub Actions** workflow that:
  - Builds and pushes Docker image to Docker Hub (optional)
  - SSH into the remote server
  - Pulls latest code / image
  - Runs `docker-compose up -d` (or similar)

### Bonus – Reverse Proxy with Nginx

- Add an **Nginx** service in `docker-compose.yml`
- Configure Nginx to proxy requests to the Node.js API
- Access the application via domain name[](http://your-domain.com)

## Project Structure (Recommended)
Setup a reverse proxy using Nginx to allow you to access the application via http://your_domain.com. You should use docker-compose to setup the reverse proxy.

After completing this project, you will have a good understanding of Docker Compose, multi-container applications, CI/CD pipelines, and more.

