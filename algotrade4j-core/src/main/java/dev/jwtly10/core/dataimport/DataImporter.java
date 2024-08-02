package dev.jwtly10.core.dataimport;

import dev.jwtly10.core.BarSeries;

public interface DataImporter {
    BarSeries importData(String source, CsvFormat format, BarSeries series) throws DataImportException;
}