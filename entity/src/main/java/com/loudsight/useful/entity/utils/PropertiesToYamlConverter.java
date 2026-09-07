package com.loudsight.useful.entity.utils;

import com.loudsight.useful.helper.logging.LoggingHelper;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class PropertiesToYamlConverter {
    private static final LoggingHelper LOGGER = LoggingHelper.wrap(PropertiesToYamlConverter.class);

    private PropertiesToYamlConverter() {
    }

    public static void main(String[] args) {
        // Specify the input and output file paths
        String propertiesFilePath = "c:/dev/code/victory2025/webapps/backendserver/src/main/config/loudsight.net.properties";
        String yamlFilePath = "c:/dev/code/victory2025/webapps/backendserver/src/main/config/loudsight.net.yaml";

        try {
            // Convert properties file to Map
            Map<String, Object> propertiesMap = propertiesToMap(propertiesFilePath);

            Map<String, Object> propertiesMapOfMap = new HashMap<>();
            propertiesToMapOfMaps(propertiesMap, propertiesMapOfMap);

            // Save Map to YAML file
            saveYaml(propertiesMapOfMap, yamlFilePath);

            LOGGER.logInfo("Conversion complete! YAML file saved as: {}", yamlFilePath);
        } catch (IOException e) {
            LOGGER.logError("Error during conversion", e);
        }
    }

    private static void propertiesToMapOfMaps(Map<String, Object> from, Map<String, Object> to) {
        from.forEach((k, v) -> {
            propertiesToMapOfMaps(k, v, to);
        });
    }

    private static void propertiesToMapOfMaps(String k, Object v, Map<String, Object> to) {
            var x = k.split("\\.", 2);
            if (x.length > 1) {
                var nested = stringObjectMap(to.get(x[0]), x[0]);
                propertiesToMapOfMaps(x[1], v, nested);
                to.put(x[0], nested);
            } else {
                to.put(k, v.toString());
            }
    }

    private static Map<String, Object> stringObjectMap(Object value, String key) {
        var result = new HashMap<String, Object>();
        if (value == null) {
            return result;
        }
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Property key is both a value and a group: " + key);
        }
        map.forEach((nestedKey, nestedValue) -> {
            if (!(nestedKey instanceof String stringKey)) {
                throw new IllegalArgumentException("Non-string property key under: " + key);
            }
            result.put(stringKey, nestedValue);
        });
        return result;
    }

    // Convert .properties file to a Map
    private static Map<String, Object> propertiesToMap(String propertiesFilePath) throws IOException {
        Properties properties = new Properties();
        
        // Load properties file
        try (InputStream inputStream = Files.newInputStream(Path.of(propertiesFilePath))) {
            properties.load(inputStream);
        }
        
        // Convert Properties to a Map
        var result = new HashMap<String, Object>();
        properties.stringPropertyNames().forEach(name -> result.put(name, properties.getProperty(name)));
        return result;
    }

    // Save the Map to a YAML file
    private static void saveYaml(Map<String, Object> propertiesMap, String yamlFilePath) throws IOException {
        // Setup YAML options for pretty printing
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Block style (indented)
        Yaml yaml = new Yaml(options);

        // Write the Map as a YAML file
        try (Writer writer = Files.newBufferedWriter(Path.of(yamlFilePath))) {
            yaml.dump(propertiesMap, writer);
        }
    }
}
