package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TakesScreenshot; // Added import
import org.openqa.selenium.OutputType; // Added import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseRatingsScraper {
    private static final Logger logger = LoggerFactory.getLogger(CourseRatingsScraper.class);
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final List<Map<String, String>> courseData;

    public CourseRatingsScraper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.courseData = new ArrayList<>();
    }

    public void processCourse(String courseLink, String courseVersionName) {
        // Navigate to the course link (already on the live version page)
        driver.get(courseLink);
        logger.info("Navigated to course link: {}", courseLink);

        // Navigate to the "Ratings" tab to scrape ratings
        AnalyticsPage analyticsPage = new AnalyticsPage(driver);
        analyticsPage.goToAnalytics();
        analyticsPage.goToRatingSection();

        // Scrape ratings data
        Map<String, String> ratingsData = new HashMap<>();
        ratingsData.put("Course Version", courseVersionName);
        ratingsData.put("Average Rating", analyticsPage.getRating());
        ratingsData.put("Rating Stats", analyticsPage.getRatingStats());
        courseData.add(ratingsData);

        logger.info("Successfully scraped ratings for version: {}", courseVersionName);
    }

    public List<Map<String, String>> getCourseData() {
        return courseData;
    }

    public void writeToExcel(String filePath) {
        // Implement Excel writing logic (e.g., using Apache POI)
        logger.info("Writing data to Excel file: {}", filePath);
    }

    private void takeScreenshot(String fileName) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot.toPath(), new File(fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Screenshot saved as {}", fileName);
        } catch (Exception ex) {
            logger.error("Failed to save screenshot: {}", ex.getMessage());
        }
    }
}