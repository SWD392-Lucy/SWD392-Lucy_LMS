package com.lucy.contentlms.curriculum.application.dto;

import com.lucy.contentlms.curriculum.domain.model.Language;
import com.lucy.contentlms.curriculum.domain.model.Stage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpsertLessonLevelRequest(
        @Min(1) @Max(100) int levelNumber,
        @NotBlank @Size(max = 255) String title,
        @NotNull Language language,
        @NotNull Stage stage,
        @NotBlank @Size(max = 50) String courseCode,
        @Min(1) @Max(240) int durationMinutes,
        @Size(max = 255) String sourceFile,
        @NotEmpty List<@Valid LessonBlockRequest> blocks
) {
}
