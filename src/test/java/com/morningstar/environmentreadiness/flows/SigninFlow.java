package com.morningstar.environmentreadiness.flows;

import com.morningstar.environmentreadiness.page.SigninPage;
import com.morningstar.environmentreadiness.utils.Wait;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class SigninFlow {

    private WebDriver driver = null;
    private SigninPage signinPage = null;
    private Wait wait = null;

    public SigninFlow(WebDriver driver) {
        this.driver = driver;
        signinPage = new SigninPage(driver);
        wait = new Wait(driver);
    }

    public void verifyNativeLogin(){
        Assert.assertTrue(signinPage.adlNativeUsernameTextbox().isDisplayed());
        Assert.assertTrue(signinPage.adlNativePasswordTextbox().isDisplayed());
        //Assert.assertTrue(signinPage.adlNativeLoginButton().isDisplayed());
    }

    public void verifyUIMLoginPage(){
        Assert.assertTrue(signinPage.morningstarLogo().isDisplayed());
        Assert.assertTrue(signinPage.emailLabel().isDisplayed());
        Assert.assertTrue(signinPage.emailTextbox().isDisplayed());
        Assert.assertTrue(signinPage.passwordLabel().isDisplayed());
        Assert.assertTrue(signinPage.passwordTextbox().isDisplayed());
        Assert.assertTrue(signinPage.rememberMeLabel().isDisplayed());
        Assert.assertTrue(signinPage.signinButton().isDisplayed());
    }
}
