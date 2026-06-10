package com.lucy.contentlms.curriculum.application;

import com.lucy.contentlms.curriculum.application.dto.LessonLevelDetailResponse;
import com.lucy.contentlms.curriculum.application.dto.LessonLevelSummaryResponse;
import com.lucy.contentlms.curriculum.application.dto.SourceDocumentResponse;
import com.lucy.contentlms.curriculum.domain.model.Language;
import com.lucy.contentlms.curriculum.infrastructure.persistence.LessonLevelRepository;
import com.lucy.contentlms.curriculum.infrastructure.persistence.SourceDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CurriculumQueryService {

    private final LessonLevelRepository lessonLevelRepository;
    private final SourceDocumentRepository sourceDocumentRepository;

    public CurriculumQueryService(
            LessonLevelRepository lessonLevelRepository,
            SourceDocumentRepository sourceDocumentRepository
    ) {
        this.lessonLevelRepository = lessonLevelRepository;
        this.sourceDocumentRepository = sourceDocumentRepository;
    }

    public List<SourceDocumentResponse> documents() {
        return sourceDocumentRepository.findAll().stream()
                .map(SourceDocumentResponse::from)
                .toList();
    }

    public List<LessonLevelSummaryResponse> levels(Language language) {
        return lessonLevelRepository.findByLanguageOrderByLevelNumberAscCourseCodeAsc(language)
                .stream()
                .map(LessonLevelSummaryResponse::from)
                .toList();
    }

    public Optional<LessonLevelDetailResponse> levelById(Long id) {
        return lessonLevelRepository.findById(id)
                .map(LessonLevelDetailResponse::from);
    }

    public List<LessonLevelDetailResponse> levelContent(Language language, int level) {
        return lessonLevelRepository.findByLanguageAndLevelNumberOrderByCourseCodeAsc(language, level)
                .stream()
                .map(LessonLevelDetailResponse::from)
                .toList();
    }

    public List<LessonLevelSummaryResponse> search(String query) {
        return lessonLevelRepository
                .findByTitleContainingIgnoreCaseOrCourseCodeContainingIgnoreCaseOrderByLanguageAscLevelNumberAsc(query, query)
                .stream()
                .map(LessonLevelSummaryResponse::from)
                .toList();
    }
}
