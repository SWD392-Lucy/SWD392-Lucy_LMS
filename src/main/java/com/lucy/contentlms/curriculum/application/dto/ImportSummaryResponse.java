package com.lucy.contentlms.curriculum.application.dto;

public record ImportSummaryResponse(
        int importedDocuments,
        int skippedDocuments,
        int importedLevels,
        int importedBlocks
) {
}
