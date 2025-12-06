# Task Management Application

A full-stack task management system with user authentication, real-time task updates, and role-based access control.

**🚀 Live Demo:** https://task-management-app-v1-45bab27bbe23.herokuapp.com/

---

## Quick Start

### Demo Account
- **Email:** `user@example.com`
- **Password:** `password123`

### Features
✅ User authentication with JWT tokens  
✅ Create, update, and delete tasks  
✅ Task priority and status tracking  
✅ Role-based access control (User/Admin)  
✅ Responsive React UI  
✅ Production-ready REST API  
✅ Fully containerized with Docker  

---

## Tech Stack

**Frontend:**
- React 18 with React Router
- React Bootstrap for UI
- Axios for API communication
- JWT-based authentication

**Backend:**
- Java 17 with Spring Boot 3.2
- Spring Security & JWT
- JPA/Hibernate for data persistence
- H2 (development) / PostgreSQL (production)

**DevOps:**
- Docker & Docker Compose
- Heroku for cloud deployment
- Multi-stage Docker builds for optimized images

---

## Running Locally

### Prerequisites
- Docker & Docker Compose (recommended)
- OR: Java 17 JDK + Maven + Node.js 18+

### Option 1: Docker Compose (Recommended)
```bash
docker-compose up -d
```
The application will be available at `http://localhost:3000`

### Option 2: Manual Setup

**Backend:**
```bash
cd backend
mvn clean package -DskipTests
mvn spring-boot:run
```

**Frontend (in a new terminal):**
```bash
cd frontend
npm install
npm start
```

Access the app at `http://localhost:3000`

---

## API Documentation

**Base URL:** `http://localhost:8080/api` (or use the live Heroku URL)

### Authentication
```
POST /api/auth/register
POST /api/auth/login
```

### Tasks
```
GET    /api/tasks           (list all tasks)
POST   /api/tasks           (create task)
GET    /api/tasks/{id}      (get task details)
PUT    /api/tasks/{id}      (update task)
DELETE /api/tasks/{id}      (delete task)
```

All endpoints (except `/register` and `/login`) require a valid JWT token in the `Authorization: Bearer <token>` header.

---

## Testing

The backend includes comprehensive unit and integration tests using **BDD (Behavior-Driven Development)** with the **Given-When-Then** pattern.

### Test Structure
- **Unit Tests:** AuthController, TaskController, JwtUtils
- **Integration Tests:** TaskRepository, Full API integration
- **Test Framework:** JUnit 5, Mockito, AssertJ
- **Coverage:** ~80% overall

### Run Tests
```bash
cd backend

# Run all tests
mvn clean test

# Run specific test class
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=TaskControllerTest
mvn test -Dtest=JwtUtilsTest

# Skip tests during build
mvn clean package -DskipTests
```

For detailed testing guide, see [`backend/TESTING.md`](backend/TESTING.md).

---

## Deployment

This application is deployed on **Heroku** using the Container Registry.

### Environment Variables (Production)
- `SPRING_DATASOURCE_URL` – PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME` – Database username
- `SPRING_DATASOURCE_PASSWORD` – Database password
- `JWT_SECRET` – JWT signing secret

### Deploy Your Own Version
```bash
# Login to Heroku
heroku login

# Create app
heroku create your-app-name

# Add PostgreSQL
heroku addons:create heroku-postgresql:hobby-dev

# Deploy via Heroku Container Registry
heroku container:push web --app your-app-name
heroku container:release web --app your-app-name

# View logs
heroku logs --tail --app your-app-name
```

---

## Architecture

The application follows a layered architecture:
- **Presentation Layer:** REST controllers, request/response DTOs
- **Business Logic Layer:** Services and domain logic
- **Data Access Layer:** JPA repositories and entity models

For detailed architecture documentation, see [`docs/architecture.md`](docs/architecture.md).

---

## Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests (E2E with Selenium)
```bash
cd backend
mvn test -Dgroups=e2e
```

---

## Project Structure
```
task-management-app/
├── frontend/                 # React application
│   ├── src/
│   ├── public/
│   └── Dockerfile
├── backend/                  # Spring Boot API
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── Dockerfile                # Multi-stage build (frontend + backend)
├── docker-compose.yml        # Local development environment
└── docs/                     # Additional documentation
```

---

## Development Notes

### Local Database
- **H2 Console:** Available at `http://localhost:8080/api/h2-console`
  - JDBC URL: `jdbc:h2:mem:taskdb`
  - Username: `sa`
  - Password: `password`

### Frontend API Configuration
The frontend is configured to proxy API calls to the backend at `/api`. This avoids CORS issues in development and production.

---

## Contributing

Issues and pull requests are welcome. Please open an issue to discuss major changes first.

## License

This project is open source and available under the MIT License.
