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
import utils.TestUtil;

import java.io.*;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.time.Duration;

public class BasePage {
    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    protected WebDriver driver;
    protected Properties prop;
    protected LoginPage loginPage;
    protected ForgottenPasswordPage forgottenPasswordPage;
    protected HomePage homePage;
    protected RegistrationPage registrationPage;
    protected DashboardPage dashboardPage;
    protected AdminCoursesPage adminCoursesPage;
    protected CourseRatingsScraper courseRatingsScraper;

    public BasePage() {
        try {
            prop = new Properties();
            FileInputStream ip = new FileInputStream(System.getProperty("user.dir") + "/src/main/java/config/config.properties");
            prop.load(ip);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("config.properties file not found at the specific path");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading config.properties file");
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setUp() {
        initializeDriver();
        loadCookies();

        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("login") || currentUrl.contains("signin")) {
            logger.warn("Authentication failed with cookies. Attempting manual login...");
            performManualLogin();
            saveCookies();
        }

        logger.info("Initializing page objects with driver: {}", driver);
        loginPage = new LoginPage(driver);
        homePage = new HomePage(driver);
        registrationPage = new RegistrationPage(driver);
        dashboardPage = new DashboardPage(driver);
        adminCoursesPage = new AdminCoursesPage(driver);
        courseRatingsScraper = new CourseRatingsScraper(driver);
        logger.info("adminCoursesPage initialized: {}", adminCoursesPage != null);
        logger.info("courseRatingsScraper initialized: {}", courseRatingsScraper != null);
    }

    public void initializeDriver() {
    String browserName = prop.getProperty("browser");
    Boolean headlessMode = Boolean.parseBoolean(prop.getProperty("headless"));

    logger.info("Initializing WebDriver with browser: {}, headless: {}", browserName, headlessMode);
    if (browserName.equals("chrome")) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        
        // CRITICAL: Disable CDP warnings and automation detection
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--disable-blink-features=AutomationControlled");
        
        // Network and performance options
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-software-rasterizer");
        options.addArguments("--disable-extensions");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--start-maximized");
        
        // Disable renderer backgrounding
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-breakpad");
        
        // Network timeout
        options.addArguments("--net-log-capture-mode=IncludeCookiesAndCredentials");
        
        // Page load strategy
        options.setPageLoadTimeout(Duration.ofSeconds(90));
        
        if (headlessMode) {
            options.addArguments("--headless=new");
        }
        
        driver = new ChromeDriver(options);
    }

    if (driver == null) {
        throw new RuntimeException("Failed to initialize WebDriver. Check browser configuration.");
    }

    driver.manage().window().maximize();
    driver.manage().deleteAllCookies();
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(90));
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(60));

    String url = prop.getProperty("url");
    logger.info("Navigating to URL: {}", url);
    try {
        driver.get(url);
    } catch (org.openqa.selenium.TimeoutException e) {
        logger.warn("Initial page load timeout, but continuing with test...");
        // Continue despite timeout
    }
}

    public void closeDriver() {
        try {
            if (driver != null) {
                driver.quit();
                logger.info("WebDriver closed successfully.");
            }
        } catch (Exception e) {
            logger.error("Error closing WebDriver: {}", e.getMessage(), e);
        } finally {
            driver = null;
            // Ensure all WebDriver service threads are stopped
            try {
                Runtime.getRuntime().exec("pkill -f chromedriver");
                logger.info("Terminated any lingering chromedriver processes.");
            } catch (IOException e) {
                logger.warn("Failed to terminate chromedriver processes: {}", e.getMessage());
            }
        }
    }

    private void loadCookies() {
        try {
            File cookieFile = new File("cookies.txt");
            if (!cookieFile.exists()) {
                logger.warn("Cookies file not found at {}. Please log in manually and save cookies first.", cookieFile.getAbsolutePath());
                return;
            }

            String baseUrl = prop.getProperty("url");
            logger.info("Navigating to base URL before adding cookies: {}", baseUrl);
            driver.get(baseUrl);

            BufferedReader reader = new BufferedReader(new FileReader(cookieFile));
            String line;
            int cookieCount = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 7) {
                    try {
                        long expiryTime = Long.parseLong(parts[4]);
                        Date expiryDate = expiryTime == 0 ? null : new Date(expiryTime);
                        Cookie cookie = new Cookie(parts[0], parts[1], parts[2], parts[3],
                                expiryDate, Boolean.parseBoolean(parts[5]), Boolean.parseBoolean(parts[6]));
                        driver.manage().addCookie(cookie);
                        cookieCount++;
                        logger.debug("Added cookie: {}", cookie.getName());
                    } catch (Exception e) {
                        logger.warn("Failed to add cookie from line: {}. Error: {}", line, e.getMessage());
                    }
                } else {
                    logger.warn("Invalid cookie format in line: {}", line);
                }
            }
            reader.close();
            logger.info("Loaded {} cookies successfully.", cookieCount);

            Set<Cookie> cookies = driver.manage().getCookies();
            logger.info("Total cookies in session after loading: {}", cookies.size());
            cookies.forEach(cookie -> logger.debug("Cookie in session: {}", cookie));

            logger.info("Refreshing page to apply cookies...");
            driver.navigate().refresh();
        } catch (Exception e) {
            logger.error("Failed to load cookies: {}", e.getMessage(), e);
        }
    }

    public void saveCookies() {
        try {
String adminUrl = "https://www.coursera.org/admin/ibm-skills-network/home/courses";
//String adminUrl = "https://www.coursera.org/admin/skillup-ed-tech/home/courses";
            logger.info("Navigating to admin URL to save cookies: {}", adminUrl);
            driver.get(adminUrl);

            File cookieFile = new File("cookies.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(cookieFile));
            Set<Cookie> cookies = driver.manage().getCookies();
            for (Cookie cookie : cookies) {
                writer.write(cookie.getName() + ";" + cookie.getValue() + ";" + cookie.getDomain() + ";" +
                        cookie.getPath() + ";" + (cookie.getExpiry() != null ? cookie.getExpiry().getTime() : "0") + ";" +
                        cookie.isSecure() + ";" + cookie.isHttpOnly());
                writer.newLine();
            }
            writer.close();
            logger.info("Cookies saved to cookies.txt");
        } catch (Exception e) {
            logger.error("Failed to save cookies: {}", e.getMessage(), e);
        }
    }

    private void performManualLogin() {
        try {
            loginPage = new LoginPage(driver);
            String username = prop.getProperty("username");
            String password = prop.getProperty("password");
            if (username == null || password == null) {
                throw new RuntimeException("Username or password not found in config.properties");
            }
            logger.info("Performing manual login with username: {}", username);
            loginPage.login(username, password);

            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(d -> d.getCurrentUrl().contains("coursera.org") && !d.getCurrentUrl().contains("login"));
            logger.info("Manual login successful. Current URL: {}", driver.getCurrentUrl());
        } catch (Exception e) {
            logger.error("Manual login failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to perform manual login", e);
        }
    }
}