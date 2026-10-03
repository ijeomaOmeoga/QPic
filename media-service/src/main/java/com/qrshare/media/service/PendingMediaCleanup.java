package com.qpic.media.service;

import com.qpic.media.domain.Media;
import com.qpic.media.domain.MediaStatus;
import com.qpic.media.repo.MediaRepository;
import com.qpic.media.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Removes uploads that were initialised but never completed. */
@Component
@RequiredArgsConstructor
@Slf4j
public class PendingMediaCleanup {

    private final MediaRepository repo;
    private final StorageService storage;

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT5M")
    @Transactional
    public void purgeAbandonedUploads() {
        List<Media> stale = repo.findByStatusAndCreatedAtBefore(MediaStatus.PENDING, Instant.now().minus(Duration.ofHours(24)));
        stale.forEach(m -> storage.delete(m.getObjectKey()));
        repo.deleteAll(stale);
        if (!stale.isEmpty()) {
            log.info("Purged {} abandoned uploads", stale.size());
        }
    }
}
