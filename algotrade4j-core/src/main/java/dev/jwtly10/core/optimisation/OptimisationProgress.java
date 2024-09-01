package dev.jwtly10.core.optimisation;

import lombok.Data;

@Data
public class OptimisationProgress {
    private final long startTime;
    private final int totalTasks;
    private long lastUpdateTime;
    private int completedTasks;
    private long estimatedTimeRemaining;

    public OptimisationProgress(int totalTasks) {
        this.startTime = System.currentTimeMillis();
        this.lastUpdateTime = startTime;
        this.totalTasks = totalTasks;
        this.completedTasks = 0;
        this.estimatedTimeRemaining = -1; // -1 indicates not yet calculated
    }

    public void updateProgress(int newCompletedTasks) {
        long now = System.currentTimeMillis();
        long timeSinceLastUpdate = now - lastUpdateTime;

        if (newCompletedTasks > this.completedTasks && timeSinceLastUpdate > 0) {
            long tasksDone = newCompletedTasks - this.completedTasks;
            long millisPerTask = timeSinceLastUpdate / tasksDone;
            long remainingTasks = totalTasks - newCompletedTasks;
            this.estimatedTimeRemaining = remainingTasks * millisPerTask;
        }

        this.completedTasks = newCompletedTasks;
        this.lastUpdateTime = now;
    }

    public double getProgressPercentage() {
        return (double) completedTasks / totalTasks * 100;
    }

    public int getRemainingTasks() {
        return totalTasks - completedTasks;
    }
}