package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class SmartWaiter {
    private static final Logger logger = LoggerFactory.getLogger(SmartWaiter.class);
    private final WebDriver driver;
    
    // Different timeout durations
    private static final long INSTANT_TIMEOUT = 2;      // For visible elements
    private static final long SHORT_TIMEOUT = 5;        // For clickable elements
    private static final long MEDIUM_TIMEOUT = 15;      // For navigation
    private static final long LONG_TIMEOUT = 30;        // For heavy operations
    
    public SmartWaiter(WebDriver driver) {
        this.driver = driver;
    }
    
    public enum WaitType {
        INSTANT(INSTANT_TIMEOUT),      // 2 seconds
        SHORT(SHORT_TIMEOUT),          // 5 seconds
        MEDIUM(MEDIUM_TIMEOUT),        // 15 seconds
        LONG(LONG_TIMEOUT);            // 30 seconds
        
        private final long seconds;
        
        WaitType(long seconds) {
            this.seconds = seconds;
        }
        
        public long getSeconds() {
            return seconds;
        }
    }
    
    public WebElement waitForElementPresence(By locator, WaitType type) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(type.getSeconds()));
        logger.debug("Waiting for presence of element: {} with timeout: {}s", 
            locator, type.getSeconds());
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
    
    public WebElement waitForElementVisibility(By locator, WaitType type) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(type.getSeconds()));
        logger.debug("Waiting for visibility of element: {} with timeout: {}s", 
            locator, type.getSeconds());
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    public WebElement waitForElementClickable(By locator, WaitType type) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(type.getSeconds()));
        logger.debug("Waiting for clickability of element: {} with timeout: {}s", 
            locator, type.getSeconds());
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    public boolean waitForUrlContains(String urlPortion, WaitType type) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(type.getSeconds()));
        logger.debug("Waiting for URL to contain: {} with timeout: {}s", 
            urlPortion, type.getSeconds());
        return wait.until(ExpectedConditions.urlContains(urlPortion));
    }
    
    public boolean waitForElementInvisibility(By locator, WaitType type) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(type.getSeconds()));
        logger.debug("Waiting for invisibility of element: {} with timeout: {}s", 
            locator, type.getSeconds());
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
}