package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class utils {
    public static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = utils.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Configuration file 'config.properties' not found in classpath");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration: " + e.getMessage(), e);
        }
        return props;
    }
}
