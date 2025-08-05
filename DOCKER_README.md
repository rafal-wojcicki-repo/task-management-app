# Docker Setup for Task Management App

This document explains how to run the Task Management Application using Docker.

## Prerequisites

Before running the application with Docker, ensure you have the following installed:

- Docker Engine (version 19.03.0+)
- Docker Compose (version 1.27.0+)

## Running the Application with Docker

1. Clone the repository (if you haven't already):
   ```
   git clone <repository-url>
   cd task-management-app
   ```

2. Build and start the containers:
   ```
   docker-compose up -d
   ```

   This command will:
   - Build the backend and frontend Docker images
   - Start the containers in detached mode
   - Create a network for communication between services

3. Access the application:
   - Frontend: http://localhost
   - Backend API: http://localhost/api
   - Swagger UI: http://localhost/api/swagger-ui.html
   - H2 Database Console: http://localhost/api/h2-console (JDBC URL: jdbc:h2:mem:taskdb, Username: sa, Password: password)

## Docker Components

The Docker setup consists of the following components:

1. **Backend Container**:
   - Spring Boot application running on Java 17
   - Exposes port 8080
   - Uses H2 in-memory database

2. **Frontend Container**:
   - React application served by Nginx
   - Exposes port 80
   - Proxies API requests to the backend

## Stopping the Application

To stop the application:

```
docker-compose down
```

To stop and remove all containers, networks, and volumes:

```
docker-compose down -v
```

## Troubleshooting

If you encounter any issues:

1. Check container logs:
   ```
   docker-compose logs
   ```

   For a specific service:
   ```
   docker-compose logs backend
   docker-compose logs frontend
   ```

2. Ensure all containers are running:
   ```
   docker-compose ps
   ```

3. If you need to rebuild the images:
   ```
   docker-compose build --no-cache
   ```

## Development with Docker

For development purposes, you can make changes to the code and rebuild the containers:

1. Make changes to the source code
2. Rebuild and restart the containers:
   ```
   docker-compose up -d --build
   ```