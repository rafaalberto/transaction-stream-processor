CREATE TABLE outbox_events
(
    id         UUID PRIMARY KEY,
    topic      VARCHAR(255)             NOT NULL,
    payload    JSONB                    NOT NULL,
    status     VARCHAR(32)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_outbox_events_status_created_at
    ON outbox_events (status, created_at);
