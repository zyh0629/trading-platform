ALTER TABLE campus_spot
    ADD COLUMN owner_id BIGINT DEFAULT NULL,
    ADD COLUMN is_public TINYINT DEFAULT 1,
    ADD INDEX idx_owner (owner_id);
