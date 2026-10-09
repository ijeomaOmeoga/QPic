package com.qpic.share.repo;

import com.qpic.share.domain.ShareScan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShareScanRepository extends JpaRepository<ShareScan, UUID> {
}
