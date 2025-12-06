package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskPriority;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskController using BDD (Behavior-Driven Development) approach.
 * Tests follow Given-When-Then pattern.
 */
@DisplayName("TaskController")
class TaskControllerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskController taskController;

    private User testUser;
    private Task testTask;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        initializeTestData();
    }

    private void initializeTestData() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
        testTask.setCreatedBy(testUser);
        testTask.setAssignedTo(testUser);

        authentication = new UsernamePasswordAuthenticationToken(
                testUser.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Nested
    @DisplayName("GET /api/tasks - List all tasks")
    class GetAllTasks {

        @Test
        @DisplayName("Should return all tasks")
        void givenTasksExist_whenGetAll_thenReturnAllTasks() {
            // Given
            List<Task> tasks = Arrays.asList(testTask);
            when(taskRepository.findAll()).thenReturn(tasks);

            // When
            List<Task> result = taskController.getAll();

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(1)
                    .contains(testTask);
            verify(taskRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no tasks exist")
        void givenNoTasksExist_whenGetAll_thenReturnEmptyList() {
            // Given
            when(taskRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<Task> result = taskController.getAll();

            // Then
            assertThat(result)
                    .isNotNull()
                    .isEmpty();
            verify(taskRepository).findAll();
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/{id} - Get task by ID")
    class GetTaskById {

        @Test
        @DisplayName("Should return task when it exists")
        void givenTaskExists_whenGetById_thenReturnTask() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

            // When
            ResponseEntity<Task> response = taskController.getById(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(testTask);
            verify(taskRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return 404 when task not found")
        void givenTaskDoesNotExist_whenGetById_thenReturnNotFound() {
            // Given
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            ResponseEntity<Task> response = taskController.getById(999L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
            verify(taskRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/my - Get authenticated user's tasks")
    class GetMyTasks {

        @Test
        @DisplayName("Should return user's tasks when authenticated")
        void givenAuthenticatedUser_whenGetMyTasks_thenReturnUsersTasks() {
            // Given
            List<Task> userTasks = Arrays.asList(testTask);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(taskRepository.findByAssignedTo(testUser)).thenReturn(userTasks);

            // When
            ResponseEntity<?> response = taskController.getMyTasks(authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .isInstanceOf(List.class)
                    .isEqualTo(userTasks);
            verify(userRepository).findByUsername("testuser");
            verify(taskRepository).findByAssignedTo(testUser);
        }

        @Test
        @DisplayName("Should return all tasks when user is not authenticated")
        void givenUnauthenticatedUser_whenGetMyTasks_thenReturnAllTasks() {
            // Given
            List<Task> allTasks = Arrays.asList(testTask);
            when(taskRepository.findAll()).thenReturn(allTasks);

            // When
            ResponseEntity<?> response = taskController.getMyTasks(null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(allTasks);
            verify(taskRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when user has no tasks")
        void givenAuthenticatedUserWithNoTasks_whenGetMyTasks_thenReturnEmptyList() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(taskRepository.findByAssignedTo(testUser)).thenReturn(Collections.emptyList());

            // When
            ResponseEntity<?> response = taskController.getMyTasks(authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .isInstanceOf(List.class)
                    .isEqualTo(Collections.emptyList());
        }
    }

    @Nested
    @DisplayName("POST /api/tasks - Create new task")
    class CreateTask {

        @Test
        @DisplayName("Should create task and assign to authenticated user")
        void givenValidTaskData_whenCreate_thenTaskIsCreatedAndAssigned() {
            // Given
            Task newTask = new Task();
            newTask.setTitle("New Task");
            newTask.setDescription("New Description");
            newTask.setStatus(TaskStatus.TODO);
            newTask.setPriority(TaskPriority.HIGH);

            Task savedTask = new Task();
            savedTask.setId(2L);
            savedTask.setTitle(newTask.getTitle());
            savedTask.setDescription(newTask.getDescription());
            savedTask.setStatus(newTask.getStatus());
            savedTask.setPriority(newTask.getPriority());
            savedTask.setCreatedAt(LocalDateTime.now());
            savedTask.setUpdatedAt(LocalDateTime.now());
            savedTask.setCreatedBy(testUser);
            savedTask.setAssignedTo(testUser);

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            ResponseEntity<Task> response = taskController.create(newTask, authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getHeaders().getLocation())
                    .isEqualTo(URI.create("/api/tasks/" + savedTask.getId()));
            assertThat(response.getBody())
                    .isNotNull()
                    .satisfies(task -> {
                        assertThat(task.getCreatedBy()).isEqualTo(testUser);
                        assertThat(task.getAssignedTo()).isEqualTo(testUser);
                        assertThat(task.getCreatedAt()).isNotNull();
                        assertThat(task.getUpdatedAt()).isNotNull();
                    });
            verify(userRepository).findByUsername("testuser");
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("Should set creation timestamp when not provided")
        void givenTaskWithoutTimestamp_whenCreate_thenTimestampIsSet() {
            // Given
            Task newTask = new Task();
            newTask.setTitle("New Task");
            newTask.setCreatedAt(null);

            Task savedTask = new Task();
            savedTask.setId(2L);
            savedTask.setTitle(newTask.getTitle());
            savedTask.setCreatedAt(LocalDateTime.now());
            savedTask.setUpdatedAt(LocalDateTime.now());

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            ResponseEntity<Task> response = taskController.create(newTask, authentication);

            // Then
            assertThat(response.getBody().getCreatedAt()).isNotNull();
            assertThat(response.getBody().getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should create task without authentication")
        void givenNoAuthentication_whenCreate_thenTaskIsCreated() {
            // Given
            Task newTask = new Task();
            newTask.setTitle("New Task");

            Task savedTask = new Task();
            savedTask.setId(2L);
            savedTask.setTitle(newTask.getTitle());
            savedTask.setCreatedAt(LocalDateTime.now());
            savedTask.setUpdatedAt(LocalDateTime.now());

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            ResponseEntity<Task> response = taskController.create(newTask, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("Should respect existing assignedTo field if set")
        void givenTaskWithAssignee_whenCreate_thenAssigneeIsPreserved() {
            // Given
            User otherUser = new User();
            otherUser.setId(2L);
            otherUser.setUsername("otheruser");

            Task newTask = new Task();
            newTask.setTitle("Assigned Task");
            newTask.setAssignedTo(otherUser);

            Task savedTask = new Task();
            savedTask.setId(2L);
            savedTask.setTitle(newTask.getTitle());
            savedTask.setAssignedTo(otherUser);
            savedTask.setCreatedAt(LocalDateTime.now());
            savedTask.setUpdatedAt(LocalDateTime.now());
            savedTask.setCreatedBy(testUser);

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            ResponseEntity<Task> response = taskController.create(newTask, authentication);

            // Then
            assertThat(response.getBody().getAssignedTo()).isEqualTo(otherUser);
        }
    }
}
