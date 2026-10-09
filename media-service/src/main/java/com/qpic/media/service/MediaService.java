package com.qpic.media.service;

import com.qpic.common.dto.MediaKind;
import com.qpic.common.dto.MediaView;
import com.qpic.common.dto.PageResponse;
import com.qpic.common.error.ApiException;
import com.qpic.media.config.MediaProperties;
import com.qpic.media.domain.Media;
import com.qpic.media.domain.MediaStatus;
import com.qpic.media.repo.MediaRepository;
import com.qpic.media.storage.StorageService;
import com.qpic.media.web.MediaDtos.CompleteUploadRequest;
import com.qpic.media.web.MediaDtos.InitUploadRequest;
import com.qpic.media.web.MediaDtos.InitUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;

import static java.util.Map.entry;

@Service
@RequiredArgsConstructor
public class MediaService {

    private static final int MAX_BATCH = 500;

    /** Allowed content types -> file extension. */
    private static final Map<String, String> EXT = Map.ofEntries(
            entry("image/jpeg", "jpg"), entry("image/png", "png"), entry("image/webp", "webp"),
            entry("image/gif", "gif"), entry("image/heic", "heic"), entry("image/heif", "heif"),
            entry("video/mp4", "mp4"), entry("video/quicktime", "mov"), entry("video/webm", "webm"),
            entry("video/x-m4v", "m4v"), entry("video/3gpp", "3gp"));

    private final MediaRepository repo;
    private final StorageService storage;
    private final MediaProperties props;

    // ------------------------------------------------------------------ upload: multipart (small files)

    @Transactional
    public MediaView upload(UUID owner, MultipartFile file, Integer width, Integer height, Long durationMs) {
        String contentType = normalize(file.getContentType());
        MediaKind kind = validate(contentType, file.getSize());

        UUID id = UUID.randomUUID();
        String key = objectKey(owner, id, contentType);
        try (InputStream in = file.getInputStream()) {
            storage.put(key, in, file.getSize(), contentType);
        } catch (IOException e) {
            throw ApiException.badRequest("Could not read uploaded file");
        }

        Media m = newMedia(id, owner, kind, key, file.getOriginalFilename(), contentType, file.getSize(), MediaStatus.READY);
        m.setWidth(width);
        m.setHeight(height);
        m.setDurationMs(durationMs);
        return toView(repo.save(m));
    }

    // ------------------------------------------------------------------ upload: presigned (recommended for video)

    @Transactional
    public InitUploadResponse initUpload(UUID owner, InitUploadRequest req) {
        String contentType = normalize(req.contentType());
        MediaKind kind = validate(contentType, req.sizeBytes());

        UUID id = UUID.randomUUID();
        String key = objectKey(owner, id, contentType);
        repo.save(newMedia(id, owner, kind, key, req.filename(), contentType, req.sizeBytes(), MediaStatus.PENDING));

        Duration ttl = Duration.ofMinutes(props.uploadUrlTtlMinutes());
        return new InitUploadResponse(id, storage.presignPut(key, ttl), Map.of("Content-Type", contentType), Instant.now().plus(ttl));
    }

