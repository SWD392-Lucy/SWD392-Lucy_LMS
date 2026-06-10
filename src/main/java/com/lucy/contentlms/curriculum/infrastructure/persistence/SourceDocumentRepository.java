package com.lucy.contentlms.curriculum.infrastructure.persistence;

import com.lucy.contentlms.curriculum.domain.model.SourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SourceDocumentRepository extends JpaRepository<SourceDocument, Long> {

    Optional<SourceDocument> findByFileName(String fileName);
}
