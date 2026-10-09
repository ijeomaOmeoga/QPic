CREATE TABLE media (
    id                UUID         PRIMARY KEY,
    owner_id          UUID         NOT NULL,
    kind              VARCHAR(10)  NOT NULL,
    object_key        VARCHAR(512) NOT NULL UNIQUE,
    original_filename VARCHAR(255),
    content_type      VARCHAR(100) NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    width             INT,
    height            INT,
    duration_ms       BIGINT,
    status            VARCHAR(10)  NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_media_owner_status_created ON media (owner_id, status, created_at DESC);
CREATE INDEX idx_media_status_created ON media (status, created_at);
