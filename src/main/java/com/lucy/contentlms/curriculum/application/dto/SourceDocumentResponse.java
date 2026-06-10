package com.lucy.contentlms.curriculum.application.dto;

import com.lucy.contentlms.curriculum.domain.model.Language;
import com.lucy.contentlms.curriculum.domain.model.SourceDocument;

import java.time.Instant;

public record SourceDocumentResponse(
        Long id,
        String fileName,
        Language language,
        String courseCode,
        Instant importedAt
) {
    public static SourceDocumentResponse from(SourceDocument document) {
        return new SourceDocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getLanguage(),
                document.getCourseCode(),
                document.getImportedAt()
        );
    }
}
