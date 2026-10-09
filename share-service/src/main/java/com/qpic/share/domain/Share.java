package com.qpic.share.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shares")
@Getter
@Setter
@NoArgsConstructor
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Unguessable public identifier that is encoded in the QR code. */
    @Column(nullable = false, unique = true, length = 32)
    private String token;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ShareTarget targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(length = 120)
    private String title;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "max_views")
    private Integer maxViews;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isExhausted() {
        return maxViews != null && viewCount >= maxViews;
    }
}
