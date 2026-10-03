package com.qpic.common.dto;

import java.util.List;
import java.util.UUID;

public record MediaGroupView(
        UUID id,
        UUID albumId,
        UUID ownerId,
        String title,
        String description,
        int position,
        List<MediaItemView> items) {
}