    @Transactional
    public MediaView completeUpload(UUID owner, UUID id, CompleteUploadRequest req) {
        Media m = owned(owner, id);
        if (m.getStatus() == MediaStatus.READY) {
            return toView(m);
        }
        OptionalLong actual = storage.size(m.getObjectKey());
        if (actual.isEmpty()) {
            throw ApiException.badRequest("File has not been uploaded yet");
        }
        if (actual.getAsLong() > limit(m.getKind())) {
            storage.delete(m.getObjectKey());
            repo.delete(m);
            throw ApiException.of(HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE", "File is too large");
        }
        m.setSizeBytes(actual.getAsLong());
        if (req != null) {
            m.setWidth(req.width());
            m.setHeight(req.height());
            m.setDurationMs(req.durationMs());
        }
        m.setStatus(MediaStatus.READY);
        return toView(m);
    }

    // ------------------------------------------------------------------ read / delete

    @Transactional(readOnly = true)
    public MediaView get(UUID owner, UUID id) {
        Media m = owned(owner, id);
        if (m.getStatus() != MediaStatus.READY) {
            throw ApiException.notFound("Media not found");
        }
        return toView(m);
    }

    @Transactional(readOnly = true)
    public PageResponse<MediaView> list(UUID owner, MediaKind kind, Pageable pageable) {
        var page = kind == null
                ? repo.findByOwnerIdAndStatus(owner, MediaStatus.READY, pageable)
                : repo.findByOwnerIdAndStatusAndKind(owner, MediaStatus.READY, kind, pageable);
        return PageResponse.of(page, this::toView);
    }

    @Transactional
    public void delete(UUID owner, UUID id) {
        Media m = owned(owner, id);
        storage.delete(m.getObjectKey());
        repo.delete(m);
    }

    // ------------------------------------------------------------------ internal (service-to-service)

    @Transactional(readOnly = true)
    public List<MediaView> batch(Collection<UUID> ids) {
        if (ids.size() > MAX_BATCH) {
            throw ApiException.badRequest("Too many ids (max " + MAX_BATCH + ")");
        }
        return repo.findByIdInAndStatus(ids, MediaStatus.READY).stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public List<UUID> ownedReady(UUID owner, Collection<UUID> ids) {
        if (ids.size() > MAX_BATCH) {
            throw ApiException.badRequest("Too many ids (max " + MAX_BATCH + ")");
        }
        return repo.findByIdInAndOwnerIdAndStatus(ids, owner, MediaStatus.READY).stream().map(Media::getId).toList();
    }

    @Transactional(readOnly = true)
    public MediaView internalGet(UUID id) {
        Media m = repo.findById(id).filter(x -> x.getStatus() == MediaStatus.READY)
                .orElseThrow(() -> ApiException.notFound("Media not found"));
        return toView(m);
    }

    @Transactional(readOnly = true)
    public UUID ownerOf(UUID id) {
        return repo.findById(id).map(Media::getOwnerId).orElseThrow(() -> ApiException.notFound("Media not found"));
    }

    // ------------------------------------------------------------------ helpers

    private Media owned(UUID owner, UUID id) {
        return repo.findById(id)
                .filter(m -> m.getOwnerId().equals(owner))
                .orElseThrow(() -> ApiException.notFound("Media not found"));
    }

    private MediaView toView(Media m) {
        String url = storage.presignGet(m.getObjectKey(), Duration.ofMinutes(props.viewUrlTtlMinutes()));
        return new MediaView(m.getId(), m.getOwnerId(), m.getKind(), m.getContentType(), m.getOriginalFilename(),
                m.getSizeBytes(), m.getWidth(), m.getHeight(), m.getDurationMs(), url, m.getCreatedAt());
    }

    private Media newMedia(UUID id, UUID owner, MediaKind kind, String key, String filename,
                           String contentType, long size, MediaStatus status) {
        Media m = new Media();
        m.setId(id);
        m.setOwnerId(owner);
        m.setKind(kind);
        m.setObjectKey(key);
        m.setOriginalFilename(filename == null ? null : truncate(filename, 255));
        m.setContentType(contentType);
        m.setSizeBytes(size);
        m.setStatus(status);
        return m;
    }

    private MediaKind validate(String contentType, long size) {
        if (!EXT.containsKey(contentType)) {
            throw ApiException.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                    "Unsupported content type: " + contentType);
        }
        MediaKind kind = contentType.startsWith("image/") ? MediaKind.IMAGE : MediaKind.VIDEO;
        if (size <= 0) {
            throw ApiException.badRequest("Empty file");
        }
        if (size > limit(kind)) {
            throw ApiException.of(HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE",
                    kind + " exceeds the maximum size of " + (limit(kind) / (1024 * 1024)) + " MB");
        }
        return kind;
    }

    private long limit(MediaKind kind) {
        return kind == MediaKind.IMAGE ? props.maxImageBytes() : props.maxVideoBytes();
    }

    private static String objectKey(UUID owner, UUID id, String contentType) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return "%s/%d/%02d/%s.%s".formatted(owner, now.getYear(), now.getMonthValue(), id, EXT.get(contentType));
    }

    private static String normalize(String contentType) {
        if (contentType == null) {
            throw ApiException.badRequest("Missing content type");
        }
        int semi = contentType.indexOf(';');
        return (semi >= 0 ? contentType.substring(0, semi) : contentType).trim().toLowerCase(Locale.ROOT);
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
