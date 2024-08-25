package dev.jwtly10.api.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Map;

@Configuration
public class VersionConfig {
    @Bean
    public Map<String, String> versionInfo() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new ClassPathResource("version.json").getInputStream());
        return Map.of(
                "version", root.get("version").asText(),
                "commit", root.get("commit").asText()
        );
    }
}