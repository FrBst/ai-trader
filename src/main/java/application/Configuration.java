package application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// Thanks to matsev https://stackoverflow.com/questions/26908854.
public class Configuration {

    private static final String CONFIGURATION_FILE = "/configuration.properties";
    private static final Properties properties;

    static {
        properties = new Properties();
        try (InputStream inputStream = Configuration.class.getResourceAsStream(CONFIGURATION_FILE)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file " + CONFIGURATION_FILE, e);

        }
    }

    public static Map<String, String> getConfiguration() {
        // ugly workaround to get String as generics
        Map temp = properties;
        Map<String, String> map = new HashMap<String, String>(temp);
        // prevent the returned configuration from being modified
        return Collections.unmodifiableMap(map);
    }


    public static String getConfig(String key) {
        return properties.getProperty(key);
    }

    public static String dailyAdjustedFolder() { return properties.getProperty("data-folder") + "daily-adjusted/"; }

    public static String testDataFolder() { return properties.getProperty("data-folder") + "test/"; }

    public static String trainDataFolder() { return properties.getProperty("data-folder") + "train/"; }

    // private constructor to prevent initialization
    private Configuration() {
    }
}