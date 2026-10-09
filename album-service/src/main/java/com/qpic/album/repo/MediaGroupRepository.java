package com.qpic.album.repo;

import com.qpic.album.domain.MediaGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaGroupRepository extends JpaRepository<MediaGroup, UUID> {
}
