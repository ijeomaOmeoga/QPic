package com.qrshare.album.web;

import com.qrshare.common.dto.MediaView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AlbumDtos {
    private AlbumDtos() {}

    public record AlbumRequest(
            @NotBlank @Size(max = 120) String title,
            @Size(max = 1000) String description,
            UUID coverMediaId) {}

    /** mediaIds optional: lets the app create a group and fill it in one call. */
    public record GroupRequest(
            @NotBlank @Size(max = 120) String title,
            @Size(max = 1000) String description,
            @Size(max = 500) List<UUID> mediaIds) {}

    public record GroupUpdateRequest(
            @NotBlank @Size(max = 120) String title,
            @Size(max = 1000) String description) {}

    public record AddItemsRequest(@NotEmpty @Size(max = 500) List<UUID> mediaIds) {}

    public record ItemUpdateRequest(@Size(max = 500) String caption) {}

    /** Full ordered list of ids (groups or items). Must contain every existing id exactly once. */
    public record ReorderRequest(@NotEmpty List<UUID> ids) {}

    public record AlbumSummary(
            UUID id,
            String title,
            String description,
            MediaView cover,
            int groupCount,
            int mediaCount,
            Instant createdAt,
            Instant updatedAt) {}
}
