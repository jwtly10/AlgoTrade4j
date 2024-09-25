package dev.jwtly10.core.data;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.model.BarSeries;
import dev.jwtly10.core.model.DefaultBarSeries;
import dev.jwtly10.core.model.Instrument;

import java.time.Duration;

public class DefaultDataManagerFactory implements DataManagerFactory {
    @Override
    public DefaultDataManager createDataManager(Instrument instrument, Duration period, EventPublisher eventPublisher, DataProvider dataProvider) {
        BarSeries barSeries = new DefaultBarSeries(4000);
        dataProvider.setDataSpeed(DataSpeed.INSTANT);

        return new DefaultDataManager(
                "optimisation-run",
                instrument,
                dataProvider,
                period,
                barSeries,
                eventPublisher
        );
    }
}