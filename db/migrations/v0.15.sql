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

alter table broker_accounts_tb
    rename broker_type to broker_env;
alter table broker_accounts_tb
    rename broker_name to broker_type;
alter table broker_accounts_tb
    add column broker_name VARCHAR(255) NOT NULL DEFAULT 'DEFAULT_NAME';