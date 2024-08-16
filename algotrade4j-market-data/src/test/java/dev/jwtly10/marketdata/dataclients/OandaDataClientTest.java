package dev.jwtly10.marketdata.dataclients;

import dev.jwtly10.core.instruments.OandaInstrument;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.marketdata.oanda.OandaClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OandaDataClientTest {

    @Mock
    private OandaClient mockApiClient;

    private OandaDataClient oandaDataClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        oandaDataClient = new OandaDataClient(mockApiClient);
    }

    @Test
    void testFetchCandlesIterator_MultipleBatches() throws Exception {
        ZonedDateTime from = ZonedDateTime.now().minusDays(2);
        ZonedDateTime to = ZonedDateTime.now();
        Duration period = Duration.ofHours(1);

        DefaultBar bar1 = new DefaultBar("EUR_USD", period, from,
                new Number(100), new Number(101), new Number(99), new Number(100), new Number(1000));
        DefaultBar bar2 = new DefaultBar("EUR_USD", period, from.plusHours(1),
                new Number(100), new Number(102), new Number(98), new Number(101), new Number(1100));
        DefaultBar bar3 = new DefaultBar("EUR_USD", period, from.plusHours(2),
                new Number(101), new Number(103), new Number(100), new Number(102), new Number(1200));

        when(mockApiClient.fetchBars(eq(OandaInstrument.EUR_USD), eq(from), any(), eq(period)))
                .thenReturn(Arrays.asList(bar1, bar2));
        when(mockApiClient.fetchBars(eq(OandaInstrument.EUR_USD), eq(from.plusHours(2)), any(), eq(period)))
                .thenReturn(Collections.singletonList(bar3));
        when(mockApiClient.fetchBars(eq(OandaInstrument.EUR_USD), eq(from.plusHours(3)), any(), eq(period)))
                .thenReturn(Collections.emptyList());

        Iterator<Bar> iterator = oandaDataClient.fetchCandlesIterator("EUR_USD", from, to, period);

        assertTrue(iterator.hasNext());
        assertEquals(bar1, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(bar2, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(bar3, iterator.next());
        assertFalse(iterator.hasNext());

        verify(mockApiClient, times(3)).fetchBars(eq(OandaInstrument.EUR_USD), any(), any(), any());
    }

    @Test
    void testFetchCandlesIterator_SingleBatch() throws Exception {
        ZonedDateTime from = ZonedDateTime.now().minusDays(1);
        ZonedDateTime to = ZonedDateTime.now();
        Duration period = Duration.ofHours(1);

        DefaultBar bar = new DefaultBar("EUR_USD", period, from,
                new Number(100), new Number(101), new Number(99), new Number(100), new Number(1000));

        when(mockApiClient.fetchBars(eq(OandaInstrument.EUR_USD), eq(from), eq(to), eq(period)))
                .thenReturn(Collections.singletonList(bar))
                .thenReturn(Collections.emptyList());

        Iterator<Bar> iterator = oandaDataClient.fetchCandlesIterator("EUR_USD", from, to, period);

        assertTrue(iterator.hasNext());
        assertEquals(bar, iterator.next());
        assertFalse(iterator.hasNext());

        verify(mockApiClient, times(2)).fetchBars(eq(OandaInstrument.EUR_USD), any(), any(), any());
    }

    @Test
    void testFetchCandlesIterator_NoData() throws Exception {
        ZonedDateTime from = ZonedDateTime.now().minusDays(1);
        ZonedDateTime to = ZonedDateTime.now();
        Duration period = Duration.ofHours(1);

        when(mockApiClient.fetchBars(eq(OandaInstrument.EUR_USD), eq(from), eq(to), eq(period)))
                .thenReturn(Collections.emptyList());

        Iterator<Bar> iterator = oandaDataClient.fetchCandlesIterator("EUR_USD", from, to, period);

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);

        verify(mockApiClient, times(1)).fetchBars(eq(OandaInstrument.EUR_USD), any(), any(), any());
    }

    @Test
    void testFetchCandlesIterator_ErrorHandling() throws Exception {
        ZonedDateTime from = ZonedDateTime.now().minusDays(1);
        ZonedDateTime to = ZonedDateTime.now();
        Duration period = Duration.ofHours(1);

        when(mockApiClient.fetchBars(eq(OandaInstrument.EUR_USD), eq(from), eq(to), eq(period)))
                .thenThrow(new RuntimeException("API Error"));

        Iterator<Bar> iterator = oandaDataClient.fetchCandlesIterator("EUR_USD", from, to, period);

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);

        verify(mockApiClient, times(1)).fetchBars(eq(OandaInstrument.EUR_USD), any(), any(), any());
    }

    @Test
    void testGetInstrumentFromString() {
        ZonedDateTime from = ZonedDateTime.now();
        ZonedDateTime to = from.plusDays(1);
        Duration period = Duration.ofHours(1);

        // Test valid instrument
        when(mockApiClient.fetchBars(eq(OandaInstrument.EUR_USD), eq(from), eq(to), eq(period)))
                .thenReturn(Collections.singletonList(
                        new DefaultBar("EUR_USD", period, from,
                                new Number(100), new Number(101), new Number(99), new Number(100), new Number(1000))
                ));

        Iterator<Bar> iterator = oandaDataClient.fetchCandlesIterator("EUR_USD", from, to, period);
        assertDoesNotThrow(() -> {
            assertTrue(iterator.hasNext());
            assertNotNull(iterator.next());
        });

        // Verify that fetchBars was called once with the correct instrument
        verify(mockApiClient, times(1)).fetchBars(eq(OandaInstrument.EUR_USD), any(), any(), any());
    }
}