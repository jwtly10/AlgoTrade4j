CREATE TABLE live_strategy_log_tb
(
    id               BIGSERIAL PRIMARY KEY,
    live_strategy_id BIGSERIAL    NOT NULL REFERENCES live_strategies_tb (id),
    level            VARCHAR(10)  NOT NULL,
    message          VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (live_strategy_id) REFERENCES live_strategies_tb (id)
);