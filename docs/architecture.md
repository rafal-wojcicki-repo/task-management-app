# Task Management Application - Technical Architecture

## System Architecture

The Task Management Application follows a layered architecture pattern with clear separation of concerns:

### Architectural Layers

1. **Presentation Layer**
   - REST Controllers
   - Request/Response DTOs
   - Exception Handling
   - Input Validation

2. **Business Logic Layer**
   - Services
   - Domain Logic
   - Security

3. **Data Access Layer**
   - Repositories
   - Entity Models
   - Data Mapping

### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Applications                    │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                        API Gateway                          │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                  │
│                                                             │
│  ┌─────────────────┐    ┌─────────────────┐    ┌──────────┐ │
│  │  Auth Service   │    │  Task Service   │    │  User    │ │
│  │                 │    │                 │    │ Service  │ │
│  └────────┬────────┘    └────────┬────────┘    └────┬─────┘ │
│           │                      │                  │       │
│           └──────────────┬───────┴───────────┬──────┘       │
│                          │                   │              │
│                          ▼                   ▼              │
│             ┌─────────────────────┐ ┌─────────────────────┐ │
│             │  Spring Security    │ │    Spring Data JPA  │ │
│             └──────────┬──────────┘ └──────────┬──────────┘ │
│                        │                       │            │
└────────────────────────┼───────────────────────┼────────────┘
                         │                       │
                         ▼                       ▼
                ┌─────────────────────────────────────┐
                │              Database               │
                └─────────────────────────────────────┘
```

## Detailed Component Design

### Authentication and Authorization

#### JWT Token Structure

```json
{
  "header": {
    "alg": "HS512",
    "typ": "JWT"
  },
  "payload": {
    "sub": "username",
    "roles": ["ROLE_USER"],
    "iat": 1627984882,
    "exp": 1628071282
  },
  "signature": "..."
}
```

#### Security Flow

1. **Authentication Process**:
   ```
   Client → Login Request → AuthController → AuthService → UserDetailsService → 
   → Password Encoder (validation) → JWT Generator → Token Response
   ```

2. **Authorization Process**:
   ```
   Client → Request with JWT → JwtAuthFilter → JwtTokenProvider (validation) → 
   → UserDetailsService → Spring Security Context → Controller
   ```

### Task Management

#### Task Lifecycle

```
CREATE → TODO → IN_PROGRESS → REVIEW → DONE
```

#### Task Priority Levels

- LOW
- MEDIUM
- HIGH
- URGENT

## Database Schema

### Entity Relationship Diagram

```
┌───────────────┐       ┌───────────────┐       ┌───────────────┐
│     User      │       │  User_Roles   │       │     Role      │
├───────────────┤       ├───────────────┤       ├───────────────┤
│ id            │       │ user_id       │◄──────┤ id            │
│ username      │       │ role_id       │       │ name          │
│ email         │◄──────┤               │       │               │
│ password      │       │               │       │               │
└───────┬───────┘       └───────────────┘       └───────────────┘
        │
        │
        │
┌───────▼───────┐
│     Task      │
├───────────────┤
│ id            │
│ title         │
│ description   │
│ status        │
│ priority      │
│ due_date      │
│ created_at    │
│ updated_at    │
│ assigned_to   │
│ created_by    │
└───────────────┘
```

## API Design

### RESTful Endpoints

#### Authentication API

- `POST /api/auth/signup` - Register a new user
- `POST /api/auth/signin` - Authenticate and get JWT token
- `POST /api/auth/refreshtoken` - Refresh JWT token

#### Task API

- `GET /api/tasks` - Get all tasks (with filtering)
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/tasks` - Create a new task
- `PUT /api/tasks/{id}` - Update a task
- `DELETE /api/tasks/{id}` - Delete a task
- `PATCH /api/tasks/{id}/status` - Update task status
- `PATCH /api/tasks/{id}/assign` - Assign task to user

#### User API

- `GET /api/users` - Get all users (admin only)
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user (admin only)

## Security Considerations

### Password Storage

- Passwords are stored using BCrypt hashing algorithm
- Salt is automatically generated for each password

### JWT Security

- Tokens are signed with HS512 algorithm
- Short expiration time (24 hours)
- Sensitive data is not stored in JWT payload

### API Security

- HTTPS is enforced in production
- CORS is configured to allow only trusted origins
- Input validation is performed on all endpoints
- Rate limiting is applied to authentication endpoints

## Testing Strategy

### Test Pyramid

1. **Unit Tests**
   - Service layer tests
   - Repository tests
   - Utility class tests

2. **Integration Tests**
   - Controller tests with MockMvc
   - Repository tests with test database
   - Security integration tests

3. **End-to-End Tests**
   - API tests with real HTTP requests
   - Authentication flow tests
   - Task management flow tests

## Deployment Architecture

### Docker Containers

```
┌─────────────────────────────────────────────────────────────┐
│                     Docker Compose                           │
│                                                             │
│  ┌─────────────────┐    ┌─────────────────┐                 │
│  │  Spring Boot    │    │    Database     │                 │
│  │  Application    │◄───┤    Container    │                 │
│  │   Container     │    │                 │                 │
│  └─────────────────┘    └─────────────────┘                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### CI/CD Pipeline

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Commit  │───►│  Build   │───►│   Test   │───►│ Package  │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                                                      │
┌──────────┐    ┌──────────┐    ┌──────────┐          │
│Production│◄───│  Deploy  │◄───│ Registry │◄─────────┘
└──────────┘    └──────────┘    └──────────┘
```

## Performance Considerations

- Database indexing on frequently queried fields
- Pagination for list endpoints
- Caching for frequently accessed data
- Connection pooling for database access

## Monitoring and Logging

- Structured logging with SLF4J
- Centralized log collection
- Health check endpoints
- Metrics collection with Micrometer
- Tracing with Spring Cloud Sleuth (in future versions)

## Future Enhancements

1. **Microservices Architecture**
   - Split into separate services for Auth, Tasks, and Users
   - Implement API Gateway
   - Add service discovery

2. **Advanced Features**
   - Task comments and attachments
   - Team management
   - Notifications system
   - Reporting and analytics

3. **Technical Improvements**
   - Reactive programming with Spring WebFlux
   - GraphQL API
   - Event-driven architecture with Kafka
   - Distributed caching