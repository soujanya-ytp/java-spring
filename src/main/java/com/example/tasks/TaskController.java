package com.example.tasks;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class TaskController {
    private final Map<UUID, Task> tasks = new ConcurrentHashMap<>();

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "ok"); }

    @GetMapping("/api/tasks")
    public List<Task> list() { return new ArrayList<>(tasks.values()); }

    @PostMapping("/api/tasks")
    public ResponseEntity<Task> create(@Valid @RequestBody TaskRequest request) {
        Task task = new Task(UUID.randomUUID(), request.title().trim(), Objects.requireNonNullElse(request.description(), ""), false, Instant.now());
        tasks.put(task.id(), task);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping("/api/tasks/{id}")
    public Task get(@PathVariable UUID id) { return requireTask(id); }

    @PatchMapping("/api/tasks/{id}")
    public Task update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Task current = requireTask(id);
        if (body.keySet().stream().anyMatch(key -> !Set.of("title", "description", "completed").contains(key))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown update field");
        }
        String title = current.title();
        String description = current.description();
        boolean completed = current.completed();
        if (body.containsKey("title")) {
            Object value = body.get("title");
            if (!(value instanceof String s) || s.isBlank() || s.trim().length() > 200) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid title");
            title = s.trim();
        }
        if (body.containsKey("description")) {
            Object value = body.get("description");
            if (!(value instanceof String s) || s.length() > 2000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid description");
            description = s;
        }
        if (body.containsKey("completed")) {
            Object value = body.get("completed");
            if (!(value instanceof Boolean b)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "completed must be a boolean");
            completed = b;
        }
        Task updated = new Task(id, title, description, completed, current.createdAt());
        tasks.put(id, updated);
        return updated;
    }

    @DeleteMapping("/api/tasks/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (tasks.remove(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        return ResponseEntity.noContent().build();
    }

    private Task requireTask(UUID id) {
        Task task = tasks.get(id);
        if (task == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        return task;
    }
}
