package com.lucy.contentlms.material.infrastructure;

import com.lucy.contentlms.material.domain.LearningMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Long> {

    boolean existsByIdAndUploadedByUserId(Long id, UUID uploadedByUserId);
}
