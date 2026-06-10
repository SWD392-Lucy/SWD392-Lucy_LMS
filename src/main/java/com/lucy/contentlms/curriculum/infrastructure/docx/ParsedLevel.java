package com.lucy.contentlms.curriculum.infrastructure.docx;

import com.lucy.contentlms.curriculum.domain.model.Language;
import com.lucy.contentlms.curriculum.domain.model.Stage;

import java.util.List;

public record ParsedLevel(
        int levelNumber,
        String title,
        Language language,
        Stage stage,
        String courseCode,
        int durationMinutes,
        List<String> blocks
) {
}
