package com.morningstar.environmentreadiness.flows;

import com.morningstar.environmentreadiness.page.PlannerPage;
import com.morningstar.environmentreadiness.utils.Wait;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.List;

public class PlannerFlow {

    private WebDriver driver = null;
    private PlannerPage plannerPage = null;
    private Wait wait = null;

    public PlannerFlow(WebDriver driver) {
        this.driver = driver;
        plannerPage = new PlannerPage(driver);
        wait = new Wait(driver);
    }

    private void navigateToPage(String pageURL) {
        driver.get(pageURL);
    }

    public void navigateToQAPage() {
        navigateToPage("http://ecquikrqa.morningstar.com/portfolio-planner/#!/demo-dev");
    }

    public void verifyLastRowOFResultantTable() {
       List<WebElement> lastRow =  plannerPage.getLastRow();
       boolean isLastRowPresent = false;
       if(lastRow.size() != 0) {
           isLastRowPresent = true;
       }
        Assert.assertTrue(isLastRowPresent, "Planner Page Is Working Fine");
    }

    public void navigateToPRODPage() {
        navigateToPage("http://eultrc.morningstar.com/quikr/tools/portfolio-planner/global-demo-dev/index.aspx");
    }

    public void LoginToSite() {
        WebElement userName = plannerPage.getUserNameLocator();
        WebElement password = plannerPage.getPasswordLocator();
        WebElement submitButton = plannerPage.getSubmitBtn();

        userName.sendKeys("tools@morningstar.com");
        password.sendKeys("tools");
        submitButton.click();
    }

    public void selectBundle(String bundleName) {
        String[] bundle = bundleName.split("-");
        WebElement selectBox = plannerPage.getSelectBoxLocator();
        WebElement option = plannerPage.getOptionLocator(bundle[0], bundle[1]);

        selectBox.click();
        option.click();
    }
}
