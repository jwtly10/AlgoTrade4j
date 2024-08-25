package dev.jwtly10.core.event.async;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.model.Instrument;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Event representing the event progress of a given strategy run
 */
@Getter
public class AsyncProgressEvent extends BaseEvent {
    private final int totalDays;
    private final int currentIndex;
    private final double percentageComplete;
    private final ZonedDateTime fromDay;
    private final ZonedDateTime toDay;
    private final ZonedDateTime currentDay;
    private final int ticksModelled;

    public AsyncProgressEvent(String strategyId, Instrument instrument, ZonedDateTime fromDay, ZonedDateTime toDay, ZonedDateTime currentDay, int ticksModelled) {
        super(strategyId, "PROGRESS", instrument);
        this.ticksModelled = ticksModelled;
        this.fromDay = fromDay.truncatedTo(ChronoUnit.DAYS);
        this.toDay = toDay.truncatedTo(ChronoUnit.DAYS);
        this.currentDay = currentDay.truncatedTo(ChronoUnit.DAYS);

        LocalDate fromLocalDate = this.fromDay.toLocalDate();
        LocalDate toLocalDate = this.toDay.toLocalDate();
        LocalDate currentLocalDate = this.currentDay.toLocalDate();

        this.totalDays = (int) ChronoUnit.DAYS.between(fromLocalDate, toLocalDate) + 1;
        this.currentIndex = (int) ChronoUnit.DAYS.between(fromLocalDate, currentLocalDate) + 1;

        this.percentageComplete = (double) this.currentIndex / this.totalDays * 100;
    }
}