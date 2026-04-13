package pages;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // Locators for Admin Courses Page
    private final By searchFieldLocator = By.xpath("//input[@placeholder='Search']");

    // private final By courseLinksLocator = By.xpath(
    //     "//tr[.//span[@data-testid='tag-root' and contains(.,'Live')]]" +
    //     "//a[contains(@class,'cds-111') and (" +
    //         "contains(normalize-space(.), 'Version') or " +
    //         "contains(normalize-space(.), 'Session') or " +
    //         "contains(normalize-space(.), 'V3') or " +
    //         "contains(normalize-space(.), 'CMO Optimization') or " +
    //         "contains(normalize-space(.), 'Flutter: Developing Cross-Platform Mobile Apps') or " +
    //         "contains(normalize-space(.), 'Test')" +
    //         "contains(normalize-space(.), 'Operating Systems: Overview, Administration, and Security')" +
    //     ")]"
    // );
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


   // private final By courseLinksLocator = By.xpath("//div[contains(@class, 'cds-1')]//a[contains(., 'Version')]");
//    private final By courseLinksLocator = By.xpath(
//     "//tr[.//span[@data-testid='tag-root' and contains(.,'Live')]]" +
//     "//a[contains(@class,'cds-111') and contains(normalize-space(.), 'Version')]"
// );
//    private final By courseLinksLocator = By.xpath( "//div[contains(@class, 'cds-111')]//a[" +
//         "contains(., 'Version 1') or " +
//         "contains(., 'Version') or " +
//         "contains(., 'Version 2') or " +
//         "contains(., 'Version 3') or " +
//         "contains(., 'Version 4') or " +
//         "contains(., 'Version 5') or " +
//         "contains(., 'Original Version') or " +
//         "contains(., 'New Version') or " +
//         "contains(., 'Control') or " +
//         "contains(., 'Version 3 - Operating Systems: Overview, Administration, and Security') or " +
//         "contains(., 'V3 Cybersecurity Compliance Framework, Standards & Regulations') or " +
//         "contains(., 'v3 - October 2024') or " +
//         "contains(., 'V3 - Incident Response and Digital Forensics') or " +
//         "contains(., 'Introduction to Neural Networks with PyTorch') or " +
//         "contains(., 'Version 3 - Data Warehouse Fundamentals') or " +
//         "contains(., 'Version 3 - Timed Final Exam') or " +
//         "contains(., 'Version 2 - ACE') or " +
//         "contains(., 'Session 1') or " +
//         "contains(., 'Test') or " +
//         "contains(., 'Flutter: Developing Cross-Platform Mobile Apps')" +
//     "]"
// );

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

    public AdminCoursesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
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

            List<WebElement> courseLinks = driver.findElements(courseLinksLocator);
            logger.info("Number of course links found on page {}: {}", pageNumber, courseLinks.size());

            if (courseLinks.isEmpty()) {
                logger.warn("No course links found for '{}' on page {}.", courseName, pageNumber);
                takeScreenshot("no-course-links-page-" + pageNumber + "-screenshot.png");
            } else {
                int attempt = 0;
                while (attempt < 3 && versions.isEmpty()) { // Retry up to 3 times if stale elements are encountered
                    try {
                        for (WebElement courseLink : new ArrayList<>(courseLinks)) {
                            processCourseLink(courseLink, slug, courseName, versions);
                            if (!versions.isEmpty()) break; // Stop after first match
                        }
                        break; // Exit loop if no exceptions occur
                    } catch (StaleElementReferenceException e) {
                        logger.warn("Stale element reference encountered on page {}. Retrying... Attempt {}", pageNumber, attempt + 1);
                        attempt++;
                        // Re-fetch the course links
                        courseLinks = driver.findElements(courseLinksLocator);
                        if (courseLinks.isEmpty()) {
                            logger.warn("No course links found after retry for '{}' on page {}.", courseName, pageNumber);
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
        return versions;
    }

    private boolean navigateToNextPage() {
        try {
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

            // Scroll to the "Next" button and click it
            scrollToElement(nextButton);
            nextButton.click();
            logger.info("Clicked 'Next' button to navigate to the next page.");

            // Wait for the page to load and stabilize
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table, div.cds-10")));
            return true;
        } catch (Exception e) {
            logger.warn("Failed to navigate to the next page: {}", e.getMessage());
            takeScreenshot("pagination-error-screenshot.png");
            return false;
        }
    }

    private boolean waitForCourseListing() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table, div.cds-10")));
            return true;
        } catch (TimeoutException e) {
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

            // Log the simplified values for debugging
            logger.info("Simplified Course Name: '{}', Simplified Href: '{}'", simplifiedCourseName, simplifiedHref);

            // Check if the simplified href contains the simplified course name or the slug
            boolean isMatch = simplifiedHref.contains(simplifiedCourseName) || href.contains("/teach/" + slug + "/");

            // Additional lenient matching: check if most words in the course name appear in the href
            if (!isMatch) {
                String[] courseNameWords = courseName.toLowerCase().split("[^a-z0-9]+");
                int matchCount = 0;
                for (String word : courseNameWords) {
                    if (word.length() > 3 && simplifiedHref.contains(word)) { // Ignore short words
                        matchCount++;
                    }
                }
                // Consider it a match if at least 50% of the significant words match
                int significantWords = (int) java.util.Arrays.stream(courseNameWords).filter(w -> w.length() > 3).count();
                if (significantWords > 0 && matchCount >= significantWords / 2) {
                    isMatch = true;
                    logger.info("Matched course using lenient word matching: {} words matched out of {}", matchCount, significantWords);
                }
            }

            if (isMatch) {
                scrollToElement(courseLink);
                logger.info("Attempting to click course link using Actions...");
                new Actions(driver).moveToElement(courseLink).click().perform();

                try {
                    wait.until(ExpectedConditions.urlContains("/teach/"));
                    logger.info("Successfully navigated to teach page: {}", driver.getCurrentUrl());
                } catch (TimeoutException e) {
                    logger.warn("Click did not navigate. Directly navigating to: {}", href);
                    driver.get(href);
                    wait.until(ExpectedConditions.urlContains("/teach/"));
                    logger.info("Navigated to teach page via direct navigation: {}", driver.getCurrentUrl());
                }

                String[] ratingAndVersion = navigateToAnalyticsAndScrapeRating();
                String rating = ratingAndVersion[0];
                // Use the requested format: "Live " + version number, with fallback to "N/A" if unknown
                String version = ratingAndVersion[1].equals("Unknown") ? "N/A" : "Live " + ratingAndVersion[1];
                versions.add(new CourseVersion(courseName, version, driver.getCurrentUrl(), rating));
            }
        } catch (StaleElementReferenceException e) {
            logger.warn("Stale element reference for course link '{}'. Skipping...", courseLink, e.getMessage());
            throw e; // Re-throw to trigger retry in searchAndGetCourseVersions
        } catch (Exception e) {
            logger.warn("Error processing course link '{}': {}", courseLink, e.getMessage());
            takeScreenshot("course-processing-error-screenshot.png");
        }
    }

    private void searchForCourse(String courseName) {
        try {
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(searchFieldLocator));
            searchBox.clear();
            searchBox.sendKeys(courseName + Keys.ENTER);
            logger.info("Searched for course: {}", courseName);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table, div.cds-10")));
            // Add a small delay to ensure the page fully loads
            Thread.sleep(2000);
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
        logger.info("Navigating to Analytics page...");
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        WebElement analyticsLink = wait.until(ExpectedConditions.elementToBeClickable(analyticsLinkLocator));
        scrollToElement(analyticsLink);
        try {
            analyticsLink.click();
        } catch (ElementClickInterceptedException e) {
            logger.info("Standard click failed, attempting JavaScript click...");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", analyticsLink);
        }
        logger.info("Clicked Analytics tab");
        wait.until(ExpectedConditions.urlContains("analytics"));
    }

    private String[] scrapeRating() {
        logger.info("Navigating to Ratings section...");
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        WebElement ratingsLink = wait.until(ExpectedConditions.presenceOfElementLocated(ratingsLinkLocator));
        scrollToElement(ratingsLink);
        
        wait.until(ExpectedConditions.elementToBeClickable(ratingsLink));
        
        try {
            ratingsLink.click();
            logger.info("Clicked Ratings section using standard click");
        } catch (ElementClickInterceptedException e) {
            logger.info("Standard click failed, attempting JavaScript click...");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ratingsLink);
            logger.info("Clicked Ratings section using JavaScript click");
        }
        
        wait.until(ExpectedConditions.urlContains("ratings"));
        String versionNumber = setBranchMergerToLiveVersion();
        String rating = retrieveRating();
        return new String[]{rating, versionNumber};
    }

    private String setBranchMergerToLiveVersion() {
        logger.info("Changing branch merger to Live Version...");

        try {
            WebElement branchMergerDropdown = wait.until(ExpectedConditions.elementToBeClickable(branchMergerDropdownLocator));
            scrollToElement(branchMergerDropdown);
            branchMergerDropdown.click();
            logger.info("Clicked branch merger dropdown.");

            try {
                System.out.println("Branch merger dropdown HTML: " + branchMergerDropdown.getAttribute("outerHTML"));
            } catch (Exception e) {
                System.out.println("Failed to get branch merger dropdown HTML: " + e.getMessage());
            }

            By dropdownListLocator = By.xpath("//ul[contains(@class, 'cds-select-list') and @role='listbox']");
            wait.until(ExpectedConditions.visibilityOfElementLocated(dropdownListLocator));
            logger.info("Dropdown options are visible.");

            WebElement liveVersionOption = wait.until(ExpectedConditions.elementToBeClickable(rliveVersionOptionLocator));
            scrollToElement(liveVersionOption);

             // Extract the version number
             String versionNumber = "Unknown";
            try {
               WebElement versionElement = liveVersionOption.findElement(versionNumberLocator);
               versionNumber = versionElement.getText().trim(); // e.g., "Version 3"
             logger.info("Extracted version number: {}", versionNumber);
            }catch (Exception e) {
             logger.warn("Failed to extract version number: {}", e.getMessage());
             }

            try {
                System.out.println("Live version option HTML: " + liveVersionOption.getAttribute("outerHTML"));
            } catch (Exception e) {
                System.out.println("Failed to get Live version option HTML: " + e.getMessage());
            }

            try {
                liveVersionOption.click();
                logger.info("Clicked Live version option using standard click.");
            } catch (ElementClickInterceptedException e) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].click();", liveVersionOption);
                logger.info("Clicked Live version option using JavaScript click.");
            }

            wait.until(ExpectedConditions.invisibilityOfElementLocated(dropdownListLocator));
            logger.info("Successfully changed branch merger to Live Version.");
            return versionNumber;
        } catch (TimeoutException e) {
            logger.error("Failed to locate or interact with the branch merger dropdown or Live version option: {}", e.getMessage());
            takeScreenshot("branch-merger-failure-screenshot.png");
            throw new RuntimeException("Unable to set branch merger to Live Version", e);
        } catch (Exception e) {
            logger.error("Unexpected error while setting branch merger to Live Version: {}", e.getMessage());
            takeScreenshot("branch-merger-unexpected-error-screenshot.png");
            throw new RuntimeException("Unexpected error while setting branch merger to Live Version", e);
        }
    }

    private String retrieveRating() {
        try {
            WebElement ratingElement = wait.until(ExpectedConditions.visibilityOfElementLocated(ratingElementLocator));
            String rating = ratingElement.getText().trim();
            logger.info("Scraped rating: {}", rating);
            return rating;
        } catch (Exception e) {
            logger.error("Failed to scrape rating using primary locator: {}", e.getMessage());
            takeScreenshot("rating-scrape-failure-screenshot.png");
            return "N/A";
        }
    }

    private void scrollToElement(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element);
            Thread.sleep(500); // Just to ensure the element is in view
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

    public void writeToExcel(String filePath, List<CourseVersion> courseVersions) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Course Ratings");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Course Name");
            headerRow.createCell(1).setCellValue("Version");
            headerRow.createCell(2).setCellValue("Rating");

            int rowNum = 1;
            for (CourseVersion version : courseVersions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(version.getCourseName());
                row.createCell(1).setCellValue(version.getVersion());
                row.createCell(2).setCellValue(version.getRating());
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            logger.info("Excel file written successfully to: {}", filePath);
        } catch (Exception e) {
            logger.error("Failed to write to Excel: {}", e.getMessage());
        }
    }

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
            return "CourseVersion{courseName='" + courseName + "', version='" + version + "', link='" + link + "', rating='" + rating + "'}";
        }
    }
}