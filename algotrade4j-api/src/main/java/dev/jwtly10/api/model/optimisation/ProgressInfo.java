package dev.jwtly10.api.model.optimisation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProgressInfo {
    @JsonProperty("percentage")
    private double progressPercentage;

    @JsonProperty("completedTasks")
    private int completedTasks;

    @JsonProperty("remainingTasks")
    private int remainingTasks;

    @JsonProperty("estimatedTimeMs")
    private long estimatedTimeRemaining;

    public ProgressInfo(double progressPercentage, int completedTasks, int remainingTasks, long estimatedTimeRemaining) {
        this.progressPercentage = progressPercentage;
        this.completedTasks = completedTasks;
        this.remainingTasks = remainingTasks;
        this.estimatedTimeRemaining = estimatedTimeRemaining;
    }
}