package com.morningstar.environmentreadiness.page;

import com.morningstar.automation.base.core.utils.SeleniumUtil;
import com.morningstar.environmentreadiness.utils.ECLogger;
import com.morningstar.environmentreadiness.utils.Wait;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SigninPage {

    private WebDriver driver = null;
    private Wait wait = null;
    private static final ECLogger logger = new ECLogger(SigninPage.class);

    public SigninPage(WebDriver driver) {
        this.driver = driver;
        wait = new Wait(driver);
    }

    public WebElement adlNativeUsernameTextbox() {

        return SeleniumUtil.waitForElementVisible(driver, By.name("login"));

    }

    public WebElement adlNativePasswordTextbox() {
        return SeleniumUtil.waitForElementVisible(driver, By.name("pwd"));
    }

    public WebElement adlNativeLoginButton() {
        return SeleniumUtil.waitForElementVisible(driver, By.name("Submit"));
    }

    public WebElement profileIcon(){
        return SeleniumUtil.waitForElementVisible(driver, By.id("profilepic"));
    }
    public WebElement morningstarLogo() {
        return SeleniumUtil.waitForElementVisible(driver, By.className("mstar-logo"));
    }

    public WebElement emailTextbox() {
        return SeleniumUtil.waitForElementVisible(driver, By.id("txtEmail"));
    }

    public WebElement emailLabel() {
        return SeleniumUtil.waitForElementVisible(driver, By.xpath("//label[@for='txtEmail']"));
    }

    public WebElement passwordLabel() {
        return SeleniumUtil.waitForElementVisible(driver, By.xpath("//label[@for='txtPassword']"));
    }

    public WebElement rememberMeLabel() {
        return SeleniumUtil.waitForElementVisible(driver, By.xpath("//label[@for='chkRemember']"));
    }

    public WebElement passwordTextbox() {
        return SeleniumUtil.waitForElementVisible(driver, By.id("txtPassword"));
    }

    public WebElement signinButton() {
        return SeleniumUtil.waitForElementVisible(driver, By.id("btnSignIn"));
    }

    public WebElement signinWithMorningstarButton(){
        return SeleniumUtil.waitForElementVisible(driver, By.className("uim-signin-button-text"));
    }
}
