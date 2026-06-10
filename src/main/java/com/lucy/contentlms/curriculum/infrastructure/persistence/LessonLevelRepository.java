package com.lucy.contentlms.curriculum.infrastructure.persistence;

import com.lucy.contentlms.curriculum.domain.model.Language;
import com.lucy.contentlms.curriculum.domain.model.LessonLevel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonLevelRepository extends JpaRepository<LessonLevel, Long> {

    @EntityGraph(attributePaths = {"sourceDocument"})
    List<LessonLevel> findByLanguageOrderByLevelNumberAscCourseCodeAsc(Language language);

    @EntityGraph(attributePaths = {"blocks", "sourceDocument"})
    List<LessonLevel> findByLanguageAndLevelNumberOrderByCourseCodeAsc(Language language, int levelNumber);

    @EntityGraph(attributePaths = {"blocks", "sourceDocument"})
    Optional<LessonLevel> findById(Long id);

    @EntityGraph(attributePaths = {"sourceDocument"})
    List<LessonLevel> findByTitleContainingIgnoreCaseOrCourseCodeContainingIgnoreCaseOrderByLanguageAscLevelNumberAsc(
            String title,
            String courseCode
    );
}
