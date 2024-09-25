package dev.jwtly10.core.data;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.model.Instrument;

import java.time.Duration;

public interface DataManagerFactory {
    DataManager createDataManager(Instrument instrument, Duration period, EventPublisher eventPublisher, DataProvider dataProvider);
}