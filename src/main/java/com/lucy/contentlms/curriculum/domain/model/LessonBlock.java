package com.lucy.contentlms.curriculum.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lesson_blocks")
public class LessonBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private LessonLevel lessonLevel;

    protected LessonBlock() {
    }

    public LessonBlock(int sequenceNumber, String content) {
        this.sequenceNumber = sequenceNumber;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getContent() {
        return content;
    }

    public LessonLevel getLessonLevel() {
        return lessonLevel;
    }

    void setLessonLevel(LessonLevel lessonLevel) {
        this.lessonLevel = lessonLevel;
    }
}
