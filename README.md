# Task Management App

## Prerequisites

Before running the application, ensure you have the following installed:

- Java 17 JDK
- Maven

## How to Run the Application

### Using Docker (Recommended)

1. Ensure Docker and Docker Compose are installed on your system.
2. Double-click the `run-docker.bat` file in the root directory, or run:
   ```
   docker-compose up -d
   ```
3. Access the application at http://localhost

For more details on the Docker setup, see [Docker README](DOCKER_README.md).

### Using the Batch File (Windows)

1. Double-click the `run-app.bat` file in the root directory.
2. The script will build and run the application automatically.

### Manual Method

1. Open a command prompt or terminal.
2. Navigate to the backend directory:
   ```
   cd backend
   ```
3. Build the application:
   ```
   mvn clean package -DskipTests
   ```
4. Run the application:
   ```
   mvn spring-boot:run
   ```

## Accessing the Application

Once the application is running:

- API Endpoint: http://localhost:8080/api
- H2 Database Console: http://localhost:8080/api/h2-console
  - JDBC URL: jdbc:h2:mem:taskdb
  - Username: sa
  - Password: password
- Swagger UI: http://localhost:8080/api/swagger-ui.html

## Troubleshooting

If you encounter any issues:

1. Ensure Java 17 is installed and set as your JAVA_HOME.
2. Ensure Maven is installed and available in your PATH.
3. Check the application logs for specific error messages.
