package com.morningstar.environmentreadiness.utils;

import com.morningstar.automation.base.core.configurations.Environment;
import com.morningstar.automation.base.core.utils.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Wait {
    private static final Logger logger = Logger.getLogger(Wait.class);
    private WebDriver driver = null;
    private long timeout = Environment.getTimeOutInSeconds();//Environment.getTimeOutInSeconds();

    public Wait(WebDriver driver) {
        this.driver = driver;
    }

    public void untilPresenceOfElements(final By locator) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public void untilAllElementPresence(final By locator) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public void forInvisibilityOf(WebElement locator) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.invisibilityOf(locator));
    }

    public void forStalenessOf(WebElement locator) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.stalenessOf(locator));
    }

    public void waitForElementToBeClickable(WebElement locator) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
}
