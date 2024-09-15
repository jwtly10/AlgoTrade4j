package dev.jwtly10.shared.controller;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApplicationInfo {

    @Value("${application.version}")
    private String version;

}