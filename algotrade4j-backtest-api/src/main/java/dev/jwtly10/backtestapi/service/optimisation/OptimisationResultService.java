package dev.jwtly10.backtestapi.service.optimisation;

import dev.jwtly10.backtestapi.model.optimisation.OptimisationResult;
import dev.jwtly10.backtestapi.repository.optimisation.OptimisationResultRepository;
import dev.jwtly10.core.optimisation.OptimisationRunResult;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OptimisationResultService {

    private final OptimisationResultRepository resultRepository;

    public OptimisationResultService(OptimisationResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @Transactional
    public void saveOptimisationResult(Long taskId, OptimisationRunResult runResult) {
        OptimisationResult resultEntity = new OptimisationResult();
        resultEntity.setOptimisationTaskId(taskId);
        resultEntity.setParameters(runResult.getParameters());
        resultEntity.setOutput(runResult.getOutput());
        resultRepository.save(resultEntity);
    }
}