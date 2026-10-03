package com.qpic.common.dto;

import java.util.List;
import java.util.UUID;

public record OwnershipCheck(UUID ownerId, List<UUID> mediaIds) {
}
