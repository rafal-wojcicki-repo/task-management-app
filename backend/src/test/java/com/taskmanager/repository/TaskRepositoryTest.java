package com.taskmanager.repository;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskPriority;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for TaskRepository using Spring Data JPA test slice.
 * Tests the data access layer with real database operations (in-memory H2).
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        taskRepository.deleteAll();
        userRepository.deleteAll();

        // Create and save test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        // Create and save test task
        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
        testTask.setCreatedBy(testUser);
        testTask.setAssignedTo(testUser);
        testTask = taskRepository.save(testTask);
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save a new task")
        void givenValidTask_whenSave_thenTaskIsPersisted() {
            // Given
            Task newTask = new Task();
            newTask.setTitle("New Task");
            newTask.setDescription("New Description");
            newTask.setStatus(TaskStatus.TODO);
            newTask.setPriority(TaskPriority.HIGH);
            newTask.setCreatedAt(LocalDateTime.now());
            newTask.setUpdatedAt(LocalDateTime.now());
            newTask.setCreatedBy(testUser);
            newTask.setAssignedTo(testUser);

            // When
            Task saved = taskRepository.save(newTask);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTitle()).isEqualTo("New Task");
            assertThat(taskRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should retrieve task by ID")
        void givenExistingTask_whenFindById_thenReturnTask() {
            // When
            Optional<Task> found = taskRepository.findById(testTask.getId());

            // Then
            assertThat(found)
                    .isPresent()
                    .contains(testTask);
        }

        @Test
        @DisplayName("Should update an existing task")
        void givenExistingTask_whenUpdate_thenTaskIsModified() {
            // Given
            testTask.setTitle("Updated Title");
            testTask.setStatus(TaskStatus.IN_PROGRESS);
            testTask.setUpdatedAt(LocalDateTime.now());

            // When
            Task updated = taskRepository.save(testTask);

            // Then
            assertThat(updated.getTitle()).isEqualTo("Updated Title");
            assertThat(updated.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Should delete a task")
        void givenExistingTask_whenDelete_thenTaskIsRemoved() {
            // Given
            Long taskId = testTask.getId();
            assertThat(taskRepository.existsById(taskId)).isTrue();

            // When
            taskRepository.deleteById(taskId);

            // Then
            assertThat(taskRepository.existsById(taskId)).isFalse();
            assertThat(taskRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return empty Optional when task not found")
        void givenNonExistentTaskId_whenFindById_thenReturnEmpty() {
            // When
            Optional<Task> found = taskRepository.findById(999L);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find by User")
    class FindByUser {

        @Test
        @DisplayName("Should find all tasks assigned to a user")
        void givenTasksAssignedToUser_whenFindByAssignedTo_thenReturnUsersTasks() {
            // Given
            Task anotherTask = new Task();
            anotherTask.setTitle("Another Task");
            anotherTask.setStatus(TaskStatus.TODO);
            anotherTask.setCreatedAt(LocalDateTime.now());
            anotherTask.setUpdatedAt(LocalDateTime.now());
            anotherTask.setCreatedBy(testUser);
            anotherTask.setAssignedTo(testUser);
            taskRepository.save(anotherTask);

            // When
            List<Task> tasks = taskRepository.findByAssignedTo(testUser);

            // Then
            assertThat(tasks)
                    .hasSize(2)
                    .allMatch(task -> task.getAssignedTo().equals(testUser));
        }

        @Test
        @DisplayName("Should return empty list when user has no assigned tasks")
        void givenUserWithNoTasks_whenFindByAssignedTo_thenReturnEmptyList() {
            // Given
            User userWithoutTasks = new User();
            userWithoutTasks.setUsername("nouser");
            userWithoutTasks.setEmail("nouser@example.com");
            userWithoutTasks.setPassword("password");
            userWithoutTasks = userRepository.save(userWithoutTasks);

            // When
            List<Task> tasks = taskRepository.findByAssignedTo(userWithoutTasks);

            // Then
            assertThat(tasks).isEmpty();
        }

        @Test
        @DisplayName("Should find tasks by creator")
        void givenTasksCreatedByUser_whenFindByCreatedBy_thenReturnUsersCreatedTasks() {
            // Given
            Task createdTask = new Task();
            createdTask.setTitle("Created Task");
            createdTask.setStatus(TaskStatus.TODO);
            createdTask.setCreatedAt(LocalDateTime.now());
            createdTask.setUpdatedAt(LocalDateTime.now());
            createdTask.setCreatedBy(testUser);
            createdTask.setAssignedTo(testUser);
            taskRepository.save(createdTask);

            // When
            List<Task> tasks = taskRepository.findByCreatedBy(testUser);

            // Then
            assertThat(tasks)
                    .isNotEmpty()
                    .allMatch(task -> task.getCreatedBy().equals(testUser));
        }
    }

    @Nested
    @DisplayName("Count Operations")
    class CountOperations {

        @Test
        @DisplayName("Should count all tasks")
        void whenCountAll_thenReturnTotalCount() {
            // When
            long count = taskRepository.count();

            // Then
            assertThat(count).isEqualTo(1);
        }
    }
}
