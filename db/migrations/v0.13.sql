-- Create daily starting equity table
CREATE TABLE daily_starting_bals_tb
(
    id               BIGSERIAL PRIMARY KEY,
    live_strategy_id BIGSERIAL REFERENCES live_strategies_tb (id),
    date             DATE           NOT NULL,
    balance          NUMERIC(15, 2) NOT NULL,
    equity           NUMERIC(15, 2) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
)