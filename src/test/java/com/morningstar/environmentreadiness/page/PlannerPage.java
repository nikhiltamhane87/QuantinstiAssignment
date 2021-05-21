package com.morningstar.environmentreadiness.page;

import com.morningstar.automation.base.core.utils.SeleniumUtil;
import com.morningstar.environmentreadiness.utils.ECLogger;
import com.morningstar.environmentreadiness.utils.Wait;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PlannerPage {
    private WebDriver driver = null;
    private Wait wait = null;
    private static final ECLogger logger = new ECLogger(PlannerPage.class);

    public PlannerPage(WebDriver driver) {
        this.driver = driver;
        wait = new Wait(driver);
    }


    public List<WebElement> getLastRow() {
        wait.untilAllElementPresence(By.cssSelector("table tbody tr:last-child"));
        List<WebElement> lastRow = SeleniumUtil.waitForAllElementsPresent(driver, By.cssSelector("table tbody tr:last-child"), "Error loading the last row");
        return lastRow;
    }

    public WebElement getUserNameLocator() {
        wait.untilPresenceOfElements(By.cssSelector("#UserEmail"));
        WebElement userNameLocator = SeleniumUtil.waitForElementPresent(driver, By.cssSelector("#UserEmail"), "Error loading user name locator");
        return userNameLocator;
    }

    public WebElement getPasswordLocator() {
        wait.untilPresenceOfElements(By.cssSelector("#UserPass"));
        WebElement passwordLocator = SeleniumUtil.waitForElementPresent(driver, By.cssSelector("#UserPass"), "Error loading password locator");
        return passwordLocator;
    }


    public WebElement getSubmitBtn() {
        wait.untilPresenceOfElements(By.xpath("//input[@value='Log On']"));
        WebElement submitBtnLocator = SeleniumUtil.waitForElementPresent(driver, By.xpath("//input[@value='Log On']"), "Error loading submit locator");
        return submitBtnLocator;
    }

    public WebElement getSelectBoxLocator() {
        wait.untilPresenceOfElements(By.cssSelector("select#bundleSourceDropdown"));
        WebElement selectboxLocator = SeleniumUtil.waitForElementPresent(driver, By.cssSelector("select#bundleSourceDropdown"), "Error loading select box");
        return selectboxLocator;
    }

    public WebElement getOptionLocator(String name1, String name2) {
        wait.untilPresenceOfElements(By.cssSelector("optgroup[value="+name1+"] option[value="+name2+"]"));
        WebElement optionLocator = SeleniumUtil.waitForElementPresent(driver, By.cssSelector("optgroup[value="+name1+"] option[value="+name2+"]"), "Error loading option locator");
        return optionLocator;
    }
}
