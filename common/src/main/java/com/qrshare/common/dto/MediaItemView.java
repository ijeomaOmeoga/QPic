package com.qpic.common.dto;

import java.util.UUID;

/** One media inside a media group (adds caption + position). */
public record MediaItemView(UUID id, int position, String caption, MediaView media) {
}
