package dev.jwtly10.marketdata.oanda;

import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.core.model.Instrument;
import lombok.Getter;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
public class InstrumentCandlesRequest {
    private final OandaClient oandaClient;
    private final Instrument instrument;
    private ZonedDateTime from;
    private ZonedDateTime to;
    private Duration period;

    InstrumentCandlesRequest(OandaClient oandaClient, Instrument instrument) {
        this.oandaClient = oandaClient;
        this.instrument = instrument;
    }

    public InstrumentCandlesRequest from(ZonedDateTime from) {
        this.from = from;
        return this;
    }

    public InstrumentCandlesRequest to(ZonedDateTime to) {
        this.to = to;
        return this;
    }

    public InstrumentCandlesRequest granularity(Duration period) {
        this.period = period;
        return this;
    }

    public List<DefaultBar> fetch() throws Exception {
        return oandaClient.executeRequest(this);
    }
}