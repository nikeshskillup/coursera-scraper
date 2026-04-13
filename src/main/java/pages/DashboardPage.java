package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DashboardPage {
    private final WebDriver driver;

    @FindBy(xpath = "//div[@data-testid='user-avatar']")
    WebElement accountIcon;

    @FindBy(xpath = "//button[@id='logout-btn']")
    WebElement logOutLink;

    @FindBy(xpath = "//button[@aria-label='Select language: English' and @type='button']")
    public WebElement languageDropDown;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(this.driver, this);
    }

    public boolean isAccountIconDisplayed() {
        return accountIcon.isDisplayed();
    }

    public HomePage clickOnAccountIcon() {
        accountIcon.click();
        logOutLink.click();
        return new HomePage(driver);
    }

    public void selectLanguage(String language) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOf(languageDropDown));

        Select select = new Select(languageDropDown);
        select.selectByVisibleText(language);
    }
}