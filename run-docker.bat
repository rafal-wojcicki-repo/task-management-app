@echo off
echo Starting Task Management App with Docker...
echo.
echo Note: This script assumes you have Docker and Docker Compose installed.
echo If you encounter any issues, please ensure Docker is properly installed and running.
echo.

REM Check if Docker is running
docker info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Docker is not running. Please start Docker Desktop and try again.
    pause
    exit /b 1
)

echo Docker is running. Starting containers...
docker-compose down
docker-compose rm -f
docker-compose build --no-cache
docker-compose up

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Docker build failed. Please check the error messages above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Docker containers started successfully!
echo.
echo You can access the application at:
echo - Frontend: http://localhost
echo - Backend API: http://localhost/api
echo - Swagger UI: http://localhost/api/swagger-ui.html
echo.
echo To stop the containers, run: docker-compose down
echo.
pause