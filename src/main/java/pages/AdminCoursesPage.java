package pages;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ExcelExporter;
import utils.RetryPolicy;
import utils.SmartWaiter;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AdminCoursesPage {
    private static final Logger logger = LoggerFactory.getLogger(AdminCoursesPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // ✅ ADD THESE OPTIMIZATIONS
    private final RetryPolicy retryPolicy = new RetryPolicy();
    private final SmartWaiter smartWaiter;
    private final ExcelExporter excelExporter = new ExcelExporter();

    // Locators for Admin Courses Page
    private final By searchFieldLocator = By.xpath("//input[@placeholder='Search']");

    private final By courseLinksLocator = By.xpath(
        "//tr[.//span[@data-testid='tag-root' and contains(.,'Live')]]" +
        "//a[contains(@class,'cds-111') and (" +
            "contains(normalize-space(.), 'Version') or " +
            "contains(normalize-space(.), 'Session') or " +
            "contains(normalize-space(.), 'V3') or " +
            "contains(normalize-space(.), 'CMO Optimization') or " +
            "contains(normalize-space(.), 'CMO_M1 Optimization') or " +
            "contains(normalize-space(.), 'Flutter: Developing Cross-Platform Mobile Apps') or " +
            "contains(normalize-space(.), 'Test') or " +   
            "contains(normalize-space(.), 'Operating Systems: Overview, Administration, and Security')" +
        ")]"
    );

    // Pagination locators
    private final By nextButtonLocator = By.xpath("//button[contains(text(), 'Next') or contains(text(), '→')]");
    private final By nextButtonDisabledLocator = By.xpath("//button[contains(text(), 'Next') or contains(text(), '→') and (@disabled or contains(@class, 'disabled'))]");

    // Locators for Analytics and Ratings
    private final By analyticsLinkLocator = By.xpath("//button[@role='tab' and .//span[contains(text(), 'Analytics')]]");
    private final By ratingsLinkLocator = By.xpath("//a[contains(text(), 'Ratings')]");
    private final By branchMergerDropdownLocator = By.xpath("//div[contains(@class, 'rc-BranchSwitcher')]//div[@role='button']");
    private final By rliveVersionOptionLocator = By.xpath("//ul[contains(@class, 'cds-select-list')]//li[.//span[contains(@class, 'cds-tag-status') and contains(., 'Live')]]");
    private final By ratingElementLocator = By.xpath("//div[contains(@class, 'rc-AverageCourseRating')]//strong");
    private final By versionNumberLocator = By.xpath(".//span[@class='rc-BranchStatus']/strong");

    // ✅ SINGLE CONSTRUCTOR (REMOVED DUPLICATE)
    public AdminCoursesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.smartWaiter = new SmartWaiter(driver);  // ✅ Initialize SmartWaiter
        logger.debug("AdminCoursesPage initialized with driver and utilities");
    }

    public void navigateToAdminCourses(String adminUrl) {
        logger.info("Navigating to Admin Courses page: {}", adminUrl);
        driver.get(adminUrl);
        wait.until(ExpectedConditions.urlContains("/admin"));
        logger.info("Current URL after navigation: {}", driver.getCurrentUrl());
    }

    private String toSlug(String courseName) {
        return courseName.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    public List<CourseVersion> searchAndGetCourseVersions(String courseName) {
        List<CourseVersion> versions = new ArrayList<>();
        
        try {
            searchForCourse(courseName);

            if (!waitForCourseListing()) {
                takeScreenshot("no-course-listing-screenshot.png");
                return versions;
            }

            String slug = toSlug(courseName);
            int pageNumber = 1;
            boolean hasNextPage = true;

            while (hasNextPage && versions.isEmpty()) {
                logger.info("Checking page {} for course: {}", pageNumber, courseName);

                // Wait for the page to stabilize to avoid stale elements
                wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

                // ✅ USE RETRY POLICY FOR FETCHING COURSE LINKS
                List<WebElement> courseLinks = retryPolicy.executeWithRetry(
                    "fetch_course_links_page_" + pageNumber,
                    () -> driver.findElements(courseLinksLocator)
                );
                
                logger.info("Number of course links found on page {}: {}", pageNumber, courseLinks.size());

                if (courseLinks.isEmpty()) {
                    logger.warn("No course links found for '{}' on page {}.", courseName, pageNumber);
                    takeScreenshot("no-course-links-page-" + pageNumber + "-screenshot.png");
                } else {
                    int attempt = 0;
                    while (attempt < 3 && versions.isEmpty()) {
                        try {
                            for (WebElement courseLink : new ArrayList<>(courseLinks)) {
                                processCourseLink(courseLink, slug, courseName, versions);
                                if (!versions.isEmpty()) break;
                            }
                            break;
                        } catch (StaleElementReferenceException e) {
                            logger.warn("Stale element reference encountered on page {}. Retrying... Attempt {}", 
                                pageNumber, attempt + 1);
                            attempt++;
                            
                            // ✅ RE-FETCH WITH RETRY
                            courseLinks = retryPolicy.executeWithRetry(
                                "refetch_course_links_page_" + pageNumber,
                                () -> driver.findElements(courseLinksLocator)
                            );
                            
                            if (courseLinks.isEmpty()) {
                                logger.warn("No course links found after retry for '{}' on page {}.", 
                                    courseName, pageNumber);
                                break;
                            }
                        }
                    }
                }

                // Check if there's a next page
                hasNextPage = navigateToNextPage();
                pageNumber++;
            }

            if (versions.isEmpty()) {
                logger.warn("No versions matched for '{}' after checking all pages.", courseName);
                takeScreenshot("final-no-versions-screenshot.png");
            }
        } catch (Exception e) {
            logger.error("Error in searchAndGetCourseVersions for '{}': {}", courseName, e.getMessage());
            takeScreenshot("search-versions-error-screenshot.png");
        }
        
        return versions;
    }

    private boolean navigateToNextPage() {
        try {
            // ✅ USE RETRY POLICY FOR NEXT BUTTON OPERATIONS
            return retryPolicy.executeWithRetry(
                "navigate_to_next_page",
                this::performNextPageNavigation
            );
        } catch (Exception e) {
            logger.warn("Failed to navigate to the next page: {}", e.getMessage());
            takeScreenshot("pagination-error-screenshot.png");
            return false;
        }
    }

    private boolean performNextPageNavigation() {
        // Check if the "Next" button exists and is enabled
        List<WebElement> nextButtons = driver.findElements(nextButtonLocator);
        List<WebElement> disabledNextButtons = driver.findElements(nextButtonDisabledLocator);

        if (nextButtons.isEmpty()) {
            logger.info("No 'Next' button found. Assuming this is the last page.");
            return false;
        }

        WebElement nextButton = nextButtons.get(0);
        if (!disabledNextButtons.isEmpty() || !nextButton.isEnabled()) {
            logger.info("'Next' button is disabled. This is the last page.");
            return false;
        }

        // ✅ USE SMART WAITER FOR NEXT BUTTON
        scrollToElement(nextButton);
        nextButton.click();
        logger.info("Clicked 'Next' button to navigate to the next page.");

        // Wait for the page to load and stabilize
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        smartWaiter.waitForElementPresence(
            By.cssSelector("table, div.cds-10"),
            SmartWaiter.WaitType.MEDIUM
        );
        
        return true;
    }

    private boolean waitForCourseListing() {
        try {
            // ✅ USE SMART WAITER (MEDIUM TIMEOUT FOR PAGE LOAD)
            smartWaiter.waitForElementPresence(
                By.cssSelector("table, div.cds-10"),
                SmartWaiter.WaitType.MEDIUM
            );
            return true;
        } catch (Exception e) {
            logger.warn("Course listing not visible: {}", e.getMessage());
            return false;
        }
    }

    private void processCourseLink(WebElement courseLink, String slug, String courseName, List<CourseVersion> versions) {
        try {
            String courseTitle = courseLink.getText().trim();
            String href = courseLink.getAttribute("href");
            logger.info("Found course link: Title='{}', Href='{}'", courseTitle, href);

            // Simplify the course name and href for matching
            String simplifiedCourseName = courseName.toLowerCase().replaceAll("[^a-z0-9]", "");
            String simplifiedHref = href.toLowerCase().replaceAll("[^a-z0-9]", "");

            logger.debug("Simplified Course Name: '{}', Simplified Href: '{}'", 
                simplifiedCourseName, simplifiedHref);

            // Check if the simplified href contains the simplified course name or the slug
            boolean isMatch = simplifiedHref.contains(simplifiedCourseName) || 
                            href.contains("/teach/" + slug + "/");

            // Additional lenient matching: check if most words in the course name appear in the href
            if (!isMatch) {
                String[] courseNameWords = courseName.toLowerCase().split("[^a-z0-9]+");
                int matchCount = 0;
                for (String word : courseNameWords) {
                    if (word.length() > 3 && simplifiedHref.contains(word)) {
                        matchCount++;
                    }
                }
                // Consider it a match if at least 50% of the significant words match
                int significantWords = (int) java.util.Arrays.stream(courseNameWords)
                    .filter(w -> w.length() > 3)
                    .count();
                    
                if (significantWords > 0 && matchCount >= significantWords / 2) {
                    isMatch = true;
                    logger.info("Matched course using lenient word matching: {} words matched out of {}", 
                        matchCount, significantWords);
                }
            }

            if (isMatch) {
                scrollToElement(courseLink);
                logger.info("Attempting to click course link using Actions...");
                
                // ✅ USE RETRY POLICY FOR CLICKING COURSE LINK
                retryPolicy.executeWithRetryVoid(
                    "click_course_link_" + courseName,
                    () -> {
                        new Actions(driver).moveToElement(courseLink).click().perform();
                    }
                );

                try {
                    // ✅ USE SMART WAITER FOR NAVIGATION
                    smartWaiter.waitForUrlContains("/teach/", SmartWaiter.WaitType.LONG);
                    logger.info("Successfully navigated to teach page: {}", driver.getCurrentUrl());
                } catch (Exception e) {
                    logger.warn("Click did not navigate. Directly navigating to: {}", href);
                    driver.get(href);
                    smartWaiter.waitForUrlContains("/teach/", SmartWaiter.WaitType.LONG);
                    logger.info("Navigated to teach page via direct navigation: {}", driver.getCurrentUrl());
                }

                String[] ratingAndVersion = navigateToAnalyticsAndScrapeRating();
                String rating = ratingAndVersion[0];
                String version = ratingAndVersion[1].equals("Unknown") ? "N/A" : "Live " + ratingAndVersion[1];
                versions.add(new CourseVersion(courseName, version, driver.getCurrentUrl(), rating));
            }
        } catch (StaleElementReferenceException e) {
            logger.warn("Stale element reference for course link '{}'. Skipping...", courseLink);
            throw e;
        } catch (Exception e) {
            logger.warn("Error processing course link: {}", e.getMessage());
            takeScreenshot("course-processing-error-screenshot.png");
        }
    }

    private void searchForCourse(String courseName) {
        try {
            // ✅ USE SMART WAITER - INSTANT TIMEOUT (search box is always visible)
            WebElement searchBox = smartWaiter.waitForElementClickable(
                searchFieldLocator, 
                SmartWaiter.WaitType.INSTANT
            );
            searchBox.clear();
            searchBox.sendKeys(courseName + Keys.ENTER);
            logger.info("Searched for course: {}", courseName);

            // ✅ USE SMART WAITER - MEDIUM TIMEOUT (page load after search)
            smartWaiter.waitForElementPresence(
                By.cssSelector("table, div.cds-10"),
                SmartWaiter.WaitType.MEDIUM
            );
            
            Thread.sleep(2000); // Small delay to ensure page fully loads
        } catch (Exception e) {
            logger.error("Failed to search for course: {}", e.getMessage());
            throw new RuntimeException("Search failed", e);
        }
    }

    private String[] navigateToAnalyticsAndScrapeRating() {
        try {
            navigateToAnalytics();
            return scrapeRating();
        } catch (Exception e) {
            logger.error("Failed to navigate to Analytics and scrape rating: {}", e.getMessage());
            takeScreenshot("analytics-scrape-error-screenshot.png");
            return new String[]{"N/A", "Unknown"};
        }
    }

    private void navigateToAnalytics() {
        try {
            logger.info("Navigating to Analytics page...");
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState")
                .equals("complete"));
            
            // ✅ USE SMART WAITER - MEDIUM TIMEOUT FOR ANALYTICS LINK
            WebElement analyticsLink = smartWaiter.waitForElementClickable(
                analyticsLinkLocator,
                SmartWaiter.WaitType.MEDIUM
            );
            
            scrollToElement(analyticsLink);
            
            try {
                analyticsLink.click();
            } catch (ElementClickInterceptedException e) {
                logger.info("Standard click failed, attempting JavaScript click...");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", analyticsLink);
            }
            
            logger.info("Clicked Analytics tab");
            smartWaiter.waitForUrlContains("analytics", SmartWaiter.WaitType.LONG);
            
        } catch (Exception e) {
            logger.error("Failed to navigate to Analytics: {}", e.getMessage());
            throw new RuntimeException("Analytics navigation failed", e);
        }
    }

    private String[] scrapeRating() {
        try {
            logger.info("Navigating to Ratings section...");
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState")
                .equals("complete"));
            
            // ✅ USE SMART WAITER - MEDIUM TIMEOUT FOR RATINGS LINK
            WebElement ratingsLink = smartWaiter.waitForElementClickable(
                ratingsLinkLocator,
                SmartWaiter.WaitType.MEDIUM
            );
            
            scrollToElement(ratingsLink);
            
            try {
                ratingsLink.click();
                logger.info("Clicked Ratings section using standard click");
            } catch (ElementClickInterceptedException e) {
                logger.info("Standard click failed, attempting JavaScript click...");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ratingsLink);
                logger.info("Clicked Ratings section using JavaScript click");
            }
            
            smartWaiter.waitForUrlContains("ratings", SmartWaiter.WaitType.LONG);
            
            String versionNumber = setBranchMergerToLiveVersion();
            String rating = retrieveRating();
            return new String[]{rating, versionNumber};
            
        } catch (Exception e) {
            logger.error("Failed to scrape rating: {}", e.getMessage());
            throw new RuntimeException("Rating scraping failed", e);
        }
    }

    private String setBranchMergerToLiveVersion() {
        logger.info("Changing branch merger to Live Version...");

        try {
            // ✅ USE SMART WAITER - SHORT TIMEOUT FOR DROPDOWN
            WebElement branchMergerDropdown = smartWaiter.waitForElementClickable(
                branchMergerDropdownLocator,
                SmartWaiter.WaitType.SHORT
            );
            
            scrollToElement(branchMergerDropdown);
            branchMergerDropdown.click();
            logger.info("Clicked branch merger dropdown.");

            By dropdownListLocator = By.xpath("//ul[contains(@class, 'cds-select-list') and @role='listbox']");
            
            // ✅ USE SMART WAITER - MEDIUM TIMEOUT FOR DROPDOWN OPTIONS
            smartWaiter.waitForElementVisibility(
                dropdownListLocator,
                SmartWaiter.WaitType.MEDIUM
            );
            logger.info("Dropdown options are visible.");

            // ✅ USE RETRY POLICY FOR FINDING LIVE VERSION OPTION
            WebElement liveVersionOption = retryPolicy.executeWithRetry(
                "find_live_version_option",
                () -> smartWaiter.waitForElementClickable(
                    rliveVersionOptionLocator,
                    SmartWaiter.WaitType.SHORT
                )
            );
            
            scrollToElement(liveVersionOption);

            // Extract the version number
            String versionNumber = "Unknown";
            try {
                WebElement versionElement = liveVersionOption.findElement(versionNumberLocator);
                versionNumber = versionElement.getText().trim();
                logger.info("Extracted version number: {}", versionNumber);
            } catch (Exception e) {
                logger.warn("Failed to extract version number: {}", e.getMessage());
            }

            try {
                liveVersionOption.click();
                logger.info("Clicked Live version option using standard click.");
            } catch (ElementClickInterceptedException e) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].click();", liveVersionOption);
                logger.info("Clicked Live version option using JavaScript click.");
            }

            // ✅ USE SMART WAITER - WAIT FOR DROPDOWN TO CLOSE
            smartWaiter.waitForElementInvisibility(
                dropdownListLocator,
                SmartWaiter.WaitType.SHORT
            );
            
            logger.info("Successfully changed branch merger to Live Version.");
            return versionNumber;
            
        } catch (Exception e) {
            logger.error("Failed to set branch merger to Live Version: {}", e.getMessage());
            takeScreenshot("branch-merger-failure-screenshot.png");
            throw new RuntimeException("Unable to set branch merger to Live Version", e);
        }
    }

    private String retrieveRating() {
        try {
            // ✅ USE SMART WAITER - INSTANT TIMEOUT (rating element should be visible)
            WebElement ratingElement = smartWaiter.waitForElementVisibility(
                ratingElementLocator,
                SmartWaiter.WaitType.INSTANT
            );
            
            String rating = ratingElement.getText().trim();
            logger.info("Scraped rating: {}", rating);
            return rating;
            
        } catch (Exception e) {
            logger.error("Failed to scrape rating: {}", e.getMessage());
            takeScreenshot("rating-scrape-failure-screenshot.png");
            return "N/A";
        }
    }

    private void scrollToElement(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element);
            Thread.sleep(500);
        } catch (Exception e) {
            logger.error("Failed to scroll to element: {}", e.getMessage());
        }
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

    // ✅ REPLACE OLD writeToExcel WITH NEW BATCH EXPORTER
    public void writeToExcel(String filePath, List<CourseVersion> courseVersions) {
        logger.info("Exporting {} courses to Excel: {}", courseVersions.size(), filePath);
        excelExporter.writeToBatchExcel(courseVersions, filePath);
    }

    // ========== COURSE VERSION MODEL CLASS ==========
    public static class CourseVersion {
        private final String courseName;
        private final String version;
        private final String link;
        private final String rating;

        public CourseVersion(String courseName, String version, String link, String rating) {
            this.courseName = courseName;
            this.version = version;
            this.link = link;
            this.rating = rating;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getVersion() {
            return version;
        }

        public String getLink() {
            return link;
        }

        public String getRating() {
            return rating;
        }

        @Override
        public String toString() {
            return "CourseVersion{" +
                    "courseName='" + courseName + '\'' +
                    ", version='" + version + '\'' +
                    ", link='" + link + '\'' +
                    ", rating='" + rating + '\'' +
                    '}';
        }
    }
}