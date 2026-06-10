package com.lucy.contentlms.curriculum.application.dto;

import com.lucy.contentlms.curriculum.domain.model.Language;
import com.lucy.contentlms.curriculum.domain.model.LessonLevel;
import com.lucy.contentlms.curriculum.domain.model.Stage;

import java.util.List;

public record LessonLevelDetailResponse(
        Long id,
        int levelNumber,
        String title,
        Language language,
        Stage stage,
        String courseCode,
        int durationMinutes,
        String sourceFile,
        List<LessonBlockResponse> blocks
) {
    public static LessonLevelDetailResponse from(LessonLevel level) {
        return new LessonLevelDetailResponse(
                level.getId(),
                level.getLevelNumber(),
                level.getTitle(),
                level.getLanguage(),
                level.getStage(),
                level.getCourseCode(),
                level.getDurationMinutes(),
                level.getSourceDocument().getFileName(),
                level.getBlocks().stream().map(LessonBlockResponse::from).toList()
        );
    }
}
