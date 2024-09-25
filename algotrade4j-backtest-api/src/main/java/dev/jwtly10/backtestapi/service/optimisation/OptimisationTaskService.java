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

    public OptimisationTaskService(OptimisationTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
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
}