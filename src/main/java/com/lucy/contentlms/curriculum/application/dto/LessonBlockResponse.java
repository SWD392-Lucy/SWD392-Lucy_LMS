package com.lucy.contentlms.curriculum.application.dto;

import com.lucy.contentlms.curriculum.domain.model.LessonBlock;

public record LessonBlockResponse(
        int sequenceNumber,
        String content
) {
    public static LessonBlockResponse from(LessonBlock block) {
        return new LessonBlockResponse(block.getSequenceNumber(), block.getContent());
    }
}
