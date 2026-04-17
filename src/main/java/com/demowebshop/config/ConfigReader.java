package com.demowebshop.config;

import com.demowebshop.models.CheckoutData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ConfigReader {
    private static final String DEFAULT_CONFIG_PATH = "src/test/resources/config.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    private ConfigReader() {
    }

    private static void loadProperties() {
        String overridePath = System.getProperty("config.file");

        try {
            if (overridePath != null && !overridePath.isBlank()) {
                try (InputStream inputStream = Files.newInputStream(Path.of(overridePath))) {
                    PROPERTIES.load(inputStream);
                }
                return;
            }

            try (InputStream inputStream = Files.newInputStream(Path.of(DEFAULT_CONFIG_PATH))) {
                PROPERTIES.load(inputStream);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load config properties.", exception);
        }
    }

    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing config value for key: " + key);
        }
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue).trim();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    public static long getLong(String key, long defaultValue) {
        return Long.parseLong(get(key, String.valueOf(defaultValue)));
    }

    public static CheckoutData getCheckoutData() {
        return CheckoutData.builder()
                .firstName(get("billing.firstName"))
                .lastName(get("billing.lastName"))
                .email(get("user.email"))
                .company(get("billing.company", ""))
                .country(get("billing.country"))
                .city(get("billing.city"))
                .addressLine1(get("billing.address1"))
                .addressLine2(get("billing.address2", ""))
                .zipCode(get("billing.zip"))
                .phoneNumber(get("billing.phone"))
                .build();
    }
}

