package com.knowledgegap.platform.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StartSessionRequest(
    @NotNull(message = "Student ID is required")
    UUID studentId
) {}
