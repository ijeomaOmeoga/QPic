package com.qpic.share.web;

import com.qpic.common.dto.AlbumView;
import com.qpic.common.dto.MediaGroupView;
import com.qpic.common.dto.MediaView;
import com.qpic.share.domain.ShareTarget;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class ShareDtos {
    private ShareDtos() {}

    public record CreateShareRequest(
            @NotNull ShareTarget targetType,
            @NotNull UUID targetId,
            @Size(max = 120) String title,
            @Size(min = 4, max = 64) String password,
            @Future Instant expiresAt,
            @Min(1) Integer maxViews) {}

    public record ShareDto(
            UUID id,
            String token,
            ShareTarget targetType,
            UUID targetId,
            String title,
            String shareUrl,        // what the QR code encodes
            String deepLink,        // app scheme link
            boolean passwordProtected,
            Instant expiresAt,
            Integer maxViews,
            int viewCount,
            boolean revoked,
            Instant createdAt) {}

    /** Safe metadata shown to a scanner BEFORE they open the content (e.g. to ask for the password). */
    public record ShareInfo(
            ShareTarget targetType,
            String title,
            boolean passwordProtected,
            boolean available,
            String unavailableReason,
            Instant expiresAt) {}

    public record ResolveRequest(@Size(max = 64) String password) {}

    /** Exactly one of album / group / media is set, matching targetType. */
    public record ShareContent(
            ShareTarget targetType,
            String title,
            AlbumView album,
            MediaGroupView group,
            MediaView media,
            Instant expiresAt) {}
}
