package dev.jwtly10.marketdata.news.forexfactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ForexFactoryClient {

    private final OkHttpClient httpClient;

    public ForexFactoryClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ForexFactoryClient() {
        this.httpClient = new OkHttpClient();
    }

    /**
     * <p>
     * Returns forex factory news based on search parameters
     * </p>
     * <p>
     * NB: All times are in UTC
     * </p>
     *
     * @param params search parameters for ForexFactory news search
     * @return List of news items
     * @throws IOException should http req fail
     */
    public List<ForexFactoryNews> searchNews(ForexFactorySearchParams params) throws IOException {
        List<ForexFactoryNews> allNews = getWeeksNews();
        return allNews.stream()
                .filter(news -> params.country() == null || news.country().equalsIgnoreCase(params.country()))
                .filter(news -> params.impact() == null || news.impact().getValue().equalsIgnoreCase(params.impact().getValue()))
                .filter(news -> params.date() == null || news.date().toLocalDate().isEqual(params.date()))
                .collect(Collectors.toList());
    }

    public List<ForexFactoryNews> searchMockedNews(ForexFactorySearchParams params) throws IOException, URISyntaxException {
        List<ForexFactoryNews> allNews = getMockedNews();
        return allNews.stream()
                .filter(news -> params.country() == null || news.country().equalsIgnoreCase(params.country()))
                .filter(news -> params.impact() == null || news.impact().getValue().equalsIgnoreCase(params.impact().getValue()))
                .filter(news -> params.date() == null || news.date().toLocalDate().isEqual(params.date()))
                .collect(Collectors.toList());
    }

    public List<ForexFactoryNews> getMockedNews() throws URISyntaxException, IOException {
        log.info("Fetching mocked news from ForexFactory");
        var path = Paths.get(Objects.requireNonNull(ForexFactoryClient.class.getResource("/forex_factory_mocked_news.json")).toURI());
        var parsed = Files.readString(path);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        return objectMapper.readValue(parsed, new TypeReference<>() {
        });
    }

    // TODO: This should be cached daily, so we don't hit the API every time
    public List<ForexFactoryNews> getWeeksNews() throws IOException {
        log.debug("Fetching news from ForexFactory");
        String url = "https://nfs.faireconomy.media/ff_calendar_thisweek.json";

        Request req = new Request.Builder()
                .url(url)
                .build();

        try (Response res = httpClient.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                throw new RuntimeException("Bad status code from ForexFactory: " + res.code());
            }

            String response = res.body().string();
            ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

            return objectMapper.readValue(response, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error("Failed to fetch new data from ForexFactory: {}", e.getMessage(), e);
            throw e;
        }
    }
}