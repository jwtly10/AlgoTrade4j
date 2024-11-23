package dev.jwtly10.backtestapi.service.optimisation;

import dev.jwtly10.backtestapi.model.optimisation.OptimisationState;
import dev.jwtly10.backtestapi.model.optimisation.OptimisationTask;
import dev.jwtly10.backtestapi.model.optimisation.ProgressInfo;
import dev.jwtly10.backtestapi.repository.optimisation.OptimisationTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OptimisationTaskService {
    private final OptimisationTaskRepository taskRepository;
    private final OptimisationResultService resultService;

    public OptimisationTaskService(OptimisationTaskRepository taskRepository, OptimisationResultService resultService) {
        this.taskRepository = taskRepository;
        this.resultService = resultService;
    }

    public void save(OptimisationTask task) {
        taskRepository.save(task);
    }

    public OptimisationTask findFirstByState(OptimisationState state) {
        return taskRepository.findFirstByState(state);
    }

    @Transactional
    public void updateProgress(Long taskId, ProgressInfo progress) {
        OptimisationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("OptimisationTask not found with id: " + taskId));

        if (!task.getState().equals(OptimisationState.RUNNING)) {
            throw new RuntimeException("Can't update progress of a task that is not running");
        }

        task.setProgressInfo(progress);
        taskRepository.save(task);
    }

    /**
     * Figures out and sets basic stats for the task
     *
     * @param taskId The task id
     */
    public void setBasicStats(long taskId) {
        OptimisationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("OptimisationTask not found with id: " + taskId));

        task.setResultSummary(resultService.getSummary(taskId));
        taskRepository.save(task);
    }
}