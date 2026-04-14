package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final Properties properties = new Properties();
    
    static {
        try {
            String configPath = System.getProperty("config.path", "config.properties");
            try (FileInputStream input = new FileInputStream(configPath)) {
                properties.load(input);
                logger.info("Configuration loaded from file: {}", configPath);
            }
        } catch (IOException fileException) {
            try (var input = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) {
                    throw new RuntimeException("Cannot find config.properties on classpath", fileException);
                }
                properties.load(input);
                logger.info("Configuration loaded from classpath: config.properties");
            } catch (IOException resourceException) {
                logger.error("Failed to load configuration", resourceException);
                throw new RuntimeException("Cannot load configuration", resourceException);
            }
        }
    }
    
    // Coursera settings
    public static String getBaseUrl() {
        return properties.getProperty("coursera.base_url", "https://www.coursera.org");
    }
    
    public static String getAdminUrl(String partner) {
        return properties.getProperty("coursera.admin." + partner, 
            "/admin/ibm-skills-network/home/courses");
    }
    
    // Scraper settings
    public static int getThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("scraper.thread_pool_size", "4"));
    }
    
    public static int getBatchSize() {
        return Integer.parseInt(properties.getProperty("scraper.batch_size", "5"));
    }
    
    public static int getRetryAttempts() {
        return Integer.parseInt(properties.getProperty("scraper.retry_attempts", "3"));
    }
    
    // Browser settings
    public static boolean isHeadless() {
        return Boolean.parseBoolean(properties.getProperty("browser.headless", "false"));
    }
    
    public static String getBrowser() {
        return properties.getProperty("browser", "chrome");
    }
    
    // Cache settings
    public static boolean isCacheEnabled() {
        return Boolean.parseBoolean(properties.getProperty("cache.enabled", "true"));
    }
    
    public static String getCacheFilePath() {
        return properties.getProperty("cache.file_path", "course_cache.json");
    }
    
    // Checkpoint settings
    public static boolean isCheckpointEnabled() {
        return Boolean.parseBoolean(properties.getProperty("checkpoint.enabled", "true"));
    }
    
    public static String getCheckpointFilePath() {
        return properties.getProperty("checkpoint.file_path", "scraper_checkpoint.json");
    }
    
    // Export settings
    public static String getExportDirectory() {
        return properties.getProperty("export.output_directory", "./reports");
    }
}