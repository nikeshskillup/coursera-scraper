package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CoursePage {
    private static final Logger logger = LoggerFactory.getLogger(CoursePage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//div[contains(@class, 'rc-CourseContextCard')]")
    private List<WebElement> versionLinks;

    public CoursePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }

    public List<String> getVersions() {
        List<String> versions = new ArrayList<>();
        try {
            logger.info("Fetching available versions on the course page...");
            wait.until(ExpectedConditions.visibilityOfAllElements(versionLinks));
            for (WebElement versionLink : versionLinks) {
                String versionText = versionLink.findElement(By.xpath(".//h3[contains(@class, 'css-6ecy9b')]")).getText().trim();
                if (!versionText.isEmpty()) {
                    versions.add(versionText);
                    logger.info("Found version: {}", versionText);
                }
            }
            if (versions.isEmpty()) {
                logger.warn("No versions found on the course page.");
            }
        } catch (Exception e) {
            logger.error("Error fetching versions: {}", e.getMessage());
        }
        return versions;
    }

    public void selectVersion(String version) {
        try {
            logger.info("Selecting version: {}", version);
            for (WebElement versionLink : versionLinks) {
                String versionText = versionLink.findElement(By.xpath(".//h3[contains(@class, 'css-6ecy9b')]")).getText().trim();
                if (versionText.equalsIgnoreCase(version)) {
                    wait.until(ExpectedConditions.elementToBeClickable(versionLink));
                    versionLink.click();
                    logger.info("Successfully selected version: {}", version);
                    return;
                }
            }
            logger.error("Version '{}' not found on the course page.", version);
            throw new RuntimeException("Version '" + version + "' not found on the course page.");
        } catch (Exception e) {
            logger.error("Error selecting version '{}': {}", version, e.getMessage());
            throw new RuntimeException("Failed to select version: " + version, e);
        }
    }
}