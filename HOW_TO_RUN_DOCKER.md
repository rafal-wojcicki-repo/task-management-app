# How to Run the Task Management App with Docker Compose

This guide explains how to run the Task Management Application using Docker Compose. Docker Compose allows you to run both the frontend and backend services with a single command.

## Prerequisites
- Docker Engine (version 19.03.0+)
- Docker Compose (version 1.27.0+)
- **IMPORTANT**: Make sure Docker Desktop is running on your system before executing any Docker commands

## Method 1: Using the Batch File (Windows)
1. Open the project folder in File Explorer
2. Double-click the `run-docker.bat` file
3. Wait for the containers to build and start
4. Access the application at http://localhost

## Method 2: Using Command Line
1. Open Command Prompt or PowerShell
2. Navigate to the project root directory:
   ```
   cd path\to\task-management-app
   ```
3. Run the following command:
   ```
   docker-compose up -d
   ```
4. Access the application at http://localhost

## Checking Container Status
```
docker-compose ps
```

## Viewing Logs
```
docker-compose logs
```

## Stopping the Application
```
docker-compose down
```

## Troubleshooting Common Issues

### Docker is not installed or running
- Ensure Docker Desktop is installed and running
- If you see an error about "//./pipe/dockerDesktopLinuxEngine", this specifically means Docker Desktop is not running
- Start Docker Desktop from your Start menu or system tray
- Check Docker installation with: `docker --version`
- Check Docker Compose installation with: `docker-compose --version`

### Port conflicts
- If ports 80 or 8080 are already in use, you'll see an error
- Stop any services using these ports or modify the port mappings in docker-compose.yml

### Container fails to start
- Check the logs: `docker-compose logs`
- For specific service logs: `docker-compose logs backend` or `docker-compose logs frontend`

### Application is not accessible
- Ensure containers are running: `docker-compose ps`
- Check if the frontend container is properly connected to the backend
- Try rebuilding the containers: `docker-compose up -d --build`

### Changes to code not reflected
- Rebuild the containers: `docker-compose up -d --build`
- If using volumes, ensure they're properly configured