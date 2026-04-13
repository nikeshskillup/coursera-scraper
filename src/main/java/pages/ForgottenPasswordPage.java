package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ForgottenPasswordPage {
    private final WebDriver driver;

    @FindBy(xpath = "//h1[@class='_1mq8col']")
    WebElement passwordHeader;

    @FindBy(name = "email")
    WebElement resetEmail;

    @FindBy(name = "captchaCode")
    WebElement captchaCode;

    @FindBy(xpath = "//*[@id= \"inputArea\"]/form/div[4]/p/button")
    WebElement resetButton;

    @FindBy(xpath = "//*[@id=\"inputArea\"]/form/div[4]/p/a")
    WebElement cancelLink;

    public ForgottenPasswordPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(this.driver, this);
    }

    public boolean isForgottenPasswordDisplayed() {
        return passwordHeader.isDisplayed();
    }
}