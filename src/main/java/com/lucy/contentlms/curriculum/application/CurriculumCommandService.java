package com.lucy.contentlms.curriculum.application;

import com.lucy.contentlms.curriculum.application.dto.LessonBlockRequest;
import com.lucy.contentlms.curriculum.application.dto.LessonLevelDetailResponse;
import com.lucy.contentlms.curriculum.application.dto.UpsertLessonLevelRequest;
import com.lucy.contentlms.curriculum.domain.model.LessonBlock;
import com.lucy.contentlms.curriculum.domain.model.LessonLevel;
import com.lucy.contentlms.curriculum.domain.model.SourceDocument;
import com.lucy.contentlms.curriculum.infrastructure.persistence.LessonLevelRepository;
import com.lucy.contentlms.curriculum.infrastructure.persistence.SourceDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class CurriculumCommandService {

    private final LessonLevelRepository lessonLevelRepository;
    private final SourceDocumentRepository sourceDocumentRepository;

    public CurriculumCommandService(
            LessonLevelRepository lessonLevelRepository,
            SourceDocumentRepository sourceDocumentRepository
    ) {
        this.lessonLevelRepository = lessonLevelRepository;
        this.sourceDocumentRepository = sourceDocumentRepository;
    }

    @Transactional
    public LessonLevelDetailResponse createLevel(UpsertLessonLevelRequest request) {
        SourceDocument sourceDocument = findOrCreateSourceDocument(request);
        LessonLevel level = new LessonLevel(
                request.levelNumber(),
                request.title(),
                request.language(),
                request.stage(),
                request.courseCode(),
                request.durationMinutes()
        );
        level.update(
                request.levelNumber(),
                request.title(),
                request.language(),
                request.stage(),
                request.courseCode(),
                request.durationMinutes(),
                sourceDocument
        );
        level.replaceBlocks(toBlocks(request.blocks()));
        return LessonLevelDetailResponse.from(lessonLevelRepository.save(level));
    }

    @Transactional
    public LessonLevelDetailResponse updateLevel(Long id, UpsertLessonLevelRequest request) {
        LessonLevel level = lessonLevelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson level does not exist: " + id));
        SourceDocument sourceDocument = findOrCreateSourceDocument(request);
        level.update(
                request.levelNumber(),
                request.title(),
                request.language(),
                request.stage(),
                request.courseCode(),
                request.durationMinutes(),
                sourceDocument
        );
        level.replaceBlocks(toBlocks(request.blocks()));
        return LessonLevelDetailResponse.from(level);
    }

    @Transactional
    public void deleteLevel(Long id) {
        if (!lessonLevelRepository.existsById(id)) {
            throw new IllegalArgumentException("Lesson level does not exist: " + id);
        }
        lessonLevelRepository.deleteById(id);
    }

    private SourceDocument findOrCreateSourceDocument(UpsertLessonLevelRequest request) {
        String sourceFile = normalizeSourceFile(request);
        return sourceDocumentRepository.findByFileName(sourceFile)
                .orElseGet(() -> sourceDocumentRepository.save(new SourceDocument(
                        sourceFile,
                        request.language(),
                        request.courseCode()
                )));
    }

    private String normalizeSourceFile(UpsertLessonLevelRequest request) {
        if (request.sourceFile() != null && !request.sourceFile().isBlank()) {
            return request.sourceFile().trim();
        }
        String courseCode = request.courseCode().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        return "manual-" + request.language().name().toLowerCase(Locale.ROOT) + "-" + courseCode + ".docx";
    }

    private List<LessonBlock> toBlocks(List<LessonBlockRequest> blocks) {
        return blocks.stream()
                .sorted(Comparator.comparingInt(LessonBlockRequest::sequenceNumber))
                .map(block -> new LessonBlock(block.sequenceNumber(), block.content().trim()))
                .toList();
    }
}
