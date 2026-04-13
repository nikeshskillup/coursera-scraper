package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

public class AnalyticsPage {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//button[@role='tab' and .//span[text()='Analytics']]")
    private WebElement analyticsLink;

    @FindBy(xpath = "//ul[contains(@class, 'rc-SideMenu')]//a[text()='Ratings']")
    private WebElement ratingsLink;

    @FindBy(xpath = "//div[contains(@class, 'rc-AverageCourseRating')]//strong")
    private WebElement ratingElement;

    @FindBy(xpath = "//div[contains(@class, 'c-average-course-rating-stats')]")
    private WebElement ratingStatsElement;

    @FindBy(xpath = "//div[contains(@class, 'cds-selectOption-container')]//select[contains(@class, 'cds-635')]")
    private WebElement branchMergerDropdown;

    public AnalyticsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }

    public void goToAnalytics() {
        try {
            logger.info("Navigating to Analytics page...");
            new WebDriverWait(driver, Duration.ofSeconds(60))
                    .until(d -> ((JavascriptExecutor) d)
                            .executeScript("return document.readyState").equals("complete"));

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[contains(@class, 'rc-MainMenuTabs')]")
                ));
                logger.info("Navigation menu found on the page.");
            } catch (Exception e) {
                logger.warn("Navigation menu not found. The page structure might have changed: {}", e.getMessage());
            }

            logger.info("Page source before looking for Analytics link: {}", driver.getPageSource());

            try {
                List<WebElement> potentialLinks = driver.findElements(By.xpath("//button[@role='tab' and .//span[text()='Analytics']]"));
                if (potentialLinks.isEmpty()) {
                    logger.warn("No elements found matching the Analytics link XPath.");
                } else {
                    for (WebElement link : potentialLinks) {
                        logger.info("Potential Analytics link found - Text: {}, Tag: {}, Class: {}", 
                            link.getText(), link.getTagName(), link.getAttribute("class"));
                    }
                }
            } catch (Exception e) {
                logger.warn("Error while searching for potential Analytics links: {}", e.getMessage());
            }

            wait.until(ExpectedConditions.visibilityOf(analyticsLink));
            wait.until(ExpectedConditions.elementToBeClickable(analyticsLink));
            logger.info("Analytics link found with text: {}", analyticsLink.getText());

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", analyticsLink);
            Thread.sleep(500);

            analyticsLink.click();
            logger.info("Successfully navigated to Analytics page.");
        } catch (Exception e) {
            logger.error("Failed to navigate to Analytics page: {}", e.getMessage());
            logger.error("Page source on failure: {}", driver.getPageSource());
            try {
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Files.copy(screenshot.toPath(), new File("analytics-link-failure-screenshot.png").toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Screenshot saved as analytics-link-failure-screenshot.png");

                Files.writeString(new File("analytics-link-failure-pagesource.html").toPath(), driver.getPageSource());
                logger.info("Page source   source saved as analytics-link-failure-pagesource.html");
            } catch (Exception ex) {
                logger.error("Failed to save screenshot or page source: {}", ex.getMessage());
            }
            throw new RuntimeException("Failed to navigate to Analytics page", e);
        }
    }

    public void goToRatingSection() {
        try {
            logger.info("Navigating to Ratings section...");

            try {
                List<WebElement> potentialRatingsLinks = driver.findElements(By.xpath("//ul[contains(@class, 'rc-SideMenu')]//a[text()='Ratings']"));
                if (potentialRatingsLinks.isEmpty()) {
                    logger.warn("No elements found matching the Ratings link XPath.");
                    List<WebElement> allLinks = driver.findElements(By.xpath("//*[contains(text(), 'Ratings')]"));
                    if (allLinks.isEmpty()) {
                        logger.warn("No elements found containing the text 'Ratings'.");
                    } else {
                        for (WebElement link : allLinks) {
                            logger.info("Potential Ratings element found - Text: {}, Tag: {}, Class: {}", 
                                link.getText(), link.getTagName(), link.getAttribute("class"));
                        }
                    }
                } else {
                    for (WebElement link : potentialRatingsLinks) {
                        logger.info("Potential Ratings link found - Text: {}, Tag: {}, Class: {}", 
                            link.getText(), link.getTagName(), link.getAttribute("class"));
                    }
                }
            } catch (Exception e) {
                logger.warn("Error while searching for potential Ratings links: {}", e.getMessage());
            }

            wait.until(ExpectedConditions.visibilityOf(ratingsLink));
            wait.until(ExpectedConditions.elementToBeClickable(ratingsLink));
            logger.info("Ratings link found with text: {}", ratingsLink.getText());

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", ratingsLink);
            Thread.sleep(500);

            ratingsLink.click();
            logger.info("Successfully navigated to Ratings section.");

            // Change branch merger to live version
            changeBranchMergerToLiveVersion();
        } catch (Exception e) {
            logger.error("Failed to navigate to Ratings section: {}", e.getMessage());
            logger.error("Page source on failure: {}", driver.getPageSource());
            try {
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Files.copy(screenshot.toPath(), new File("ratings-link-failure-screenshot.png").toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Screenshot saved as ratings-link-failure-screenshot.png");

                Files.writeString(new File("ratings-link-failure-pagesource.html").toPath(), driver.getPageSource());
                logger.info("Page source saved as ratings-link-failure-pagesource.html");
            } catch (Exception ex) {
                logger.error("Failed to save screenshot or page source: {}", ex.getMessage());
            }
            throw new RuntimeException("Failed to navigate to Ratings section", e);
        }
    }

    private void changeBranchMergerToLiveVersion() {
        try {
            logger.info("Changing branch merger to Live Version...");
            wait.until(ExpectedConditions.elementToBeClickable(branchMergerDropdown));
            branchMergerDropdown.click();

            WebElement liveVersionOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//option[contains(text(), 'LIVE')]")
            ));
            liveVersionOption.click();
            logger.info("Successfully changed branch merger to Live Version.");
        } catch (Exception e) {
            logger.error("Failed to change branch merger to Live Version: {}", e.getMessage());
            throw new RuntimeException("Failed to change branch merger to Live Version", e);
        }
    }

    public String getRating() {
        try {
            try {
                List<WebElement> potentialRatings = driver.findElements(By.xpath("//div[contains(@class, 'rc-AverageCourseRating')]//strong"));
                if (potentialRatings.isEmpty()) {
                    logger.warn("No elements found matching the rating number XPath.");
                    List<WebElement> allStrongTags = driver.findElements(By.xpath("//strong[contains(text(), '.')]"));
                    if (allStrongTags.isEmpty()) {
                        logger.warn("No <strong> elements found containing a decimal number.");
                    } else {
                        for (WebElement rating : allStrongTags) {
                            logger.info("Potential rating number found - Text: {}, Tag: {}, Class: {}", 
                                rating.getText(), rating.getTagName(), rating.getAttribute("class"));
                        }
                    }
                } else {
                    for (WebElement rating : potentialRatings) {
                        logger.info("Potential rating number found - Text: {}, Tag: {}, Class: {}", 
                            rating.getText(), rating.getTagName(), rating.getAttribute("class"));
                    }
                }
            } catch (Exception e) {
                logger.warn("Error while searching for potential rating numbers: {}", e.getMessage());
            }

            wait.until(ExpectedConditions.visibilityOf(ratingElement));
            String rating = ratingElement.getText().trim();
            logger.info("Rating found: {}", rating);
            return rating;
        } catch (Exception e) {
            logger.error("Failed to get rating: {}", e.getMessage());
            return "Not found";
        }
    }

    public String getRatingStats() {
        try {
            wait.until(ExpectedConditions.visibilityOf(ratingStatsElement));
            String ratingStats = ratingStatsElement.getText().trim();
            logger.info("Rating stats found: {}", ratingStats);
            return ratingStats;
        } catch (Exception e) {
            logger.error("Failed to get rating stats: {}", e.getMessage());
            return "Not found";
        }
    }
}