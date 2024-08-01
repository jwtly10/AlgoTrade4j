package dev.jwtly10.core.dataimport;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.BarSeries;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvDataImporter implements DataImporter {

    @Override
    public BarSeries importData(String filePath, CsvFormat format, BarSeries series) throws DataImportException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            if (format.hasHeader()) {
                reader.readLine(); // Skip header
            }
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(format.getDelimiter());
                try {
                    Bar bar = format.parseBar(fields);
                    series.addBar(bar);
                } catch (Exception e) {
                    throw new DataImportException("Error parsing line: " + line, e);
                }
            }
        } catch (IOException e) {
            throw new DataImportException("Error reading file: " + filePath, e);
        }
        return series;
    }
}