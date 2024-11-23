ALTER TABLE optimisation_task_tb
    ADD COLUMN res_summary json;


-- Used to populate the new field with existing data
CREATE OR REPLACE PROCEDURE update_res_summary()
    LANGUAGE plpgsql
AS
$$
DECLARE
    task_record RECORD;
BEGIN
    FOR task_record IN
        SELECT DISTINCT optimisation_task_id
        FROM algotrade.optimisation_results_tb
        LOOP
            UPDATE optimisation_task_tb
            SET res_summary = (SELECT json_build_object(
                                              'totalCombinations', COUNT(*),
                                              'successfulRuns', SUM(CASE WHEN (output ->> 'failed')::boolean = false THEN 1 ELSE 0 END),
                                              'failedRuns', SUM(CASE WHEN (output ->> 'failed')::boolean = true THEN 1 ELSE 0 END)
                                      )
                               FROM algotrade.optimisation_results_tb
                               WHERE optimisation_task_id = task_record.optimisation_task_id)
            WHERE id = task_record.optimisation_task_id;
        END LOOP;
END;
$$;

CALL update_res_summary();