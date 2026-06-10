package com.lucy.contentlms.curriculum.application.dto;

import com.lucy.contentlms.curriculum.domain.model.Language;
import com.lucy.contentlms.curriculum.domain.model.LessonLevel;
import com.lucy.contentlms.curriculum.domain.model.Stage;

public record LessonLevelSummaryResponse(
        Long id,
        int levelNumber,
        String title,
        Language language,
        Stage stage,
        String courseCode,
        int durationMinutes,
        String sourceFile
) {
    public static LessonLevelSummaryResponse from(LessonLevel level) {
        return new LessonLevelSummaryResponse(
                level.getId(),
                level.getLevelNumber(),
                level.getTitle(),
                level.getLanguage(),
                level.getStage(),
                level.getCourseCode(),
                level.getDurationMinutes(),
                level.getSourceDocument().getFileName()
        );
    }
}
