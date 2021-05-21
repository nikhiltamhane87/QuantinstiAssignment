package com.morningstar.automation.base.core;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.google.common.base.Strings;
import com.morningstar.automation.base.cons.BaseCons;
import com.morningstar.automation.base.core.annotation.MorningstarAutomationAnnotation;
import com.morningstar.automation.base.core.beans.TestDataBean;
import com.morningstar.automation.base.core.beans.TestRailInfoBean;
import com.morningstar.automation.base.core.beans.UserBean;
import com.morningstar.automation.base.core.configurations.Environment;
import com.morningstar.automation.base.core.testdata.TestDataUtil;
import com.morningstar.automation.base.core.users.UserManager;
import com.morningstar.automation.base.core.utils.Logger;
import com.morningstar.automation.base.core.utils.TestObjectManager;
import com.morningstar.automation.base.core.utils.TestRailTestObjectManager;
import com.morningstar.automation.base.core.utils.Util;
import com.morningstar.automation.base.dynamic.DynamicUtil;
import com.morningstar.automation.base.testrail.annotation.TestRailCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


public abstract class AbstractTest implements IDriver {
    private static final Logger logger = Logger.getLogger(AbstractTest.class);
    public ExtentHtmlReporter htmlReporter;
    public ExtentReports extent;
    public ExtentTest test;

    private static final String SET_UP_TEST_CASE_ID = "[SetUp]-{BaseBeforeMethod}-Test Case ID:";

    private final ThreadLocal<WebDriver> webDriverThreadLocal = new InheritableThreadLocal<WebDriver>() {
        @Override
        public WebDriver initialValue() {
            return null;
        }
    };


    /**
     * @deprecated The old version of this method had a race condition on method name causing data providers and
     * repeated invocations to fail. As a result of fixing this, the method no longer requires a methodname parameter.
     * @param method No longer needed. Use {@link #getDriver()} instead.
     * @return the WebDriver instance allocated for this test.
     */
    @Deprecated
    protected WebDriver getDriver(Method method) {
        return getDriver();
    }

