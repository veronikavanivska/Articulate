CREATE TABLE profile_user (
                                      user_id    BIGINT PRIMARY KEY,
                                      fullname   VARCHAR(120),
                                      bio        VARCHAR(1000),
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE profile_worker (
                                        user_id    BIGINT PRIMARY KEY
                                            REFERENCES profile_user(user_id) ON DELETE CASCADE,
                                        degree_title VARCHAR(100),
                                        unit_name    VARCHAR(200),
                                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE profile_admin (
                                       user_id    BIGINT PRIMARY KEY
                                           REFERENCES profile_user(user_id) ON DELETE CASCADE,
                                       unit_name  VARCHAR(200),
                                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);