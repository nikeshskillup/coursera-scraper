package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.*;

import java.io.*;
import java.time.Duration;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

public class BasePage {

    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);

    protected WebDriver driver;
    protected Properties prop;

    protected LoginPage loginPage;
    protected HomePage homePage;
    protected RegistrationPage registrationPage;
    protected DashboardPage dashboardPage;
    protected AdminCoursesPage adminCoursesPage;
    protected CourseRatingsScraper courseRatingsScraper;

    // =========================
    // LOAD CONFIG (FIXED)
    // =========================
    public BasePage() {
        try {
            prop = new Properties();

            InputStream input = getClass()
                    .getClassLoader()
                    .getResourceAsStream("config.properties");

            if (input == null) {
                throw new RuntimeException("config.properties NOT found in resources folder");
            }

            prop.load(input);
            logger.info("Config loaded successfully");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    // =========================
    // SETUP
    // =========================
    public void setUp() {
        initializeDriver();

        loadCookies();

        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("login") || currentUrl.contains("signin")) {
            logger.warn("Cookies failed. Doing manual login...");
            performManualLogin();
            saveCookies();
        }

        // Initialize pages
        loginPage = new LoginPage(driver);
        homePage = new HomePage(driver);
        registrationPage = new RegistrationPage(driver);
        dashboardPage = new DashboardPage(driver);
        adminCoursesPage = new AdminCoursesPage(driver);
        courseRatingsScraper = new CourseRatingsScraper(driver);

        logger.info("All pages initialized successfully");
    }

    // =========================
    // DRIVER INIT (FIXED)
    // =========================
    public void initializeDriver() {

        String browserName = prop.getProperty("browser", "chrome");
        boolean headlessMode = Boolean.parseBoolean(prop.getProperty("headless", "true"));

        logger.info("Initializing browser: {}, headless: {}", browserName, headlessMode);

        if (browserName.equalsIgnoreCase("chrome")) {

            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();

            // Anti-detection
            options.setExperimentalOption("useAutomationExtension", false);
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.addArguments("--disable-blink-features=AutomationControlled");

            // Performance
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");

            if (headlessMode) {
                options.addArguments("--headless=new");
            }

            driver = new ChromeDriver(options);
        }

        if (driver == null) {
            throw new RuntimeException("WebDriver initialization failed");
        }

        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // ✅ CRITICAL FIX
        String url = prop.getProperty("coursera.base_url");

        if (url == null || url.isEmpty()) {
            throw new RuntimeException("❌ URL missing in config.properties");
        }

        logger.info("Navigating to URL: {}", url);
        driver.get(url);
    }

    // =========================
    // CLEANUP
    // =========================
    public void closeDriver() {
        try {
            if (driver != null) {
                driver.quit();
                logger.info("Driver closed");
            }
        } catch (Exception e) {
            logger.error("Error closing driver", e);
        } finally {
            driver = null;
        }
    }

    // =========================
    // COOKIES
    // =========================
    private void loadCookies() {
        try {
            File file = new File("cookies.txt");

            if (!file.exists()) {
                logger.warn("No cookies file found");
                return;
            }

            driver.get(prop.getProperty("url"));

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");

                if (parts.length >= 7) {
                    Cookie cookie = new Cookie(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3],
                            parts[4].equals("0") ? null : new Date(Long.parseLong(parts[4])),
                            Boolean.parseBoolean(parts[5]),
                            Boolean.parseBoolean(parts[6])
                    );

                    driver.manage().addCookie(cookie);
                }
            }

            reader.close();

            driver.navigate().refresh();

            logger.info("Cookies loaded");

        } catch (Exception e) {
            logger.warn("Failed to load cookies: {}", e.getMessage());
        }
    }

    public void saveCookies() {
        try {
            File file = new File("cookies.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            for (Cookie cookie : driver.manage().getCookies()) {
                writer.write(cookie.getName() + ";" +
                        cookie.getValue() + ";" +
                        cookie.getDomain() + ";" +
                        cookie.getPath() + ";" +
                        (cookie.getExpiry() != null ? cookie.getExpiry().getTime() : "0") + ";" +
                        cookie.isSecure() + ";" +
                        cookie.isHttpOnly());
                writer.newLine();
            }

            writer.close();
            logger.info("Cookies saved");

        } catch (Exception e) {
            logger.error("Failed to save cookies", e);
        }
    }

    // =========================
    // LOGIN
    // =========================
    private void performManualLogin() {

        String username = prop.getProperty("username");
        String password = prop.getProperty("password");

        if (username == null || password == null) {
            throw new RuntimeException("Username/password missing in config");
        }

        loginPage = new LoginPage(driver);
        loginPage.login(username, password);

        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> !d.getCurrentUrl().contains("login"));

        logger.info("Login successful");
    }
}