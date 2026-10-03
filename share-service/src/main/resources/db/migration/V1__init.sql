CREATE TABLE shares (
    id            UUID         PRIMARY KEY,
    token         VARCHAR(32)  NOT NULL UNIQUE,
    owner_id      UUID         NOT NULL,
    target_type   VARCHAR(20)  NOT NULL,
    target_id     UUID         NOT NULL,
    title         VARCHAR(120),
    password_hash VARCHAR(255),
    expires_at    TIMESTAMPTZ,
    max_views     INT,
    view_count    INT          NOT NULL DEFAULT 0,
    revoked       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_shares_owner_created ON shares (owner_id, created_at DESC);
CREATE INDEX idx_shares_target ON shares (target_type, target_id);

CREATE TABLE share_scans (
    id         UUID         PRIMARY KEY,
    share_id   UUID         NOT NULL REFERENCES shares (id) ON DELETE CASCADE,
    scanned_at TIMESTAMPTZ  NOT NULL,
    ip_hash    VARCHAR(64),
    user_agent VARCHAR(300)
);
CREATE INDEX idx_share_scans_share ON share_scans (share_id, scanned_at DESC);
