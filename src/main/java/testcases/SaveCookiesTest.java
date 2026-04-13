package testcases;

import base.BasePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.HomePage;
import pages.LoginPage;

public class SaveCookiesTest extends BasePage {
    private static final Logger logger = LoggerFactory.getLogger(SaveCookiesTest.class);

    public static void main(String[] args) {
        SaveCookiesTest test = new SaveCookiesTest();
        try {
            test.setUp();

            HomePage homePage = new HomePage(test.getDriver());
            LoginPage loginPage = homePage.clickOnSignIn();
            loginPage.login("gaurav.kapoor@skillup.online", "Jill@1233");

            logger.info("Please solve the CAPTCHA manually and navigate to https://www.coursera.org/admin-v2/ibm-skills-network/home/courses within 60 seconds...");
            try {
                //Thread.sleep(300,000); // 10 minutes
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }

            test.saveCookies();
            logger.info("Cookies saved. You can now use them in tests.");
        } catch (Exception e) {
            logger.error("Error in SaveCookiesTest: {}", e.getMessage(), e);
            throw e;
        } finally {
            test.closeDriver();
            logger.info("Driver closed in finally block.");
            // Force JVM exit to ensure all threads are terminated
            System.exit(0);
        }
    }
}