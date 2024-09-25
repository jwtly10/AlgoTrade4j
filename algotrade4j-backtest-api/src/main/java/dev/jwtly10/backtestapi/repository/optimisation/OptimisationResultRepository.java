package dev.jwtly10.backtestapi.repository.optimisation;

import dev.jwtly10.backtestapi.model.optimisation.OptimisationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptimisationResultRepository extends JpaRepository<OptimisationResult, Long> {
    List<OptimisationResult> findByOptimisationTaskId(Long taskId);
}