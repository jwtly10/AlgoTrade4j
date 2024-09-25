package dev.jwtly10.backtestapi.repository.optimisation;

import dev.jwtly10.backtestapi.model.optimisation.OptimisationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptimisationUserRepository extends JpaRepository<OptimisationUser, Long> {
    List<OptimisationUser> findByUserId(Long userId);

    List<OptimisationUser> findByUserIdAndActiveTrue(Long user);

    boolean existsByOptimisationTaskIdAndUserIdAndActiveTrue(Long optimisationTaskId, Long userId);

    List<OptimisationUser> findByUserIdAndActiveTrueOrderByOptimisationTaskIdDesc(Long user);

    Optional<OptimisationUser> findByOptimisationTaskIdAndUserIdAndActiveTrue(Long optimisationTaskId, Long userId);

    Optional<OptimisationUser> findByOptimisationTaskIdAndUserId(Long optimisationTaskId, Long userId);
}