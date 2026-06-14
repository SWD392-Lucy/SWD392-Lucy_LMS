package com.lucy.contentlms.material.application;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lucy.contentlms.material.api.dto.MaterialDownloadResponse;
import com.lucy.contentlms.material.api.dto.MaterialResponse;
import com.lucy.contentlms.material.config.CloudinaryProperties;
import com.lucy.contentlms.material.domain.LearningMaterial;
import com.lucy.contentlms.material.infrastructure.LearningMaterialRepository;
import com.lucy.contentlms.material.infrastructure.RoomPinnedMaterialRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MaterialService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "ppt", "pptx");

    private final Cloudinary cloudinary;
    private final CloudinaryProperties cloudinaryProperties;
    private final LearningMaterialRepository materialRepository;
    private final RoomPinnedMaterialRepository pinnedMaterialRepository;

    public MaterialService(
            Cloudinary cloudinary,
            CloudinaryProperties cloudinaryProperties,
            LearningMaterialRepository materialRepository,
            RoomPinnedMaterialRepository pinnedMaterialRepository
    ) {
        this.cloudinary = cloudinary;
        this.cloudinaryProperties = cloudinaryProperties;
        this.materialRepository = materialRepository;
        this.pinnedMaterialRepository = pinnedMaterialRepository;
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> listMaterials() {
        return materialRepository.findAll()
                .stream()
                .map(MaterialResponse::from)
                .toList();
    }

    @Transactional
    public MaterialResponse uploadMaterial(MultipartFile file, String title, Authentication authentication) {
        validateCloudinaryConfig();
        validateFile(file);

        String originalFileName = requireFileName(file);
        UUID userId = currentUserId(authentication);
        String normalizedTitle = title == null || title.isBlank()
                ? stripExtension(originalFileName)
                : title.trim();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "raw",
                    "folder", cloudinaryProperties.getMaterialFolder(),
                    "use_filename", true,
                    "unique_filename", true
            ));

            LearningMaterial material = new LearningMaterial(
                    normalizedTitle,
                    originalFileName,
                    contentType(file),
                    file.getSize(),
                    String.valueOf(result.get("public_id")),
                    String.valueOf(result.getOrDefault("resource_type", "raw")),
                    String.valueOf(result.get("secure_url")),
                    userId
            );
            return MaterialResponse.from(materialRepository.save(material));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cannot read uploaded material: " + originalFileName, exception);
        }
    }

    @Transactional(readOnly = true)
    public MaterialDownloadResponse downloadMaterial(Long id) {
        LearningMaterial material = getMaterial(id);
        return new MaterialDownloadResponse(
                material.getCloudinarySecureUrl(),
                material.getContentType(),
                material.getOriginalFileName()
        );
    }

    @Transactional
    public void deleteMaterial(Long id, Authentication authentication) {
        LearningMaterial material = getMaterial(id);
        UUID userId = currentUserId(authentication);
        if (!isSuper(authentication) && !material.getUploadedByUserId().equals(userId)) {
            throw new IllegalArgumentException("Only the uploader or Super can delete this material.");
        }

        validateCloudinaryConfig();
        try {
            cloudinary.uploader().destroy(material.getCloudinaryPublicId(), ObjectUtils.asMap("resource_type", "raw"));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cannot delete material from Cloudinary.", exception);
        }
        pinnedMaterialRepository.deleteByMaterialId(id);
        materialRepository.delete(material);
    }

    LearningMaterial getMaterial(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material does not exist: " + id));
    }

    UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private void validateCloudinaryConfig() {
        if (isBlank(cloudinaryProperties.getCloudName())
                || isBlank(cloudinaryProperties.getApiKey())
                || isBlank(cloudinaryProperties.getApiSecret())) {
            throw new IllegalArgumentException("Cloudinary is not configured for LMS materials.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Material file is required.");
        }
        String extension = extension(requireFileName(file));
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only PDF, DOC, DOCX, PPT, and PPTX materials are supported.");
        }
    }

    private String requireFileName(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Uploaded material must have a file name.");
        }
        return fileName.trim();
    }

    private String contentType(MultipartFile file) {
        return file.getContentType() == null || file.getContentType().isBlank()
                ? "application/octet-stream"
                : file.getContentType();
    }

    private String extension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private boolean isSuper(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_Super"::equals);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
