package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // GET /api/tasks -> list all tasks
    @GetMapping({"", "/"})
    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    // GET /api/tasks/my -> list tasks assigned to current user (if authenticated)
    @GetMapping("/my")
    public ResponseEntity<?> getMyTasks(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                (authentication.getPrincipal() instanceof String &&
                        "anonymousUser".equals(authentication.getPrincipal()))) {
            // When not authenticated (JWT temporarily disabled), return all for demo purposes
            return ok(taskRepository.findAll());
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ok(List.of());
        }
        return ok(taskRepository.findByAssignedTo(userOpt.get()));
    }

    // GET /api/tasks/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> notFound().build());
    }

    // POST /api/tasks -> create new task
    @PostMapping({"", "/"})
    public ResponseEntity<Task> create(@RequestBody Task task, Authentication authentication) {
        // Make sure timestamps are set
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }
        task.setUpdatedAt(LocalDateTime.now());

        // If user is authenticated, set createdBy and (if missing) assign to that user
        if (authentication != null && authentication.isAuthenticated() &&
            !(authentication.getPrincipal() instanceof String &&
                "anonymousUser".equals(authentication.getPrincipal()))) {
            userRepository.findByUsername(authentication.getName())
                .ifPresent(user -> {
                task.setCreatedBy(user);
                if (task.getAssignedTo() == null) {
                    task.setAssignedTo(user);
                }
                });
        }

        Task saved = taskRepository.save(task);
        return created(URI.create("/api/tasks/" + saved.getId())).body(saved);
    }
}
