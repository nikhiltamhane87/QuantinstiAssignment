package com.morningstar.environmentreadiness.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class WaitUtils {
    private static final ECLogger logger = ECLogger.getLogger(WaitUtils.class);
    public static Wait wait = null;

    public static WebElement isElementPresent(WebDriver driver, By locator) {
        WebElement isElementPresent = null;
        wait = new Wait(driver);
        try {
            wait.untilPresenceOfElements(locator);
            isElementPresent = driver.findElement(locator);
            return  isElementPresent;
        } catch (Exception e){
            logger.debug("Element is not present === " + locator);
            return null;
        }
    }

    public static List<WebElement> isElementAllPresent(WebDriver driver, By locator) {
        List<WebElement> isAllElementPresent = null;
        wait = new Wait(driver);
        try {
            wait.untilAllElementPresence(locator);
            isAllElementPresent = driver.findElements(locator);
            return isAllElementPresent;
        } catch (Exception e) {
            logger.debug("All Elements are not present === " + locator);
            return null;
        }
    }
}
