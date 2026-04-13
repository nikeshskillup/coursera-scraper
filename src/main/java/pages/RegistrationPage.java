package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class RegistrationPage {
    private final WebDriver driver;

    @FindBy(xpath = "//h1[@class='_1hchl1r']")
    WebElement signInHeader;

    @FindBy(name = "name")
    public WebElement fullName;

    @FindBy(name = "email")
    public WebElement email;

    @FindBy(name = "password")
    public WebElement pswd;

    @FindBy(xpath = "//button[@data-e2e='signup-form-submit-button']")
    public WebElement signInButton;

    public RegistrationPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(this.driver, this);
    }

    public boolean isSignUpDisplayed() {
        return signInHeader.isDisplayed();
    }

    public void signUpUser(String name, String emailAddress, String password) {
        fullName.sendKeys(name);
        email.sendKeys(emailAddress);
        pswd.sendKeys(password);
        signInButton.click();
    }

    public void clearFields() {
        fullName.clear();
        email.clear();
        pswd.clear();
    }
}