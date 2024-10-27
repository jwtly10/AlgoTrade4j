package dev.jwtly10.liveapi.controller.news;

import dev.jwtly10.marketdata.news.forexfactory.ForexFactoryClient;
import dev.jwtly10.marketdata.news.forexfactory.ForexFactoryNews;
import dev.jwtly10.shared.exception.ApiException;
import dev.jwtly10.shared.exception.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
@Slf4j
public class NewsController {

    private final ForexFactoryClient forexFactoryClient;

    public NewsController(ForexFactoryClient forexFactoryClient) {
        this.forexFactoryClient = forexFactoryClient;
    }

    @GetMapping("/forexfactory")
    public List<ForexFactoryNews> getForexFactoryNews() {
        try {
            return forexFactoryClient.getMockedNews();
        } catch (Exception e) {
            log.error("Error while fetching news from ForexFactory", e);
            throw new ApiException("Error while fetching news from ForexFactory: " + e.getMessage(), ErrorType.INTERNAL_ERROR);
        }
    }
}