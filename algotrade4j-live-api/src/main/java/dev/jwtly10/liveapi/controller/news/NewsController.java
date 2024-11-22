package dev.jwtly10.liveapi.controller.news;

import dev.jwtly10.core.external.news.forexfactory.ForexFactoryClient;
import dev.jwtly10.core.external.news.forexfactory.ForexFactoryNews;
import dev.jwtly10.shared.exception.ApiException;
import dev.jwtly10.shared.exception.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
@Slf4j
public class NewsController {

    private final ForexFactoryClient forexFactoryClient;
    private final Environment environment;

    public NewsController(ForexFactoryClient forexFactoryClient, Environment environment) {
        this.forexFactoryClient = forexFactoryClient;
        this.environment = environment;
    }

    @GetMapping("/forexfactory")
    public List<ForexFactoryNews> getForexFactoryNews() {
        try {
            if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
                log.debug("Using mocked ForexFactory news data in dev spring profile");
                return forexFactoryClient.getMockedNews();
            }
            return forexFactoryClient.getThisWeeksNews();
        } catch (Exception e) {
            log.error("Error while fetching news from ForexFactory", e);
            throw new ApiException("Error while fetching news from ForexFactory: " + e.getMessage(), ErrorType.INTERNAL_ERROR);
        }
    }
}