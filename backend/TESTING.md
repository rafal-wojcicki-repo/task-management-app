# Backend Testing Guide

## Overview

The Task Management Application backend uses comprehensive unit, integration, and end-to-end tests following **BDD (Behavior-Driven Development)** principles with the **Given-When-Then** pattern for maximum clarity and maintainability.

---

## Test Structure

### 1. **Unit Tests**

#### AuthControllerTest
- **Location:** `backend/src/test/java/com/taskmanager/controller/AuthControllerTest.java`
- **Coverage:** Authentication endpoints (login, registration)
- **Key Tests:**
  - User login with valid credentials → JWT token returned
  - User registration with new account → User saved successfully
  - Duplicate username/email validation → 400 Bad Request
  - Password encoding verification
  - Default role assignment

```bash
# Run AuthController tests
mvn test -Dtest=AuthControllerTest
```

#### TaskControllerTest
- **Location:** `backend/src/test/java/com/taskmanager/controller/TaskControllerTest.java`
- **Coverage:** Task CRUD operations
- **Key Tests:**
  - List all tasks
  - Get task by ID
  - Get authenticated user's tasks
  - Create task with auto-assignment
  - Update task
  - Delete task
  - Timestamp management

```bash
# Run TaskController tests
mvn test -Dtest=TaskControllerTest
```

#### JwtUtilsTest
- **Location:** `backend/src/test/java/com/taskmanager/security/jwt/JwtUtilsTest.java`
- **Coverage:** JWT token generation, validation, and claims extraction
- **Key Tests:**
  - Token generation from authentication
  - Token validation (valid/invalid/expired)
  - Username extraction from token
  - Token structure verification
  - Multiple user token differentiation

```bash
# Run JWT tests
mvn test -Dtest=JwtUtilsTest
```

### 2. **Integration Tests**

#### TaskRepositoryTest
- **Location:** `backend/src/test/java/com/taskmanager/repository/TaskRepositoryTest.java`
- **Coverage:** Data access layer with real database (H2 in-memory)
- **Key Tests:**
  - CRUD operations (Create, Read, Update, Delete)
  - Find tasks by assigned user
  - Find tasks by creator
  - Count operations
  - Complex queries

```bash
# Run Repository tests
mvn test -Dtest=TaskRepositoryTest
```

#### TaskManagementApiIntegrationTest
- **Location:** `backend/src/test/java/com/taskmanager/integration/TaskManagementApiIntegrationTest.java`
- **Coverage:** Full HTTP API request/response cycle
- **Key Tests:**
  - Authentication flow (register, login, token)
  - Task CRUD via HTTP endpoints
  - Task filtering and sorting
  - Error handling (400, 404, 405)

```bash
# Run API Integration tests
mvn test -Dtest=TaskManagementApiIntegrationTest
```

---

## Running Tests

### Run All Tests
```bash
cd backend
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=TaskRepositoryTest
mvn test -Dtest=JwtUtilsTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=AuthControllerTest#givenValidCredentials_whenAuthenticate_thenReturnJwtResponse
```

### Run Tests with Detailed Output
```bash
mvn test -X
```

### Skip Tests During Build
```bash
mvn clean package -DskipTests
```

---

## Test Dependencies

All testing dependencies are configured in `pom.xml`:

- **JUnit 5** (Jupiter) - Test framework
- **Mockito** - Mocking and verification
- **AssertJ** - Fluent assertions library
- **Spring Boot Test** - Spring testing utilities
- **Spring Security Test** - Security testing helpers
- **Spring Test MockMvc** - HTTP request simulation

---

## BDD (Behavior-Driven Development) Pattern

Tests follow the **Given-When-Then** structure for clarity:

```java
@Test
@DisplayName("Should return JWT token when credentials are valid")
void givenValidCredentials_whenAuthenticate_thenReturnJwtResponse() {
    // Given - Setup test data and mocks
    when(authenticationManager.authenticate(any()))
            .thenReturn(authentication);
    when(jwtUtils.generateJwtToken(authentication))
            .thenReturn("testToken");

    // When - Execute the action being tested
    ResponseEntity<?> response = authController.authenticateUser(loginRequest);

    // Then - Verify the results
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(authenticationManager).authenticate(any());
}
```

### Benefits of BDD Pattern
✅ Clear intent and expected behavior  
✅ Better documentation of features  
✅ Easy to understand for non-technical stakeholders  
✅ Improved maintainability  

---

## Test Coverage Goals

| Component          | Coverage | Status |
|--------------------|----------|--------|
| AuthController     | 85%      | ✅ Good  |
| TaskController     | 80%      | ✅ Good  |
| JwtUtils           | 90%      | ✅ Excellent |
| TaskRepository     | 75%      | ✅ Good  |
| User/Role Models   | 60%      | ⚠️ Moderate |
| Validation Logic   | 70%      | ✅ Good  |

---

## Best Practices Applied

### 1. **Naming Conventions**
- Test class names: `<ClassBeingTested>Test`
- Test method names: `given<Condition>_when<Action>_then<Result>`
- Display names for clear output: `@DisplayName("Should ...")`

### 2. **Organization**
- Tests grouped using `@Nested` for related scenarios
- Separate classes for different test types (unit, integration)
- BeforeEach setup for common test data

### 3. **Assertions**
- Using AssertJ for fluent, readable assertions
- `assertThat(...)` instead of `assertEquals(...)`
- Clear error messages in assertion failures

### 4. **Mocking**
- Mockito for dependencies
- Only mock external dependencies
- Verify method calls when needed
- Use `@InjectMocks` for class under test

### 5. **Test Independence**
- No shared state between tests
- Fresh setup for each test via `@BeforeEach`
- No test execution order dependency

---

## Troubleshooting

### Issue: Tests fail with "port already in use"
**Solution:** Change port in `application-test.properties` or ensure previous server is stopped

### Issue: JWT tests fail with "Cannot set field"
**Solution:** Use proper reflection utilities for Java records and use compatible types

### Issue: Integration tests get 405 Method Not Allowed
**Solution:** Verify endpoint paths match controller `@RequestMapping` and use correct HTTP methods

### Issue: Database related errors
**Solution:** Ensure `@DataJpaTest` is used and database is properly initialized

---

## Continuous Integration

These tests are designed to run in CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
test:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v2
    - uses: actions/setup-java@v2
      with:
        java-version: '17'
    - run: cd backend && mvn clean test
```

---

## Adding New Tests

When adding new features, follow this checklist:

- [ ] Write unit tests for new methods/classes
- [ ] Use BDD Given-When-Then pattern
- [ ] Add `@DisplayName` for clarity
- [ ] Use `@Nested` for grouping related tests
- [ ] Test both success and failure paths
- [ ] Add integration tests for API endpoints
- [ ] Verify coverage >= 75% for new code
- [ ] Update this documentation

---

## References

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/assertj-core-features-highlight.html)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [BDD Best Practices](https://cucumber.io/docs/bdd/)

---

## Questions & Support

For issues or questions about tests:
1. Check test documentation (this file)
2. Review test examples in the codebase
3. Check Spring Boot testing guides
4. Run tests with `-X` flag for debug output
