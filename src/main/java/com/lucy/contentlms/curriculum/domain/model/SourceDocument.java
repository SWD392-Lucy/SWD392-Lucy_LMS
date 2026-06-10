package com.lucy.contentlms.curriculum.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "source_documents", uniqueConstraints = {
        @UniqueConstraint(name = "uk_source_documents_file_name", columnNames = "file_name")
})
public class SourceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Language language;

    @Column(name = "course_code", nullable = false, length = 50)
    private String courseCode;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt = Instant.now();

    @OneToMany(mappedBy = "sourceDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonLevel> levels = new ArrayList<>();

    protected SourceDocument() {
    }

    public SourceDocument(String fileName, Language language, String courseCode) {
        this.fileName = fileName;
        this.language = language;
        this.courseCode = courseCode;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public Language getLanguage() {
        return language;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public Instant getImportedAt() {
        return importedAt;
    }

    public List<LessonLevel> getLevels() {
        return levels;
    }

    public void addLevel(LessonLevel level) {
        levels.add(level);
        level.setSourceDocument(this);
    }
}
