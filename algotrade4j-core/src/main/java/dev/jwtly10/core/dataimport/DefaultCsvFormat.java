package dev.jwtly10.core.dataimport;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.DefaultBar;
import dev.jwtly10.core.Price;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Default CSV format for importing data.
 * <p>
 * The default CSV format assumes that the CSV file has a header and the fields are in the following order:
 *     <ul>
 *         <li>DateTime</li>
 *         <li>Open</li>
 *         <li>High</li>
 *         <li>Low</li>
 *         <li>Close</li>
 *         <li>Volume</li>
 *     </ul>
 *     The delimiter is a comma.
 *     The time period of the bars is specified in the constructor.
 *     The date time is expected to be in the format "yyyy.MM.dd'T'HH:mm". (UTC assumed)
 *     Example:
 *     <pre>
 *         Date,Open,High,Low,Close,Volume
 *         2022.01.02T22:00,16419.7,16526.0,16310.6,16512.8,209249
 *         2022.01.03T22:00,16516.0,16579.2,16155.8,16276.6,255990
 *         2022.01.04T22:00,16278.4,16283.4,15768.0,15768.6,396027
 *         2022.01.05T22:00,15779.4,15910.6,15614.2,15808.3,629752
 *         2022.01.06T22:00,15826.0,15867.8,15525.2,15586.6,449885
 *    </pre>
 * </p>
 */
public class DefaultCsvFormat implements CsvFormat {
    private final Duration timePeriod;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd'T'HH:mm");


    public DefaultCsvFormat(Duration timePeriod) {
        this.timePeriod = timePeriod;
    }

    @Override
    public boolean hasHeader() {
        return true;
    }

    @Override
    public String getDelimiter() {
        return ",";
    }

    @Override
    public Bar parseBar(String[] fields) {
        return DefaultBar.builder()
                .timePeriod(timePeriod)
                .dateTime(LocalDateTime.parse(fields[0], DATE_TIME_FORMATTER))
                .open(new Price(Double.parseDouble(fields[1])))
                .high(new Price(Double.parseDouble(fields[2])))
                .low(new Price(Double.parseDouble(fields[3])))
                .close(new Price(Double.parseDouble(fields[4])))
                .volume(Long.parseLong(fields[5]))
                .build();
    }

    @Override
    public Duration getTimePeriod() {
        return timePeriod;
    }
}