CREATE DATABASE "algotrade4j-db";

CREATE SCHEMA algotrade;

SET search_path TO algotrade;

CREATE TABLE users_tb
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50) UNIQUE  NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255)        NOT NULL,
    first_name VARCHAR(50),
    last_name  VARCHAR(50),
    role       VARCHAR(20)         NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_login_log_tb
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT                   NOT NULL,
    ip_address VARCHAR(45)              NOT NULL,
    login_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users_tb (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_user_login_log_user_id ON user_login_log_tb (user_id);
CREATE INDEX idx_user_login_log_login_time ON user_login_log_tb (login_time);

CREATE TABLE optimisation_task_tb
(
    id            BIGSERIAL PRIMARY KEY,
    config        JSON        NOT NULL,
    progress_info JSON,
    state         VARCHAR(20) NOT NULL,
    error_message TEXT,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE optimisation_results_tb
(
    id                   BIGSERIAL PRIMARY KEY,
    optimisation_task_id BIGINT REFERENCES optimisation_task_tb (id),
    parameters           JSON NOT NULL,
    output               JSON NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE optimisation_user_tb
(
    id                   BIGSERIAL PRIMARY KEY,
    optimisation_task_id BIGINT REFERENCES optimisation_task_tb (id),
    user_id              BIGINT  NOT NULL,
    active               BOOLEAN NOT NULL         DEFAULT TRUE,
    created_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users_tb (id)
            ON DELETE CASCADE,
    CONSTRAINT unique_optimisation_task_user
        UNIQUE (optimisation_task_id, user_id)
);

CREATE INDEX idx_optimisation_results_task_id ON optimisation_results_tb (optimisation_task_id);
CREATE INDEX idx_optimisation_user_task_id ON optimisation_user_tb (optimisation_task_id);
CREATE INDEX idx_optimisation_user_user_id ON optimisation_user_tb (user_id);;