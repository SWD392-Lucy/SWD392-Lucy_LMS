package com.lucy.contentlms.material.api.dto;

import com.lucy.contentlms.material.domain.LearningMaterial;

import java.time.Instant;
import java.util.UUID;

public record MaterialResponse(
        Long id,
        String title,
        String originalFileName,
        String contentType,
        long fileSizeBytes,
        UUID uploadedByUserId,
        Instant uploadedAt,
        String downloadEndpoint
) {
    public static MaterialResponse from(LearningMaterial material) {
        return new MaterialResponse(
                material.getId(),
                material.getTitle(),
                material.getOriginalFileName(),
                material.getContentType(),
                material.getFileSizeBytes(),
                material.getUploadedByUserId(),
                material.getUploadedAt(),
                "/api/content/materials/" + material.getId() + "/download"
        );
    }
}
