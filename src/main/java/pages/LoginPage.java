package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginPage {
    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);
    private final WebDriver driver;

    @FindBy(xpath = "//h1[@class='_297whj']")
    WebElement logInHeader;

    @FindBy(name = "email")
    public WebElement username;

    @FindBy(name = "password")
    public WebElement password;

    @FindBy(xpath = "//button[@data-e2e='login-form-submit-button']")
    public WebElement loginBtn;

    @FindBy(xpath = "Forgot password?")
    public WebElement forgotPasswordLink;

    @FindBy(xpath = "//div[@role='alert' and @aria-live='assertive']")
    WebElement errorMessage;

    @FindBy(xpath = "//button[text()='Sign up']")
    WebElement signUpLink;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(this.driver, this);
    }

    public boolean isSignInDisplayed() {
        return logInHeader.isDisplayed();
    }

    public RegistrationPage clickOnSignUpLink() {
        signUpLink.click();
        return new RegistrationPage(driver);
    }

    public DashboardPage login(String un, String pwd) {
        logger.info("Entering login credentials for user: {}", un);
        username.sendKeys(un);
        password.sendKeys(pwd);
        logger.info("Clicking login button...");
        loginBtn.click();

        logger.info("Please solve the CAPTCHA manually in the browser within 30 seconds...");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for manual CAPTCHA solving: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

        logger.info("Resuming after CAPTCHA pause. Checking if login succeeded...");
        return new DashboardPage(driver);
    }

    public ForgottenPasswordPage clickForgotPasswordLink() {
        forgotPasswordLink.click();
        return new ForgottenPasswordPage(driver);
    }

    public String getErrorMessage() {
        return errorMessage.getText();
    }

    public void clearFields() {
        username.clear();
        password.clear();
    }
}