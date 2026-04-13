package pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HomePage {
    private static final Logger logger = LogManager.getLogger(HomePage.class);
    private WebDriver driver;

    @FindBy(xpath = "//a[@data-e2e='header-login-button']")
    private WebElement signInButton;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        driver.get("https://www.coursera.org/");
        // Wait for the page to fully load, using a different parameter name in the lambda
        new WebDriverWait(driver, Duration.ofSeconds(20))
            .until(d -> ((org.openqa.selenium.JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
    }

    public LoginPage clickOnSignIn() {
        int retries = 3;
        while (retries > 0) {
            try {
                // Handle cookie consent popup if present
                try {
                    WebElement acceptCookies = driver.findElement(By.xpath("//button[contains(text(), 'Accept')]"));
                    if (acceptCookies.isDisplayed()) {
                        acceptCookies.click();
                        logger.info("Accepted cookie consent popup.");
                        Thread.sleep(1000); // Wait for popup to close
                    }
                } catch (Exception e) {
                    logger.debug("No cookie consent popup found.");
                }

                new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.elementToBeClickable(signInButton));
                signInButton.click();
                logger.info("Clicked on Sign In button successfully.");
                return new LoginPage(driver);
            } catch (TimeoutException | NoSuchElementException e) {
                retries--;
                logger.warn("Retry {}/3 failed to find Sign In button. Current URL: {}", 3 - retries, driver.getCurrentUrl());
                if (retries == 0) {
                    logger.error("Failed to click Sign In button: {}. Page source: {}", e.getMessage(), driver.getPageSource());
                    throw new RuntimeException("Sign In button not found: " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null;
    }
}