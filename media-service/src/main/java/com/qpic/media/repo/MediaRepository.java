package com.qpic.media.repo;

import com.qpic.common.dto.MediaKind;
import com.qpic.media.domain.Media;
import com.qpic.media.domain.MediaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {

    Page<Media> findByOwnerIdAndStatus(UUID ownerId, MediaStatus status, Pageable pageable);

    Page<Media> findByOwnerIdAndStatusAndKind(UUID ownerId, MediaStatus status, MediaKind kind, Pageable pageable);

    List<Media> findByIdInAndStatus(Collection<UUID> ids, MediaStatus status);

    List<Media> findByIdInAndOwnerIdAndStatus(Collection<UUID> ids, UUID ownerId, MediaStatus status);

    List<Media> findByStatusAndCreatedAtBefore(MediaStatus status, Instant before);
}
