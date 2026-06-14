package com.lucy.contentlms.material.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "learning_materials")
public class LearningMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Column(name = "cloudinary_public_id", nullable = false, length = 500)
    private String cloudinaryPublicId;

    @Column(name = "cloudinary_resource_type", nullable = false, length = 40)
    private String cloudinaryResourceType;

    @Column(name = "cloudinary_secure_url", nullable = false, length = 1000)
    private String cloudinarySecureUrl;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private UUID uploadedByUserId;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    protected LearningMaterial() {
    }

    public LearningMaterial(
            String title,
            String originalFileName,
            String contentType,
            long fileSizeBytes,
            String cloudinaryPublicId,
            String cloudinaryResourceType,
            String cloudinarySecureUrl,
            UUID uploadedByUserId
    ) {
        this.title = title;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.cloudinaryPublicId = cloudinaryPublicId;
        this.cloudinaryResourceType = cloudinaryResourceType;
        this.cloudinarySecureUrl = cloudinarySecureUrl;
        this.uploadedByUserId = uploadedByUserId;
        this.uploadedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }

    public String getCloudinaryResourceType() {
        return cloudinaryResourceType;
    }

    public String getCloudinarySecureUrl() {
        return cloudinarySecureUrl;
    }

    public UUID getUploadedByUserId() {
        return uploadedByUserId;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}
