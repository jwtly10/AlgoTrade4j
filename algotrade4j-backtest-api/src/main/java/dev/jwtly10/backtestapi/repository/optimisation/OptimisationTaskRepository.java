package dev.jwtly10.backtestapi.repository.optimisation;

import dev.jwtly10.backtestapi.model.optimisation.OptimisationState;
import dev.jwtly10.backtestapi.model.optimisation.OptimisationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptimisationTaskRepository extends JpaRepository<OptimisationTask, Long> {
    List<OptimisationTask> findByState(OptimisationState state);

    OptimisationTask findFirstByState(OptimisationState state);
}