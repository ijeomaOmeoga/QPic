package com.qpic.share.service;

import com.qpic.common.client.AlbumClient;
import com.qpic.common.client.MediaClient;
import com.qpic.common.dto.PageResponse;
import com.qpic.common.error.ApiException;
import com.qpic.share.config.ShareProperties;
import com.qpic.share.domain.Share;
import com.qpic.share.domain.ShareScan;
import com.qpic.share.domain.ShareTarget;
import com.qpic.share.repo.ShareRepository;
import com.qpic.share.repo.ShareScanRepository;
import com.qpic.share.web.ShareDtos.CreateShareRequest;
import com.qpic.share.web.ShareDtos.ShareContent;
import com.qpic.share.web.ShareDtos.ShareDto;
import com.qpic.share.web.ShareDtos.ShareInfo;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareRepository shares;
    private final ShareScanRepository scans;
    private final AlbumClient albumClient;
    private final MediaClient mediaClient;
    private final PasswordEncoder encoder;
    private final ShareProperties props;
    private final TransactionTemplate tx;

    // =================================================================== owner side

    public ShareDto create(UUID owner, CreateShareRequest req) {
        assertOwnsTarget(owner, req.targetType(), req.targetId());

        Share s = new Share();
        s.setToken(TokenGenerator.newToken());
        s.setOwnerId(owner);
        s.setTargetType(req.targetType());
        s.setTargetId(req.targetId());
        s.setTitle(req.title() == null || req.title().isBlank() ? null : req.title().trim());
        s.setExpiresAt(req.expiresAt());
        s.setMaxViews(req.maxViews());
        if (req.password() != null && !req.password().isBlank()) {
            s.setPasswordHash(encoder.encode(req.password()));
        }
        return toDto(shares.save(s));
    }

    public PageResponse<ShareDto> listMine(UUID owner, Pageable pageable) {
        return PageResponse.of(shares.findByOwnerId(owner, pageable), this::toDto);
    }

    public ShareDto getMine(UUID owner, UUID id) {
        return toDto(owned(owner, id));
    }

    public ShareDto revoke(UUID owner, UUID id) {
        Share s = owned(owner, id);
        s.setRevoked(true);
        return toDto(shares.save(s));
    }

    public String qrContentFor(UUID owner, UUID id) {
        return shareUrl(owned(owner, id).getToken());
    }

    // =================================================================== public (scanner) side

    public ShareInfo info(String token) {
        Share s = shares.findByToken(token).orElseThrow(() -> ApiException.notFound("Share not found"));
        String reason = unavailableReason(s);
        return new ShareInfo(s.getTargetType(), s.getTitle(), s.getPasswordHash() != null,
                reason == null, reason, s.getExpiresAt());
    }

    public String qrContentForToken(String token) {
        shares.findByToken(token).orElseThrow(() -> ApiException.notFound("Share not found"));
        return shareUrl(token);
    }

    public ShareContent resolve(String token, String password, String ip, String userAgent) {
        Share s = shares.findByToken(token).orElseThrow(() -> ApiException.notFound("Share not found"));

        String reason = unavailableReason(s);
        if (reason != null) {
            throw ApiException.gone(reason, messageFor(reason));
        }

        if (s.getPasswordHash() != null) {
            if (password == null || password.isBlank()) {
                throw ApiException.of(HttpStatus.FORBIDDEN, "SHARE_PASSWORD_REQUIRED", "This share is password protected");
            }
            if (!encoder.matches(password, s.getPasswordHash())) {
                throw ApiException.of(HttpStatus.FORBIDDEN, "SHARE_PASSWORD_INVALID", "Incorrect password");
            }
        }

        Boolean counted = tx.execute(status -> {
            if (shares.incrementViews(s.getId()) == 0) {
                return false;
            }
            ShareScan scan = new ShareScan();
            scan.setShareId(s.getId());
            scan.setIpHash(hash(ip));
            scan.setUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(userAgent.length(), 300)));
            scans.save(scan);
            return true;
        });
        if (!Boolean.TRUE.equals(counted)) {
            throw ApiException.gone("SHARE_EXHAUSTED", messageFor("SHARE_EXHAUSTED"));
        }

        return loadContent(s);
    }

    // =================================================================== helpers

    private ShareContent loadContent(Share s) {
        try {
            return switch (s.getTargetType()) {
                case ALBUM -> {
                    var album = albumClient.album(s.getTargetId());
                    yield new ShareContent(ShareTarget.ALBUM, album.title(), album, null, null, s.getExpiresAt());
                }
                case MEDIA_GROUP -> {
                    var group = albumClient.group(s.getTargetId());
                    yield new ShareContent(ShareTarget.MEDIA_GROUP, group.title(), null, group, null, s.getExpiresAt());
                }
                case MEDIA -> {
                    var media = mediaClient.get(s.getTargetId());
                    yield new ShareContent(ShareTarget.MEDIA, s.getTitle(), null, null, media, s.getExpiresAt());
                }
            };
        } catch (FeignException.NotFound e) {
            throw ApiException.gone("SHARE_TARGET_DELETED", "The shared content no longer exists");
        }
    }

    private void assertOwnsTarget(UUID owner, ShareTarget type, UUID targetId) {
        UUID actualOwner;
        try {
            actualOwner = switch (type) {
                case ALBUM -> albumClient.albumOwner(targetId).ownerId();
                case MEDIA_GROUP -> albumClient.groupOwner(targetId).ownerId();
                case MEDIA -> mediaClient.owner(targetId).ownerId();
            };
        } catch (FeignException.NotFound e) {
            throw ApiException.notFound("Target not found");
        }
        if (!owner.equals(actualOwner)) {
            throw ApiException.notFound("Target not found");   // don't reveal that it exists
        }
    }

    private Share owned(UUID owner, UUID id) {
        return shares.findById(id).filter(s -> s.getOwnerId().equals(owner))
                .orElseThrow(() -> ApiException.notFound("Share not found"));
    }

    private static String unavailableReason(Share s) {
        if (s.isRevoked()) return "SHARE_REVOKED";
        if (s.isExpired()) return "SHARE_EXPIRED";
        if (s.isExhausted()) return "SHARE_EXHAUSTED";
        return null;
    }

    private static String messageFor(String reason) {
        return switch (reason) {
            case "SHARE_REVOKED" -> "The owner has revoked this link";
            case "SHARE_EXPIRED" -> "This link has expired";
            case "SHARE_EXHAUSTED" -> "This link has reached its view limit";
            default -> "This link is no longer available";
        };
    }

    private String shareUrl(String token) {
        String base = props.baseUrl().endsWith("/") ? props.baseUrl().substring(0, props.baseUrl().length() - 1) : props.baseUrl();
        return base + "/s/" + token;
    }

    private ShareDto toDto(Share s) {
        return new ShareDto(s.getId(), s.getToken(), s.getTargetType(), s.getTargetId(), s.getTitle(),
                shareUrl(s.getToken()), props.deepLinkPrefix() + s.getToken(),
                s.getPasswordHash() != null, s.getExpiresAt(), s.getMaxViews(), s.getViewCount(),
                s.isRevoked(), s.getCreatedAt());
    }

    private static String hash(String value) {
        if (value == null) return null;
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
