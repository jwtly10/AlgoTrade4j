-- Revision ID: 93bd0fc162e9c69348c379d51885bd97145c8b23
-- Refactoring usage of Number types
-- Updates all config where initialCash": {"value": 10000.00000} ->  "initialCash": "10000"
UPDATE optimisation_task_tb
SET config = jsonb_set(
        config::jsonb,
        '{initialCash}',
        to_jsonb((config ->> 'initialCash')::json ->> 'value')::jsonb
             )
WHERE config ->> 'initialCash' IS NOT NULL
  AND (config -> 'initialCash')::jsonb ? 'value';

-- Updates all config where spread": {"value": 10} ->  "spread": "10"
UPDATE optimisation_task_tb
SET config = jsonb_set(
        config::jsonb,
        '{spread}',
        to_jsonb((config ->> 'spread')::json ->> 'value')::jsonb
             )
WHERE config ->> 'spread' IS NOT NULL
  AND (config -> 'spread')::jsonb ? 'value';

-- Validates and sets all 'strings' to integers to match java int type
UPDATE optimisation_task_tb
SET config = jsonb_set(
        config::jsonb,
        '{spread}',
        to_jsonb(CAST(CAST(config ->> 'spread' AS numeric) AS INTEGER))
             )
WHERE config ->> 'spread' IS NOT NULL
  AND config ->> 'spread' ~ '^[0-9]*\.?[0-9]+$';
-- This checks if the value is a valid number

-- Live trading service SQL

CREATE TABLE broker_accounts_tb
(
    id              BIGSERIAL PRIMARY KEY,
    broker_name     VARCHAR(255) NOT NULL,
    broker_type     VARCHAR(10)  NOT NULL, -- LIVE/DEMO
    account_id VARCHAR(255) NOT NULL,
    active     BOOLEAN DEFAULT TRUE,
    initial_balance INTEGER      NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_broker_account_id ON broker_accounts_tb (account_id);

CREATE TABLE live_strategies_tb
(
    id                BIGSERIAL PRIMARY KEY,
    strategy_name VARCHAR(255) NOT NULL,
    broker_account_id BIGINT       NOT NULL,
    config            JSON         NOT NULL,
    stats             JSON,
    is_active         BOOLEAN   DEFAULT FALSE,
    is_hidden         BOOLEAN   DEFAULT FALSE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (broker_account_id) REFERENCES broker_accounts_tb (id)
);

drop table live_strategies_tb;

drop table broker_accounts_tb;