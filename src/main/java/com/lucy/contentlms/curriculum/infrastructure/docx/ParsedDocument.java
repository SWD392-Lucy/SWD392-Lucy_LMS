package com.lucy.contentlms.curriculum.infrastructure.docx;

import com.lucy.contentlms.curriculum.domain.model.Language;

import java.util.List;

public record ParsedDocument(
        String fileName,
        Language language,
        String courseCode,
        List<ParsedLevel> levels
) {
}
