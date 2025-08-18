CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL REFERENCES users(id),
                                token_hash TEXT NOT NULL UNIQUE,
                                expires_at TIMESTAMP NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                                created_at TIMESTAMP NOT NULL DEFAULT now(),
                                last_used_at TIMESTAMP
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
