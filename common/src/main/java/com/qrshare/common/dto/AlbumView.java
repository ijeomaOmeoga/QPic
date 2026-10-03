package com.qpic.common.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Album -> MediaGroups -> Items -> Media (fully resolved tree). */
public record AlbumView(
        UUID id,
        UUID ownerId,
        String title,
        String description,
        MediaView cover,
        Instant createdAt,
        Instant updatedAt,
        List<MediaGroupView> groups) {
}
