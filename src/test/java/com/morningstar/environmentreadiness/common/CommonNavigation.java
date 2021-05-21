package com.morningstar.environmentreadiness.common;

import com.aventstack.extentreports.ExtentTest;
import com.morningstar.automation.base.core.AbstractTest;
import com.morningstar.automation.base.core.configurations.Environment;
import com.morningstar.environmentreadiness.utils.ECLogger;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

public class CommonNavigation extends AbstractTest {
    private static final ECLogger logger = ECLogger.getLogger(com.morningstar.environmentreadiness.common.CommonNavigation.class);
    public ExtentTest test;
    public static boolean isSiteUp;
    public static String environment = null;
    @BeforeMethod(alwaysRun = true)
    public void statusCheck(Method method, ITestContext context) throws IOException {
        logger.info("Checking status of the page");
        System.out.println(method.getName());
        String[] methodNameArray = method.getName().split("_");
        String pageURL = null;
        //String environment = null;
        switch (methodNameArray[1]) {
            case "QAEnvironment":
                pageURL = "https://advqa.morningstar.com/advisor/login/instlgn.asp?instid=INDDEM";
                environment = "QA";
                verifyStatusCode(pageURL, environment);
            break;
            case "PRODEnvironment":
                pageURL = "https://advtools.morningstar.com/advisor/login/instlgn.asp?INSTID=INDDEM";
                environment = "PROD";
                verifyStatusCode(pageURL, environment);
            break;
            case "STGEnvironment" :
                pageURL = "https://awsstgmain.morningstar.com/advisor/login/instlgn.asp?instid=INDDEM";
                environment = "STG";
                verifyStatusCode(pageURL, environment);
                break;

        }
        getDriver().get(pageURL);
    }

    private void verifyStatusCode(String pageURL, String environment) {
        int statusCode = 0;
        isSiteUp = false;

        try {
            URL url = new URL(pageURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

           statusCode = connection.getResponseCode();
            logger.info("Status code for Environment '"+environment+"' ==== " + statusCode);
        } catch(Exception e) {
            logger.info("Error Occured while Checking The Status for Environment '"+environment+"' ==== " + e.getMessage());
        }
        if(statusCode == 200 || statusCode == 301) {
            isSiteUp = true;
        }
    }
}
