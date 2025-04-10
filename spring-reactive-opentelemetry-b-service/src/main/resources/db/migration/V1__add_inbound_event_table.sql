-- adds inbound_events table

CREATE TABLE inbound_events (
    id bigserial PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    created_at bigint NOT NULL
);