package dev.jwtly10.core.dataimport;

import dev.jwtly10.core.Bar;
import dev.jwtly10.core.BarSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CsvDataImporterTest {

    @TempDir
    Path tempDir;

    @Mock
    private BarSeries mockBarSeries;

    private CsvDataImporter importer;
    private DefaultCsvFormat format;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        importer = new CsvDataImporter();
        format = new DefaultCsvFormat(Duration.ofDays(1));
    }

    @Test
    void testImportData() throws DataImportException, IOException {
        Path csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile,
                "Date,Open,High,Low,Close,Volume\n" +
                        "2022.01.02T22:00,16419.7,16526.0,16310.6,16512.8,209249\n" +
                        "2022.01.03T22:00,16516.0,16579.2,16155.8,16276.6,255990\n"
        );

        BarSeries result = importer.importData(csvFile.toString(), format, mockBarSeries);

        verify(mockBarSeries, times(2)).addBar(any(Bar.class));

        assertEquals(mockBarSeries, result);
    }

    @Test
    void testImportDataWithInvalidFile() {
        assertThrows(DataImportException.class, () ->
                importer.importData("non_existent_file.csv", format, mockBarSeries)
        );
    }
}