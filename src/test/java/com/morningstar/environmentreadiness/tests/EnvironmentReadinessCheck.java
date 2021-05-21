package com.morningstar.environmentreadiness.tests;

import com.aventstack.extentreports.ExtentTest;
import com.morningstar.environmentreadiness.common.CommonNavigation;
import com.morningstar.environmentreadiness.flows.SigninFlow;
import com.morningstar.environmentreadiness.utils.ECLogger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EnvironmentReadinessCheck extends CommonNavigation {

    private static final ECLogger logger = new ECLogger(EnvironmentReadinessCheck.class);
    private SigninFlow signinFlow = null;
    public ExtentTest test;
    WebDriver driver;


    @BeforeMethod(alwaysRun = true)
    public void preRequisitesForComponentLoader() {
        driver = getDriver();
        signinFlow = new SigninFlow(driver);
    }

    @Test(priority = 1,enabled = true)
    public void verifyApplicationOn_PRODEnvironment() {
        logger.info("Verifying Application On PROD Environment ---- Started");
        Assert.assertTrue(isSiteUp, environment+" Server Is Not Responding");
        signinFlow.verifyNativeLogin();
        logger.info("Verifying Application On PROD Environment ---- Ended");
    }

    @Test(priority = 2,enabled = true)
    public void verifyApplicationOn_QAEnvironment() {
        logger.info("Verifying Application On QA Environment ---- Started");
        Assert.assertTrue(isSiteUp, environment+" Server Is Not Responding");
        signinFlow.verifyNativeLogin();
        logger.info("Verifying Application On QA Environment ---- Ended");
    }

    @Test(priority = 3,enabled = true)
    //public void verifyApplicationOn_INTQAEnvironmentV1() {
    public void verifyApplicationOn_STGEnvironment()   {

        logger.info("Verifying Application On Integration STG Environment ---- Started");
        Assert.assertTrue(isSiteUp, environment + " Server Is Not Responding");
        signinFlow.verifyNativeLogin();
        //signinFlow.verifyUIMLoginPage();
        logger.info("Verifying Application On Integration QA-V1 Environment ---- Ended");
    }


    /*@Test(priority = 4,enabled = true)
    public void verifyApplicationOn_INTQAEnvironmentV2() {
        logger.info("Verifying Application On Integration QA-V2 Environment ---- Started");
        Assert.assertTrue(isSiteUp, environment+" Server Is Not Responding");
        signinFlow.verifyUIMLoginPage();
        logger.info("Verifying Application On Integration QA-V2 Environment ---- Ended");
    }*/
}
