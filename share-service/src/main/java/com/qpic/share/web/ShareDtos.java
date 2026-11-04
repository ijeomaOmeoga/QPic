package com.qpic.share.web;

import com.qpic.common.dto.AlbumView;
import com.qpic.common.dto.MediaGroupView;
import com.qpic.common.dto.MediaView;
import com.qpic.share.domain.ShareTarget;

import java.time.Instant;
import java.util.UUID;

public class ShareDtos {

    public record CreateShareRequest(
            ShareTarget targetType,
            UUID targetId,
            String title,
            Instant expiresAt,
            Integer maxViews,
            String password
    ) {
    }

    public record ShareDto(
            UUID id,
            String token,
            ShareTarget targetType,
            UUID targetId,
            String title,
            String shareUrl,
            String deepLink,
            boolean passwordProtected,
            Instant expiresAt,
            Integer maxViews,
            int viewCount,
            boolean revoked,
            Instant createdAt
    ) {
    }

    public record ShareInfo(
            ShareTarget targetType,
            String title,
            boolean passwordProtected,
            boolean available,
            String reason,
            Instant expiresAt
    ) {
    }

    public record ShareContent(
            ShareTarget targetType,
            String title,
            AlbumView album,
            MediaGroupView group,
            MediaView media,
            Instant expiresAt
    ) {
    }

    public record ResolveRequest(String password) {
    }
}
