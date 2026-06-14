package com.lucy.contentlms.curriculum.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record LessonBlockRequest(
        @Positive int sequenceNumber,
        @NotBlank String content
) {
}
