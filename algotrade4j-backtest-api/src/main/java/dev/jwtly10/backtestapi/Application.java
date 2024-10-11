package dev.jwtly10.backtestapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = {"dev.jwtly10.backtestapi", "dev.jwtly10.shared"})
@EnableJpaRepositories(basePackages = {"dev.jwtly10.backtestapi", "dev.jwtly10.shared"})
@ComponentScan(basePackages = {"dev.jwtly10.backtestapi", "dev.jwtly10.core", "dev.jwtly10.shared"})
@EnableCaching
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}