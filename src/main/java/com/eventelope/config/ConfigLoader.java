package com.eventelope.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads configuration files including user authentication configs.
 */
public class ConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String DEFAULT_USER_CONFIG_PATH = "src/test/resources/configs/users.yaml";
    private final Yaml yaml = new Yaml();

    /**
     * Load user configurations from the users.yaml file.
     *
     * @return Map of user IDs to UserConfig objects
     */
    public Map<String, UserConfig> loadUserConfigs() {
        return loadUserConfigs(DEFAULT_USER_CONFIG_PATH);
    }

    /**
     * Load user configurations from a specified path.
     *
     * @param configPath Path to the user config file
     * @return Map of user IDs to UserConfig objects
     */
    public Map<String, UserConfig> loadUserConfigs(String configPath) {
        Map<String, UserConfig> userConfigs = new HashMap<>();
        
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            LOGGER.warn("User config file not found: {}", configPath);
            return userConfigs;
        }
        
        try (InputStream input = new FileInputStream(configFile)) {
            Map<String, Map<String, Object>> usersData = yaml.load(input);
            
            if (usersData != null) {
                for (Map.Entry<String, Map<String, Object>> entry : usersData.entrySet()) {
                    String userId = entry.getKey();
                    Map<String, Object> userData = entry.getValue();
                    
                    UserConfig userConfig = new UserConfig();
                    userConfig.setId(userId);
                    
                    if (userData.containsKey("authType")) {
                        userConfig.setAuthType((String) userData.get("authType"));
                    }
                    
                    if (userData.containsKey("username")) {
                        userConfig.setUsername((String) userData.get("username"));
                    }
                    
                    if (userData.containsKey("password")) {
                        userConfig.setPassword((String) userData.get("password"));
                    }
                    
                    if (userData.containsKey("token")) {
                        userConfig.setToken((String) userData.get("token"));
                    }
                    
                    if (userData.containsKey("apiKey")) {
                        userConfig.setApiKey((String) userData.get("apiKey"));
                    }
                    
                    if (userData.containsKey("apiKeyHeader")) {
                        userConfig.setApiKeyHeader((String) userData.get("apiKeyHeader"));
                    }
                    
                    userConfigs.put(userId, userConfig);
                }
            }
            
            LOGGER.info("Loaded {} user configurations from {}", userConfigs.size(), configPath);
            
        } catch (IOException e) {
            LOGGER.error("Error loading user configs from {}", configPath, e);
        }
        
        return userConfigs;
    }
}
