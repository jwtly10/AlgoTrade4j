package dev.jwtly10.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
public class SystemInfoController {
    private final Map<String, String> versionInfo;
    private final Environment environment;
    private final Date startTime;


    @Autowired
    public SystemInfoController(Map<String, String> versionInfo, Environment environment) {
        this.versionInfo = versionInfo;
        this.environment = environment;
        this.startTime = new Date();
    }

    @GetMapping("/version")
    public Map<String, String> getVersion() {
        Map<String, String> info = new HashMap<>(versionInfo);
        info.put("environment", getEnvironment());
        info.put("startTime", formatDateTime(startTime));
        return info;
    }

    @GetMapping("/monitor")
    public Map<String, Object> getMonitorInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;
        long usedMaxMemory = maxMemory - freeMemory;

        Map<String, Object> info = new HashMap<>(versionInfo);
        info.putAll(Map.of(
                "javaVersion", System.getProperty("java.version"),
                "timestamp", new Date().toString(),
                "environment", getEnvironment(),
                "startTime", formatDateTime(startTime),
                "uptime", getUptime(),
                "jvmMemory", Map.of(
                        "current", Map.of(
                                "total", formatBytes(totalMemory),
                                "free", formatBytes(freeMemory),
                                "used", formatBytes(usedMemory)
                        ),
                        "max", formatBytes(maxMemory),
                        "usedOfMax", formatBytes(usedMaxMemory),
                        "percentUsed", String.format("%.2f%%", (double) usedMaxMemory / maxMemory * 100)
                )
        ));
        return info;
    }

    private String getEnvironment() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev") ? "dev" : "production";
    }

    private String getUptime() {
        long uptime = System.currentTimeMillis() - startTime.getTime();
        long days = uptime / (24 * 60 * 60 * 1000);
        long hours = (uptime % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (uptime % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (uptime % (60 * 1000)) / 1000;
        return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
    }

    private String formatBytes(long bytes) {
        final long kilobyte = 1024;
        final long megabyte = kilobyte * 1024;
        final long gigabyte = megabyte * 1024;

        if (bytes >= gigabyte) {
            return String.format("%.2f GB", (double) bytes / gigabyte);
        } else if (bytes >= megabyte) {
            return String.format("%.2f MB", (double) bytes / megabyte);
        } else if (bytes >= kilobyte) {
            return String.format("%.2f KB", (double) bytes / kilobyte);
        } else {
            return bytes + " B";
        }
    }

    private String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}