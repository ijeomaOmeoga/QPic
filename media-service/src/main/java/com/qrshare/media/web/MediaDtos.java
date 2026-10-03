package com.qrshare.media.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class MediaDtos {
    private MediaDtos() {}

    public record InitUploadRequest(
            @NotBlank @Size(max = 255) String filename,
            @NotBlank @Size(max = 100) String contentType,
            @Positive long sizeBytes) {}

    /** Phone does: PUT uploadUrl with the given headers and the raw file as body. */
    public record InitUploadResponse(UUID mediaId, String uploadUrl, Map<String, String> headers, Instant expiresAt) {}

    public record CompleteUploadRequest(Integer width, Integer height, Long durationMs) {}
}
