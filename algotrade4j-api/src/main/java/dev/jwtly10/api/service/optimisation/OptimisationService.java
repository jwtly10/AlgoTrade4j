package dev.jwtly10.api.service.optimisation;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.jwtly10.api.exception.ErrorType;
import dev.jwtly10.api.exception.StrategyManagerException;
import dev.jwtly10.api.model.StrategyConfig;
import dev.jwtly10.api.model.optimisation.*;
import dev.jwtly10.api.repository.optimisation.OptimisationResultRepository;
import dev.jwtly10.api.repository.optimisation.OptimisationTaskRepository;
import dev.jwtly10.api.repository.optimisation.OptimisationUserRepository;
import dev.jwtly10.api.utils.ConfigConverter;
import dev.jwtly10.core.optimisation.OptimisationConfig;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OptimisationService {
    private final OptimisationUserRepository userRepository;
    private final OptimisationTaskRepository taskRepository;
    private final OptimisationResultRepository resultRepository;

    public OptimisationService(OptimisationUserRepository userRepository, OptimisationTaskRepository taskRepository, OptimisationResultRepository resultRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.resultRepository = resultRepository;
    }

    @Transactional
    public OptimisationTask queueOptimisation(StrategyConfig strategyConfig, Long userId) throws JsonProcessingException {
        validateOptimisationConfig(strategyConfig);

        // Convert config
        OptimisationConfig config = ConfigConverter.convertToOptimisationConfig(strategyConfig);
        OptimisationTask task = new OptimisationTask();

        // Save task
        task.setConfig(config);
        task.setState(OptimisationState.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        // Save task against user
        OptimisationUser userTask = new OptimisationUser();
        userTask.setOptimisationTaskId(task.getId());
        userTask.setUserId(userId);
        userTask.setActive(true);
        userRepository.save(userTask);

        return task;
    }

    public List<OptimisationTask> getUserTasks(Long userId) {
        List<OptimisationUser> userTasks = userRepository.findByUserIdAndActiveTrueOrderByOptimisationTaskIdDesc(userId);
        return userTasks.stream()
                .map(userTask -> taskRepository.findById(userTask.getOptimisationTaskId())
                        .orElseThrow(() -> new StrategyManagerException("Task not found", ErrorType.NOT_FOUND)))
                .collect(Collectors.toList());
    }

    public void shareTask(Long taskId, Long userId, Long targetUserId) {
        // Throw if this task doesn't exist or is not active for the sharing user
        userRepository.findByOptimisationTaskIdAndUserIdAndActiveTrue(taskId, userId)
                .orElseThrow(() -> new StrategyManagerException("Task not found or not active for sharing user", ErrorType.NOT_FOUND));

        // Check if the target user already has this task (active or inactive)
        Optional<OptimisationUser> existingTask = userRepository.findByOptimisationTaskIdAndUserId(taskId, targetUserId);

        if (existingTask.isPresent()) {
            OptimisationUser task = existingTask.get();
            if (task.isActive()) {
                throw new StrategyManagerException("The user can already access this run.", ErrorType.BAD_REQUEST);
            } else {
                // If the task exists but is inactive, reactivate it
                task.setActive(true);
                userRepository.save(task);
            }
        } else {
            // If the task doesn't exist for the target user, create a new entry
            OptimisationUser sharedTask = new OptimisationUser();
            sharedTask.setOptimisationTaskId(taskId);
            sharedTask.setUserId(targetUserId);
            sharedTask.setActive(true);
            userRepository.save(sharedTask);
        }
    }

    public OptimisationTask getTask(Long taskId, Long userId) {
        OptimisationUser userTask = userRepository.findByOptimisationTaskIdAndUserIdAndActiveTrue(taskId, userId)
                .orElseThrow(() -> new StrategyManagerException("Task not found or not accessible", ErrorType.NOT_FOUND));

        return taskRepository.findById(userTask.getOptimisationTaskId())
                .orElseThrow(() -> new StrategyManagerException("Task not found", ErrorType.NOT_FOUND));
    }

    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        OptimisationUser userTask = userRepository.findByOptimisationTaskIdAndUserIdAndActiveTrue(taskId, userId)
                .orElseThrow(() -> new StrategyManagerException("Task not found or not accessible", ErrorType.NOT_FOUND));

        OptimisationTask task = taskRepository.findById(userTask.getOptimisationTaskId())
                .orElseThrow(() -> new StrategyManagerException("Task not found", ErrorType.NOT_FOUND));

        if (task.getState().equals(OptimisationState.RUNNING)) {
            throw new StrategyManagerException("Cannot delete a running task", ErrorType.BAD_REQUEST);
        }

        // Deactivate the task for the user
        userTask.setActive(false);
        userRepository.save(userTask);
    }

    @Transactional
    public List<OptimisationResultDTO> getResultsForTask(Long taskId, Long userId) {
        OptimisationUser userTask = userRepository.findByOptimisationTaskIdAndUserIdAndActiveTrue(taskId, userId)
                .orElseThrow(() -> new StrategyManagerException("Task not found or not accessible", ErrorType.NOT_FOUND));

        OptimisationTask task = taskRepository.findById(userTask.getOptimisationTaskId())
                .orElseThrow(() -> new StrategyManagerException("Task not found", ErrorType.NOT_FOUND));

        // Check if the task is in a state where results should be available
        if (task.getState() != OptimisationState.COMPLETED) {
            throw new StrategyManagerException("Results are not available for this task", ErrorType.BAD_REQUEST);
        }

        List<OptimisationResult> results = resultRepository.findByOptimisationTaskId(taskId);
        log.info("Found {} results for taskId {}", results.size(), taskId);
        return results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private OptimisationResultDTO convertToDTO(OptimisationResult result) {
        return new OptimisationResultDTO(
                result.getId(),
                result.getParameters(),
                result.getOutput()
        );
    }

    private void validateOptimisationConfig(StrategyConfig config) throws IllegalArgumentException {
        OptimisationConfig optimisationConfig = ConfigConverter.convertToOptimisationConfig(config);
        optimisationConfig.validate();
    }
}