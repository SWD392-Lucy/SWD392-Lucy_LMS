package com.lucy.contentlms.curriculum.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lesson_levels", indexes = {
        @Index(name = "idx_lesson_levels_language_number", columnList = "language,level_number"),
        @Index(name = "idx_lesson_levels_course", columnList = "course_code")
})
public class LessonLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_number", nullable = false)
    private int levelNumber;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Stage stage;

    @Column(name = "course_code", nullable = false, length = 50)
    private String courseCode;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SourceDocument sourceDocument;

    @OneToMany(mappedBy = "lessonLevel", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    private List<LessonBlock> blocks = new ArrayList<>();

    protected LessonLevel() {
    }

    public LessonLevel(int levelNumber, String title, Language language, Stage stage, String courseCode, int durationMinutes) {
        this.levelNumber = levelNumber;
        this.title = title;
        this.language = language;
        this.stage = stage;
        this.courseCode = courseCode;
        this.durationMinutes = durationMinutes;
    }

    public Long getId() {
        return id;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getTitle() {
        return title;
    }

    public Language getLanguage() {
        return language;
    }

    public Stage getStage() {
        return stage;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public SourceDocument getSourceDocument() {
        return sourceDocument;
    }

    void setSourceDocument(SourceDocument sourceDocument) {
        this.sourceDocument = sourceDocument;
    }

    public List<LessonBlock> getBlocks() {
        return blocks;
    }

    public void addBlock(LessonBlock block) {
        blocks.add(block);
        block.setLessonLevel(this);
    }
}
