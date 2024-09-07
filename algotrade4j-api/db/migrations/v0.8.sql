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