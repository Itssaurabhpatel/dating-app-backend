CREATE TABLE live_rooms (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    host_id VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    participants INTEGER NOT NULL DEFAULT 1,
    max_participants INTEGER NOT NULL DEFAULT 10,
    bg_image TEXT,
    tags TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