    public WebDriver getDriver() {
        return webDriverThreadLocal.get();
    }

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext context) {
        String mode = Util.getMode();
        if (StringUtils.equalsIgnoreCase(BaseCons.DEBUG_MODE.toString(), mode) && !Util.isUseGrid()) {
            checkForTooManyWindows(context);
        }

        if(Util.isSendResultToTestRail()
                && !Strings.isNullOrEmpty(Environment.getTestRailPW())
                && !Strings.isNullOrEmpty(Environment.getTestRailUserName())) {
            ITestNGMethod[] tests = context.getAllTestMethods();
            for (int count = 0; count < tests.length; count++) {
                if(isTestRailAnnotationPresent(tests[count])) {
                    TestRailInfoBean infoBean = new TestRailInfoBean(getTestRailIDFromAnnotation(tests[count]), getTesetRailProjectIDFromAnnotation(tests[count]));
                    TestRailTestObjectManager.addTestNameTestRailInfo(tests[count].getMethodName(), infoBean);
                }
            }
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(ITestContext context, Method method) {
        String methodName = method.getName();
        try {
            DynamicUtil.setTimeout(Environment.getTimeOutInSeconds());
            DynamicUtil.setPollingInterval(Environment.getSleepInMillis());
            logger.info(SET_UP_TEST_CASE_ID + methodName + "====BaseBeforeMethod start");

            //Get environment
            logger.info(SET_UP_TEST_CASE_ID + methodName + "====current env: " + Util.getEnvStr());
            setUser(method);

            setDriver(methodName);

            // log node to help to debug on Grid
            if (Util.isUseGrid()) {
                logger.info(SET_UP_TEST_CASE_ID + methodName + "===current node: "
                        + Util.GetNodeName(getDriver()));
            }

            String owner = this.getOwner(method);
            TestObjectManager.addOwner(methodName, owner);
            logger.info(SET_UP_TEST_CASE_ID + methodName + "====Add owner into TestObjectManager...");

            logger.info(SET_UP_TEST_CASE_ID + methodName + "====Loading test data...");
            this.initTestData(context, method);
            logger.info(SET_UP_TEST_CASE_ID + methodName + "====Load test data successfully.");

            logger.info(SET_UP_TEST_CASE_ID + methodName + "====BaseBeforeMethod end");
        } catch (Exception ex) {
            logger.error(SET_UP_TEST_CASE_ID + methodName + "====" + ex.getMessage());
            logger.error(SET_UP_TEST_CASE_ID + methodName + "====The stracktrace of this case====");
            ex.printStackTrace();
            Assert.fail(SET_UP_TEST_CASE_ID + methodName + "====ERROR:Fail to SetUp");
        }
    }

    public void setDriver(String methodName) {
        WebDriver driver = DriverFactory.newDriver(methodName);
        webDriverThreadLocal.set(driver);
        // Maximize browser window
        driver.manage().window().maximize();
    }

    private void loadHomepage(String methodName, WebDriver driver) {
        String homeURL = Environment.getHomePageUrl();
        driver.get(homeURL);
        logger.info(SET_UP_TEST_CASE_ID + methodName + "====Navigating to URL: " + homeURL);

        // if the URL is failed to forward, refresh the browser again
        while (driver.getCurrentUrl().contains("undefined")) {
            logger.info(SET_UP_TEST_CASE_ID + methodName + "====Re-Navigating to URL: " + homeURL);
            driver.get(homeURL);
        }
        logger.info(SET_UP_TEST_CASE_ID + methodName + "====Navigate to URL: " + homeURL + " sucessfully.");
    }

    /**
     * @deprecated This method has a race condition on the method name. If two methods run in parallel have the same name
     * or are run in parallel, value will be overridden
     * @param method
     */
    @Deprecated
    protected void setUser(Method method) {
        //Get user type
        String userType = null;
        MorningstarAutomationAnnotation annotation = method.getAnnotation(MorningstarAutomationAnnotation.class);
        if (annotation != null) {
            userType = annotation.userType();

            logger.info(SET_UP_TEST_CASE_ID + method.getName() + "====Get user type successfully.");

            //Get user by type
            UserBean user = UserManager.getUser(userType);
            logger.info(SET_UP_TEST_CASE_ID + method.getName() + "====Get the user you want successfully.");

            //Add user into ThreadLocal
            logger.info(SET_UP_TEST_CASE_ID + method.getName() + "====Add the user into TestObjectManager...");
            TestObjectManager.addUser(method.getName(), user);
            logger.info("[SetUp]Add test case ID: " + method.getName() + ", user: "+TestObjectManager.getUser(method.getName())+" into TestObjectManager successfully.");
        }
        else
            logger.info("[SetUp]didn't set user attribute in Annotation[MorningstarAutomationAnnotation], skip use it");
    }

    /**
     * @deprecated This method has a race condition on the method name. If two methods have the same name but
     * different owners, value returned will be incorrect
     */
    @Deprecated
    public String getOwner(Method method) {
        String result = "";
        MorningstarAutomationAnnotation annotation = method.getAnnotation(MorningstarAutomationAnnotation.class);
        if (annotation != null) {
            result = annotation.owner();
        }
        return result;
    }

    /**
     * go through the src/test/resources/testdata/ to check all the excel file
     * Caution: the excel file should be xls(Excel2003)
     * @deprecated 	 This method has a race condition on the method name. If two methods have the same name or a method is
     * invoked multiple times in parallel, value returned will be incorrect
     */
    @Deprecated
    protected void initTestData(ITestContext context, Method method) {
        String testcaseId = method.getName();
        List<TestDataBean> data = TestDataUtil.getTestData(testcaseId);
        //duplicated function with TestDataUtil
        //TestObjectManager.addTestData(method.getName(), data);
    }

    /**
     *  @deprecated This method has a race condition on method name. It will malfunction if multiple tests with the
     *  same name are executed in parallel.
     */
    @Deprecated
    public UserBean getUser(Method method) {
        return TestObjectManager.getUser(method.getName());
    }

    public String getTestDataByKey(String testCaseId, String parameter) {
        List<TestDataBean> data = TestDataUtil.getTestData(testCaseId);
        String env = Environment.getEnvironmentBean().getType().toLowerCase();

        for (TestDataBean bean : data) {
            if (bean.getParameter().equals(parameter)) {
                Map<String, String> dataMap = bean.getDataMap();
                return dataMap.get(env.toLowerCase());
            }
        }
        return null;
    }

    public void closeBrowserSession() throws IOException {
        if(Environment.getDefaultPlatform().contains("WINDOWS")){
            Runtime.getRuntime().exec("taskkill /F /IM ChromeDriver*");
            Runtime.getRuntime().exec("taskkill /F /IM IEDriverServer*");
            Runtime.getRuntime().exec("taskkill /F /IM Firefox.exe");
        }else{
            Runtime.getRuntime().exec("pkill chromedriver");
            Runtime.getRuntime().exec("pkill firefox");
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestContext context, Method method) {
        String prefix = "[TearDown]-{BaseAfterMethod}-Test Case ID:";
        logger.info(prefix + method.getName() + "====BaseAfterMethod start");
        WebDriver driver = this.getDriver();
        try {
            logger.info(prefix + method.getName() + "====URL: " + driver.getCurrentUrl());
        } catch (Exception ex) {
            logger.error(prefix + method.getName() + "====" + ex.getMessage());
            logger.error(prefix + method.getName() + "====The stracktrace of this case====");
            ex.printStackTrace();
            logger.error(prefix + method.getName() + "====ERROR:Fail to TearDown");
        }finally{
            try{
                driver.quit();
            }catch(Exception ex){
                logger.error("[Quit]-{BaseAfterMethod}-Test Case ID:" + method.getName() + "====" + ex.getMessage());
            }finally{
                webDriverThreadLocal.set(null);
                logger.info("[Quit]Test Case ID:" + method.getName() + "====All browsers quit successfully on Windows.");
            }
        }

        logger.info(prefix + method.getName() + "====BaseAfterMethod end");
    }

    protected void checkForTooManyWindows(ITestContext context) {
        int threadCount = context.getSuite().getXmlSuite().getThreadCount();
        if (threadCount > getMaximumLocalWindowCount()) {
            promptUserWindowCount(threadCount);
        }
    }

    /**
     * Written as a method so that subclasses can override
     * @return 5 -- the largest potentially reasonable number of windows to run on a local development machine
     */
    protected int getMaximumLocalWindowCount() {
        return 5;
    }

    protected void promptUserWindowCount(int numWindows) {
        try {
            int option = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to run " + numWindows + " browser instances locally?");
            if (option != 0)
                System.exit(0);
        } catch (HeadlessException e) {
            logger.error("Debug mode is not available on the grid. Please fix environment.xml. \n" + e);
        }
    }


    /**
     * @author Brian Chen
     * @param testNgMethod
     * @return Decide if a test case has TestRail annotation
     */
    private boolean isTestRailAnnotationPresent(ITestNGMethod testNgMethod) {
        Method method = testNgMethod.getConstructorOrMethod().getMethod();
        if (method.isAnnotationPresent(TestRailCase.class)) {
            TestRailCase testRailAnnotation = method.getAnnotation(TestRailCase.class);
            if(testRailAnnotation.testRailID() != -1 && testRailAnnotation.projectId() != -1 && testRailAnnotation.selfReporting() == true) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * @author Brian Chen
     * @param testNgMethod
     * @return get TestRail Id
     */
    private Integer getTestRailIDFromAnnotation (ITestNGMethod testNgMethod) {
        Method method = testNgMethod.getConstructorOrMethod().getMethod();
        return method.getAnnotation(TestRailCase.class).testRailID();
    }


    /**
     * @author Brian Chen
     * @param testNgMethod
     * @return get TestRail Project Id
     */
    private Integer getTesetRailProjectIDFromAnnotation(ITestNGMethod testNgMethod) {
        Method method = testNgMethod.getConstructorOrMethod().getMethod();
        return method.getAnnotation(TestRailCase.class).projectId();
    }

    @BeforeTest
    public void extentReportSetup() {
        logger.info("Extent Report Generation");
        htmlReporter = new ExtentHtmlReporter(System.getProperty("user.dir") + "/test-output/ExtentReport.html");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);

        test = extent.createTest("Environment Readiness Check : "+ Environment.getTeamName());

        htmlReporter.config().setDocumentTitle("Environment Readiness Check!!");
        htmlReporter.config().setReportName("ADLINT");
        htmlReporter.config().setTheme(Theme.STANDARD);
    }

    @AfterTest
    public void endReport() {
        extent.flush();
        logger.info("Extent Report Generation Completed");
    }

    @AfterMethod (alwaysRun = true)
    public void getResult(ITestResult result) throws Exception {
        logger.info("Collecting data for reporting");
        if(result.getStatus() == ITestResult.FAILURE) {
            //MarkupHelper is used to display the output in different colors
            test.log(Status.FAIL, MarkupHelper.createLabel(result.getName() + " - Test Case Failed", ExtentColor.RED));
            test.log(Status.FAIL, MarkupHelper.createLabel(result.getThrowable() + " - Test Case Failed", ExtentColor.RED));

            //To capture screenshot path and store the path of the screenshot in the string "screenshotPath"
            //We do pass the path captured by this method in to the extent reports using "logger.addScreenCapture" method.

            //	String Scrnshot=TakeScreenshot.captuerScreenshot(driver,"TestCaseFailed");
//            String screenshotPattPath = TakeScreenshot(getDriver(), result.getName());
            //To add it in the extent report

//            test.fail("Test Case Failed Snapshot is below " + test.addScreenCaptureFromPath(screenshotPath));
        } else if(result.getStatus() == ITestResult.SKIP) {
            //logger.log(Status.SKIP, "Test Case Skipped is "+result.getName());
            test.log(Status.SKIP, MarkupHelper.createLabel(result.getName() + " - Test Case Skipped", ExtentColor.ORANGE));
        }
        else if(result.getStatus() == ITestResult.SUCCESS) {
            test.log(Status.PASS, MarkupHelper.createLabel(result.getName()+" Test Case PASSED", ExtentColor.GREEN));
        }
        logger.info("Data Collection For Reporting Is Completed");
    }

    private static String TakeScreenshot(WebDriver driver, String screenshotName) throws IOException {
        String dateName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        TakesScreenshot ts = (TakesScreenshot) driver;
        File source = ts.getScreenshotAs(OutputType.FILE);

        // after execution, you could see a folder "FailedTestsScreenshots" under src folder
        String destination = System.getProperty("user.dir") + "/Screenshots/" + screenshotName + dateName + ".png";
        File finalDestination = new File(destination);
        FileUtils.copyFile(source, finalDestination);
        return destination;
    }
}
