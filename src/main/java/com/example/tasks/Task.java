package com.example.tasks;

import java.time.Instant;
import java.util.UUID;

public record Task(UUID id, String title, String description, boolean completed, Instant createdAt) {}
