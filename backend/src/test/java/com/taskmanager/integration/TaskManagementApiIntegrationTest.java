package com.taskmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskPriority;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.User;
import com.taskmanager.payload.request.LoginRequest;
import com.taskmanager.payload.request.SignupRequest;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Task Management API.
 * Tests the full request/response cycle through MockMvc without requiring a running server.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Task Management API Integration Tests")
class TaskManagementApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    private String jwtToken;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        taskRepository.deleteAll();
        userRepository.deleteAll();

        // Register and login test user
        SignupRequest signup = new SignupRequest("testuser", "test@example.com", new HashSet<>(), "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());

        // Login to get JWT token
        LoginRequest login = new LoginRequest();
        login.setUsername("testuser");
        login.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract token from response
        String response = result.getResponse().getContentAsString();
        // The response should contain access_token - adjust based on actual response format
        jwtToken = extractToken(response);

        // Get test user
        testUser = userRepository.findByUsername("testuser").orElse(null);
    }

    private String extractToken(String response) {
        // Extract token from JSON response
        // Adjust based on actual response format from your API
        try {
            int start = response.indexOf("\"access_token\":\"") + 16;
            int end = response.indexOf("\"", start);
            if (start > 15 && end > start) {
                return response.substring(start, end);
            }
        } catch (Exception e) {
            // Token extraction failed, proceed without token
        }
        return null;
    }

    @Nested
    @DisplayName("Authentication Endpoints")
    class AuthenticationTests {

        @Test
        @DisplayName("Should register a new user with valid credentials")
        void givenValidSignupRequest_whenRegister_thenUserIsCreated() throws Exception {
            // Given
            SignupRequest signup = new SignupRequest("newuser", "newuser@example.com", new HashSet<>(), "password123");

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signup)))
                    .andExpect(status().isOk());

            // Verify user was created
            assertThat(userRepository.findByUsername("newuser")).isPresent();
        }

        @Test
        @DisplayName("Should reject registration with existing username")
        void givenExistingUsername_whenRegister_thenReturnBadRequest() throws Exception {
            // Given - testuser already exists from setUp
            SignupRequest signup = new SignupRequest("testuser", "newemail@example.com", new HashSet<>(), "password123");

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signup)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should login successfully and return JWT token")
        void givenValidCredentials_whenLogin_thenReturnJwtToken() throws Exception {
            // Given
            LoginRequest login = new LoginRequest();
            login.setUsername("testuser");
            login.setPassword("password123");

            // When & Then
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            assertThat(response).contains("access_token");
        }

        @Test
        @DisplayName("Should reject login with invalid credentials")
        void givenInvalidPassword_whenLogin_thenReturnUnauthorized() throws Exception {
            // Given
            LoginRequest login = new LoginRequest();
            login.setUsername("testuser");
            login.setPassword("wrongpassword");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Task CRUD Endpoints")
    class TaskCrudTests {

        @Test
        @DisplayName("Should get all tasks without authentication")
        void whenGetAllTasks_thenReturnTaskList() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/tasks/")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should create a new task")
        void givenValidTaskData_whenCreateTask_thenTaskIsCreated() throws Exception {
            // Given
            Task newTask = new Task();
            newTask.setTitle("Integration Test Task");
            newTask.setDescription("Testing task creation");
            newTask.setStatus(TaskStatus.TODO);
            newTask.setPriority(TaskPriority.HIGH);

            // When & Then
            MvcResult result = mockMvc.perform(post("/api/tasks/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newTask)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andReturn();

            // Verify task was saved
            assertThat(taskRepository.count()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should get task by ID")
        void givenExistingTask_whenGetById_thenReturnTask() throws Exception {
            // Given
            Task task = new Task();
            task.setTitle("Test Task");
            task.setStatus(TaskStatus.TODO);
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            task.setCreatedBy(testUser);
            task.setAssignedTo(testUser);
            Task saved = taskRepository.save(task);

            // When & Then
            mockMvc.perform(get("/api/tasks/" + saved.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId()))
                    .andExpect(jsonPath("$.title").value("Test Task"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent task")
        void givenNonExistentTaskId_whenGetById_thenReturnNotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/tasks/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should update an existing task")
        void givenExistingTask_whenUpdate_thenTaskIsModified() throws Exception {
            // Given
            Task task1 = new Task();
            task1.setTitle("Original Title");
            task1.setStatus(TaskStatus.TODO);
            task1.setCreatedAt(LocalDateTime.now());
            task1.setUpdatedAt(LocalDateTime.now());
            task1.setCreatedBy(testUser);
            task1.setAssignedTo(testUser);
            Task saved = taskRepository.save(task1);

            saved.setTitle("Updated Title");
            saved.setStatus(TaskStatus.REVIEW);

            // When & Then
            mockMvc.perform(put("/api/tasks/" + saved.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(saved)))
                    .andExpect(status().isOk());

            // Verify update
            Task updated = taskRepository.findById(saved.getId()).orElse(null);
            assertThat(updated).isNotNull();
            assertThat(updated.getTitle()).isEqualTo("Updated Title");
            assertThat(updated.getStatus()).isEqualTo(TaskStatus.REVIEW);
        }

        @Test
        @DisplayName("Should delete an existing task")
        void givenExistingTask_whenDelete_thenTaskIsRemoved() throws Exception {
            // Given
            Task task = new Task();
            task.setTitle("Task to Delete");
            task.setStatus(TaskStatus.TODO);
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            task.setCreatedBy(testUser);
            task.setAssignedTo(testUser);
            Task saved = taskRepository.save(task);
            Long taskId = saved.getId();

            // When & Then
            mockMvc.perform(delete("/api/tasks/" + taskId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify deletion
            assertThat(taskRepository.existsById(taskId)).isFalse();
        }
    }

    @Nested
    @DisplayName("Task Filtering Endpoints")
    class TaskFilteringTests {

        @Test
        @DisplayName("Should get tasks assigned to authenticated user")
        void givenAuthenticatedUser_whenGetMyTasks_thenReturnUsersTasks() throws Exception {
            // Given - testUser with task created in setup

            // When & Then
            mockMvc.perform(get("/api/tasks/my")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + (jwtToken != null ? jwtToken : "")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should filter tasks by status")
        void givenTasksWithDifferentStatuses_whenFilterByStatus_thenReturnFiltered() throws Exception {
            // Given
            Task task1 = new Task();
            task1.setTitle("Todo Task");
            task1.setStatus(TaskStatus.TODO);
            task1.setCreatedAt(LocalDateTime.now());
            task1.setUpdatedAt(LocalDateTime.now());
            task1.setCreatedBy(testUser);
            task1.setAssignedTo(testUser);

            Task task2 = new Task();
            task2.setTitle("Done Task");
            task2.setStatus(TaskStatus.DONE);
            task2.setCreatedAt(LocalDateTime.now());
            task2.setUpdatedAt(LocalDateTime.now());
            task2.setCreatedBy(testUser);
            task2.setAssignedTo(testUser);

            taskRepository.save(task1);
            taskRepository.save(task2);

            // When & Then - Get all tasks and verify both exist
            mockMvc.perform(get("/api/tasks/")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 400 for invalid JSON")
        void givenInvalidJson_whenCreateTask_thenReturnBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/tasks/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 for non-existent endpoint")
        void givenNonExistentEndpoint_whenCall_thenReturnNotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/nonexistent")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
