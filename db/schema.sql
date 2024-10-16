-- Contains the full schema, taking into account all migration scripts
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
    user_agent VARCHAR(255)             NOT NULL DEFAULT 'UNKNOWN',
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
CREATE INDEX idx_optimisation_user_user_id ON optimisation_user_tb (user_id);

CREATE TABLE broker_accounts_tb
(
    id              BIGSERIAL PRIMARY KEY,
    broker_name     VARCHAR(255) NOT NULL,
    broker_type     VARCHAR(255) NOT NULL,
    broker_env      VARCHAR(255) NOT NULL, -- LIVE/DEMO
    account_id      VARCHAR(255) NOT NULL,
    active          BOOLEAN   DEFAULT TRUE,
    initial_balance INTEGER      NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_broker_account_id ON broker_accounts_tb (account_id);

CREATE TABLE live_strategies_tb
(
    id                BIGSERIAL PRIMARY KEY,
    strategy_name     VARCHAR(255) NOT NULL UNIQUE,
    broker_account_id BIGINT       NOT NULL,
    config            JSON         NOT NULL,
    stats             JSON,
    is_active         BOOLEAN   DEFAULT FALSE,
    is_hidden         BOOLEAN   DEFAULT FALSE,
    telegram_chat_id  VARCHAR(255),
    last_error_msg    VARCHAR(255),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (broker_account_id) REFERENCES broker_accounts_tb (id)
);

CREATE INDEX idx_live_strategies_strategy_name ON live_strategies_tb (strategy_name);

CREATE TABLE user_action_log_tb
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT       NOT NULL,
    action    VARCHAR(255) NOT NULL,
    meta_data JSON,
    timestamp TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_action
        FOREIGN KEY (user_id)
            REFERENCES users_tb (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_user_action_log_user_id ON user_action_log_tb (user_id);
CREATE INDEX idx_user_action_log_timestamp ON user_action_log_tb (timestamp);

CREATE TABLE mt5_credentials_tb
(
    id         BIGSERIAL PRIMARY KEY,
    broker_id  BIGINT       NOT NULL,
    password   VARCHAR(255) NOT NULL,
    server     VARCHAR(255) NOT NULL,
    path       VARCHAR(255) NOT NULL,
    timezone   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_broker_mt5
        FOREIGN KEY (broker_id)
            REFERENCES broker_accounts_tb (id)
            ON DELETE CASCADE
);