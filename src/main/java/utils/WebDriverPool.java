package utils;



import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import org.openqa.selenium.Cookie;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.*;

public class WebDriverPool {
    private static final Logger logger = LoggerFactory.getLogger(WebDriverPool.class);
    
    private final BlockingQueue<WebDriver> pool;
    private final int poolSize;
    private final boolean headlessMode;
    
    public WebDriverPool(int poolSize, boolean headlessMode) {
        this.poolSize = poolSize;
        this.headlessMode = headlessMode;
        this.pool = new LinkedBlockingQueue<>(poolSize);
        
        logger.info("Initializing WebDriver pool with {} drivers", poolSize);
        for (int i = 0; i < poolSize; i++) {
            try {
                pool.put(createDriver());
            } catch (InterruptedException e) {
                logger.error("Interrupted while initializing pool", e);
                Thread.currentThread().interrupt();
            }
        }
        logger.info("WebDriver pool initialized successfully");
    }
    
    public WebDriver borrowDriver() throws InterruptedException {
        WebDriver driver = pool.take();
        logger.debug("Driver borrowed. Available drivers: {}", pool.size());
        return driver;
    }
    
    public void returnDriver(WebDriver driver) {
        if (driver != null) {
            try {
                // Clear cookies before returning to pool
                driver.manage().deleteAllCookies();
                pool.put(driver);
                logger.debug("Driver returned to pool. Available drivers: {}", pool.size());
            } catch (InterruptedException e) {
                logger.error("Failed to return driver to pool", e);
                driver.quit();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public void addCookiesToDriver(WebDriver driver, Set<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            try {
                driver.manage().addCookie(cookie);
            } catch (Exception e) {
                logger.warn("Failed to add cookie: {}", cookie.getName());
            }
        }
    }

    private WebDriver createDriver() {
        logger.debug("Creating new WebDriver instance");
        
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        
        // Anti-detection settings
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", 
            new String[]{"enable-automation"});
        options.addArguments("--disable-blink-features=AutomationControlled");
        
        // Resource optimization
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-sync");
        
        // Performance settings
        options.addArguments("--disable-background-networking");
        options.addArguments("--disable-client-side-phishing-detection");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-component-extensions-with-background-pages");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        
        // Window settings
        options.addArguments("--start-maximized");
        options.addArguments("--window-size=1920,1080");
        
        if (headlessMode) {
            options.addArguments("--headless=new");
        }
        
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(90));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(60));
        
        logger.debug("New WebDriver created successfully");
        return driver;
    }
    
    public void shutdown() {
        logger.info("Shutting down WebDriver pool");
        WebDriver driver;
        int closedCount = 0;
        
        while ((driver = pool.poll()) != null) {
            try {
                driver.quit();
                closedCount++;
            } catch (Exception e) {
                logger.warn("Error closing driver: {}", e.getMessage());
            }
        }
        
        logger.info("WebDriver pool shutdown complete. Closed {} drivers", closedCount);
    }
    
    public int getAvailableDrivers() {
        return pool.size();
    }
    
    public int getPoolSize() {
        return poolSize;
    }
}