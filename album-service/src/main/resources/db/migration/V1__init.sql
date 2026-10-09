CREATE TABLE albums (
    id             UUID          PRIMARY KEY,
    owner_id       UUID          NOT NULL,
    title          VARCHAR(120)  NOT NULL,
    description    VARCHAR(1000),
    cover_media_id UUID,
    created_at     TIMESTAMPTZ   NOT NULL,
    updated_at     TIMESTAMPTZ   NOT NULL
);
CREATE INDEX idx_albums_owner_created ON albums (owner_id, created_at DESC);

CREATE TABLE media_groups (
    id          UUID          PRIMARY KEY,
    album_id    UUID          NOT NULL REFERENCES albums (id) ON DELETE CASCADE,
    title       VARCHAR(120)  NOT NULL,
    description VARCHAR(1000),
    sort_order  INT           NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL
);
CREATE INDEX idx_media_groups_album ON media_groups (album_id, sort_order);

CREATE TABLE media_group_items (
    id         UUID         PRIMARY KEY,
    group_id   UUID         NOT NULL REFERENCES media_groups (id) ON DELETE CASCADE,
    media_id   UUID         NOT NULL,          -- owned by media-service (no cross-service FK)
    sort_order INT          NOT NULL,
    caption    VARCHAR(500),
    created_at TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_group_media UNIQUE (group_id, media_id)
);
CREATE INDEX idx_items_group ON media_group_items (group_id, sort_order);
CREATE INDEX idx_items_media ON media_group_items (media_id);
