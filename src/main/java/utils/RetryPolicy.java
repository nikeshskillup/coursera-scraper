package utils;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class RetryPolicy {
    private static final Logger logger = LoggerFactory.getLogger(RetryPolicy.class);
    
    private static final int MAX_RETRIES = 3;
    private static final int BASE_DELAY_MS = 100;
    
    public <T> T executeWithRetry(String operation, Callable<T> task) throws Exception {
        int attempt = 0;
        
        while (attempt < MAX_RETRIES) {
            try {
                logger.debug("Attempt {}/{} for operation: {}", 
                    attempt + 1, MAX_RETRIES, operation);
                return task.call();
                
            } catch (StaleElementReferenceException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    logger.error("Stale element in {} after {} retries", operation, MAX_RETRIES);
                    throw e;
                }
                
                long delayMs = BASE_DELAY_MS * (long) Math.pow(2, attempt - 1);
                logger.warn("Stale element in {}, retrying after {}ms (attempt {}/{})", 
                    operation, delayMs, attempt, MAX_RETRIES);
                Thread.sleep(delayMs);
                
            } catch (TimeoutException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    logger.error("Timeout in {} after {} retries", operation, MAX_RETRIES);
                    throw e;
                }
                
                long delayMs = BASE_DELAY_MS * (long) Math.pow(2, attempt - 1);
                logger.warn("Timeout in {}, retrying after {}ms (attempt {}/{})", 
                    operation, delayMs, attempt, MAX_RETRIES);
                Thread.sleep(delayMs);
            }
        }
        
        throw new RuntimeException("Operation failed after " + MAX_RETRIES + " retries");
    }
    
    public void executeWithRetryVoid(String operation, VoidCallable task) throws Exception {
        executeWithRetry(operation, () -> {
            task.call();
            return null;
        });
    }
    
    @FunctionalInterface
    public interface VoidCallable {
        void call() throws Exception;
    }
}