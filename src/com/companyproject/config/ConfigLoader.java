package com.companyproject.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        // Try to load from user home directory first (for local development)
        String homeConfig = Paths.get(System.getProperty("user.home"), ".companyproject", "application.properties").toString();
        if (loadFromFile(homeConfig)) {
            logger.info("Loaded configuration from home directory: {}", homeConfig);
            return;
        }

        // Try to load from current working directory
        String cwdConfig = "application.properties";
        if (loadFromFile(cwdConfig)) {
            logger.info("Loaded configuration from current directory: {}", cwdConfig);
            return;
        }

        // Try to load from project root (for IDE/NetBeans)
        String projectConfig = Paths.get(System.getProperty("user.dir"), "application.properties").toString();
        if (loadFromFile(projectConfig)) {
            logger.info("Loaded configuration from project directory: {}", projectConfig);
            return;
        }

        logger.warn("No application.properties found. Using environment variables and defaults.");
    }

    private static boolean loadFromFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    properties.load(fis);
                    return true;
                }
            }
        } catch (IOException e) {
            logger.debug("Could not load properties from {}: {}", path, e.getMessage());
        }
        return false;
    }

    public static String get(String key, String defaultValue) {
        // Priority: properties file > environment variable > default value
        String fileValue = properties.getProperty(key);
        if (fileValue != null && !fileValue.trim().isEmpty()) {
            return fileValue.trim();
        }

        String envValue = System.getenv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        return defaultValue;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for {}: {}", key, value);
            return defaultValue;
        }
    }

    private ConfigLoader() {
    }
}
