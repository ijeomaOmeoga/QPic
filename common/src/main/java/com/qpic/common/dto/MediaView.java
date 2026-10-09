package com.qpic.common.dto;

import java.time.Instant;
import java.util.UUID;

/** A single image/video. {@code url} is a time-limited presigned URL that the phone can load directly. */
public record MediaView(
        UUID id,
        UUID ownerId,
        MediaKind kind,
        String contentType,
        String originalFilename,
        long sizeBytes,
        Integer width,
        Integer height,
        Long durationMs,
        String url,
        Instant createdAt) {
}
