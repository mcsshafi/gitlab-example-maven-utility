package org.example.utility;

import java.io.InputStream;
import java.util.Properties;

public class AppProperties {
    private final static Properties PropertiesInstance;

    static {
        PropertiesInstance = new Properties();
        try {
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
            PropertiesInstance.load(stream);
        } catch (Exception e) { }
    }

    public static String get(String key){
        return (PropertiesInstance == null || PropertiesInstance.isEmpty() ? "" : PropertiesInstance.getProperty(key));
    }
}
