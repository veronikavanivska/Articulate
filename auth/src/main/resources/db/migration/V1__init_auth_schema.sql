-- 1) USERS: authentication-only data
CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       email         VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 2) ROLES: dictionary of roles
CREATE TABLE roles (
                       id          BIGSERIAL PRIMARY KEY,
                       name        VARCHAR(50) NOT NULL UNIQUE,
                       description TEXT
);



-- 3) USER_ROLES: many-to-many link between users and roles
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- Seed standard roles
INSERT INTO roles (name, description) VALUES
    ('ROLE_USER',   'Base role for all authenticated users'),
    ('ROLE_WORKER', 'Scientist role'),
    ('ROLE_ADMIN',  'Administrator role')
    ON CONFLICT (name) DO NOTHING;
