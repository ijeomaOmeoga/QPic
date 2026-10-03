package com.qpic.share.repo;

import com.qpic.share.domain.Share;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ShareRepository extends JpaRepository<Share, UUID> {

    Optional<Share> findByToken(String token);

    Page<Share> findByOwnerId(UUID ownerId, Pageable pageable);

    /** Atomic: also enforces maxViews under concurrency. Returns 0 if the limit has been reached. */
    @Modifying
    @Query("update Share s set s.viewCount = s.viewCount + 1 " +
           "where s.id = :id and (s.maxViews is null or s.viewCount < s.maxViews)")
    int incrementViews(@Param("id") UUID id);
}
