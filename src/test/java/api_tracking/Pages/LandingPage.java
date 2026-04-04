package api_tracking.Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LandingPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public LandingPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // Locators
    private By designStudio = By.xpath("//div[contains(@class,'landingPageAntButton') and contains(.,'Design Studio')]");
    private By testCase = By.xpath("//div[@class='landing_header_middle_label font-bold text-sm ' and text()='Test Case']");
    private By execution = By.xpath("//div[@class='landing_header_middle_label font-bold text-sm ' and text()='Execution']");
    private By reports = By.xpath("//div[@class='landing_header_middle_label font-bold text-sm ' and text()='Reports']");

    public void clickDesignStudio() {
        WebElement ds = wait.until(ExpectedConditions.elementToBeClickable(designStudio));
        ds.click();
    }

    public void clickTestCase() {
        WebElement tc = wait.until(ExpectedConditions.elementToBeClickable(testCase));
        tc.click();
    }

    public void clickExecution() {
        WebElement ex = wait.until(ExpectedConditions.elementToBeClickable(execution));
        ex.click();
    }

    public void clickReports() {
        WebElement rp = wait.until(ExpectedConditions.elementToBeClickable(reports));
        rp.click();
    }
}
