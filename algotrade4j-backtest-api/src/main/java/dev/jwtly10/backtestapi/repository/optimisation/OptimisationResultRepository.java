package dev.jwtly10.backtestapi.repository.optimisation;

import dev.jwtly10.backtestapi.model.optimisation.OptimisationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OptimisationResultRepository extends JpaRepository<OptimisationResult, Long> {
    List<OptimisationResult> findByOptimisationTaskId(Long taskId);

    @Query(value = """
            SELECT
                COUNT(*) as total_combinations,
                SUM(CASE WHEN (output->>'failed')::boolean = false THEN 1 ELSE 0 END) as successful_runs,
                SUM(CASE WHEN (output->>'failed')::boolean = true THEN 1 ELSE 0 END) as failed_runs
            FROM algotrade.optimisation_results_tb
            WHERE optimisation_task_id = :taskId
            """, nativeQuery = true)
    Map<String, Long> getResultsSummary(@Param("taskId") Long taskId);
}