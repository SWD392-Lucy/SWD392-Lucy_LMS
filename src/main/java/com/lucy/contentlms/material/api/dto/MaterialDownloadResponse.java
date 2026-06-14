package com.lucy.contentlms.material.api.dto;

public record MaterialDownloadResponse(
        String url,
        String contentType,
        String fileName
) {
}
