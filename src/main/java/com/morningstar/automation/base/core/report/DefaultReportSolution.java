package com.morningstar.automation.base.core.report;

import com.alibaba.fastjson.JSONObject;
import com.morningstar.automation.base.cons.BaseCons;
import com.morningstar.automation.base.core.beans.MailSenderInfo;
import com.morningstar.automation.base.core.beans.ReportBean;
import com.morningstar.automation.base.core.beans.ResultStatusEnum;
import com.morningstar.automation.base.core.beans.TestCaseResultBean;
import com.morningstar.automation.base.core.configurations.Environment;
import com.morningstar.automation.base.core.utils.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @Description: need depend on testng-result.xml
 * @author june.you@morningstar.com
 * @date 11/20/2013 23:15:30
 */
public class DefaultReportSolution {

    private static final Logger logger = Logger.getLogger(DefaultReportSolution.class);
    private ReportBean reportBean;
    private ISuite suite;
    private Date startDate;
    private Date endDate;
    private String rndNum;
    boolean flagPassed = false;
    boolean flagFailed = false;
    boolean flagSkipped = false;
    List<String> statusMessage = new ArrayList<String>();

    public DefaultReportSolution(){}

    public DefaultReportSolution(ISuite suite, Date startDate, Date endDate) {
        this.suite = suite;
        this.startDate = startDate;
        this.endDate = endDate;
        initDestPath();
        this.rndNum = Integer.toString((new Random()).nextInt(1000));
    }

    public String getTimeStamp() {
        String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z").format(startDate.getTime());
        return timeStamp;
    }

    public String getExcutiontimeStr() {
        String result = "";
        long distance = endDate.getTime() - startDate.getTime();
        long min = distance / (1000 * 60);
        if (min > 0) {
            result = min + "  min  ";
        }

        long s = (distance - min * 60 * 1000) / 1000;
        if (s > 0) {
            result += s + "  s";
        }

        if (result.isEmpty()) {
            result = "0 s";
        }

        return result;
    }
    /**
     get the basic information in each build
     Local: from Environment.xml
     Jenkins: from Environment.xml + Jenkins
     */
    public ReportBean getReportData() {
        if (reportBean == null) {
            String suiteName = suite.getName();
            logger.info("suiteName=" + suiteName);
            String outputPath = suite.getOutputDirectory();
            logger.info("outputPath=" + outputPath);

            String browser = Util.getBrowserType();
            String browserVersion = Util.getBrowserVersion();
            if (browserVersion != null && !browserVersion.isEmpty()) {
                browser += browserVersion;
            }

            String executionTime = getExcutiontimeStr();
            String url = Environment.getHomePageUrl();
            String environment = Environment.getEnvironmentBean().getType();
            String teamName = Environment.getTeamName();
            String environmentVerified = Environment.getEnvironmentVerified();
            String timeStamp = getTimeStamp();

            reportBean = new ReportBean();
            reportBean.setBrowser(browser);
            reportBean.setEnvironment(environment);
            reportBean.setExecutionTime(executionTime);
            reportBean.setSuite(suiteName);
            reportBean.setUrl(url);
            reportBean.setTeam(teamName);
            reportBean.setEnvironmentVerified(environmentVerified);
            reportBean.setTimeStamp(timeStamp);
        }

        return reportBean;
    }

    private List<String> getResultFileList() {
        List<String> resultFileList = new ArrayList<String>();
        Map<String, ISuiteResult> suiteResults = suite.getResults();
        Set<String> keySet = suiteResults.keySet();
        for (String key : keySet) {
            ISuiteResult sr = suiteResults.get(key);
            ITestContext tc = sr.getTestContext();
            resultFileList.add(tc.getName());
        }
        return resultFileList;
    }



    /*
    upload all the file generated in each build to specified FTP site, if can't or fail to do, the screenshot link may invalid
     */
    public void backupReport() {
        String targetFTPFolder = getFtpFolderName();
        FtpUploadUtil.upload(Environment.getTeamName(), targetFTPFolder, suite.getOutputDirectory());
    }

    /*
    generate report in HTML format
    1 collect all the TestNG build files and load them into TestCaseResultBean list
    2 generate different list according to the status of each bean
    3 go through the list and build some list for passed cases, failed cases and skipped cases
     */
    void generateReportV1() {
        List<String> resultFileList = getResultFileList();
        Document allDoc = DocumentHelper.createDocument();
        Element rootEl = allDoc.addElement("testsuites");
        for (String fileName : resultFileList) {
            try {
                String filePath = suite.getOutputDirectory() + File.separator + fileName + ".xml";
                logger.info("filePath=" + filePath);
                File file = new File(filePath);
                SAXReader saxReader = new SAXReader();
                Document doc = saxReader.read(file);
                //logger.info(doc.getRootElement().asXML());
                rootEl.add(doc.getRootElement().createCopy());
            } catch (DocumentException e) {
                e.printStackTrace();
                logger.error(e);
            }
        }

        List<TestCaseResultBean> results = parseTestResults(allDoc);
        //clean up the list and combine passed,failed and skipped test cases
        String reportContent = getReportContent(results);
        ReportBean bean = getReportData();

        File htmlfile = new File(suite.getOutputDirectory() + File.separator + bean.getSuite() + ".html");
        File xmlfile = new File(suite.getOutputDirectory() + File.separator + bean.getSuite() + ".xml");
        try {
            FileUtils.writeStringToFile(xmlfile, allDoc.asXML(), Charset.defaultCharset());
            FileUtils.writeStringToFile(htmlfile, reportContent, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String pdfFileName = getPdfFileName();
        //PdfReport report = new PdfReport(bean, suite.getOutputDirectory());
        //report.create(pdfFileName);
        PDFReportV2 report = new PDFReportV2(bean,suite.getOutputDirectory());
        report.GenerateReport(pdfFileName);
        logger.info("pdfReportName=" + pdfFileName);
    }


    /*
    generate report in HTML format
    1 collect all the TestNG build files and load them into TestCaseResultBean list
    2 generate different list according to the status of each bean
    3 go through the list and build some list for passed cases, failed cases and skipped cases
     */
    public void generateReport() {
        //List<String> resultFileList = getResultFileList();
        Document allDoc = DocumentHelper.createDocument();
        //Element rootEl = allDoc.addElement("testsuites");
        //for (String fileName : resultFileList) {
        try {
            String outPath = suite.getOutputDirectory();
            logger.info("filePath=" + outPath);
            File file = new File(outPath);
            String filePath = file.getParent() + File.separator+"testng-results.xml";
            logger.info("filePath=" + filePath);
            file = new File(filePath);
            if(!file.exists()) {
                logger.error("TestNG report file:" + filePath + " doesn't exist");
                return;
            }
            SAXReader saxReader = new SAXReader();
            allDoc = saxReader.read(file);
            //logger.info(doc.getRootElement().asXML());
            //rootEl.add(doc.getRootElement().createCopy());
        } catch (DocumentException e) {
            e.printStackTrace();
            logger.error(e);
        }
        //	}

        List<TestCaseResultBean> results = parseTestResults(allDoc);
        //clean up the list and combine passed,failed and skipped test cases
        String reportContent = getReportContent(results);
        ReportBean bean = getReportData();

        File htmlfile = new File(suite.getOutputDirectory() + File.separator + bean.getSuite() + ".html");
        File xmlfile = new File(suite.getOutputDirectory() + File.separator + bean.getSuite() + ".xml");
        try {
            FileUtils.writeStringToFile(xmlfile, allDoc.asXML(), Charset.defaultCharset());
            FileUtils.writeStringToFile(htmlfile, reportContent, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String pdfFileName = getPdfFileName();
        //PdfReport report = new PdfReport(bean, suite.getOutputDirectory());
        //report.create(pdfFileName);
        PDFReportV2 report = new PDFReportV2(bean,suite.getOutputDirectory());
        report.GenerateReport(pdfFileName);
        logger.info("pdfReportName=" + pdfFileName);
    }

    /**
     * Author: pli3
     * Created Date: 2015.12.23 16:32:24
     * Description: Get the FTP folder name to save the backup report
     * Modification Records:
     *   Park Li, 2015/12/23 15:23, Add a random value to crate FTP folder to avoid backup to same FTP folder when run parallel job.
     *   Park Li, 2015/12/28 11:04, Fixed issue for wrong FTP folder.
     */
    private String getFtpFolderName() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        ReportBean bean = getReportData();
        return bean.getSuite() + "_" + bean.getBrowser() + "_" + format.format(startDate) + "_rnd_" + rndNum;
    }

    private String getPdfFileName() {
        ReportBean bean = getReportData();
        return bean.getSuite() + "_" + bean.getBrowser() + "_" + bean.getExecutionTime().replaceAll(" +", "") + ".pdf";
    }

    /*
     * replace html code in message
     */
    private String fiterHtmlTag(String str) {
        if (str != null && !str.isEmpty()) {
            str = str.replaceAll("<", "");
            str = str.replaceAll(">", "");
            return str;
        }
        return "";
    }


    /*
     *
     * format html report
     * Modification Records:
     * Ocean.zhou, 2017/1/15, improve the UI of Report
     * Ocean.zhou, 2017/3/6, improve the Structure of the function
     */
    private String getReportContent(List<TestCaseResultBean> results) {
        StringBuffer content = new StringBuffer();
        File file = new File(Util.getClassPath() + "reports" + File.separator + "email-template.html");

        if(file.exists()) {
            try {
                content.append(FileUtils.readFileToString(file));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }else
            content.append(DefaultReportStyle.getDefaultStyle());

        ReportBean bean = getReportData();
        content.append(getDefaultBuildInfoTable(bean));
        content.append(DefaultReportStyle.createHeader("Summary"));
        String targetFTPFolderName = getFtpFolderName();
        String relativeSceenshotFilePath = bean.getTeam() + File.separator
                + targetFTPFolderName + File.separator + bean.getSuite() + File.separator + BaseCons.SCREENSHOT_FOLDER;
        String ftpScreenshotFilePath = "ftp://" + BaseCons.FTP_HOST + File.separator + relativeSceenshotFilePath;
        logger.info("screenshot:"+ftpScreenshotFilePath);
        addReportContentByStatus(results, content, ftpScreenshotFilePath);
        content.append(getDefaultSummaryTable(bean));
        content.append("<p>For detailed information, please refer the attached HTML file.</p>");
        content.append("<p><p></p></p>");
        content.append("<p>In case of any queries, please reach out to <strong>ADL Integration QA</strong> Team.</p>");
        return content.toString();
    }

    private void addReportContentByStatus(List<TestCaseResultBean> testCaseResults, StringBuffer content, String ftpScreenshotFilePath) {
        StringBuffer failedContent = new StringBuffer();
        StringBuffer passedContent = new StringBuffer();
        StringBuffer skippedContent = new StringBuffer();
        boolean passedFlag = false, failedFlag = false, skippedFlag = false;
        String errMessage = "";
        String screenLinkStr = "";
        DecimalFormat numFormat = new DecimalFormat("##0.000");
        for (TestCaseResultBean tcrb : testCaseResults) {
            String name = tcrb.getName();
            String owner = TestObjectManager.getOwner(name);
            switch (tcrb.getStatus()) {
                case PASSED://column: name, owner, costtime, retry status
                    flagPassed = true;
                    if(name.contains("PRODEnvironment")){
                        passedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("PROD environment available - application responding! ")));
                        statusMessage.add("PROD environment available - application responding!");
                    } else if(name.contains("INTQAEnvironmentV1")) {
                        passedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("ADL Integration QA-v1 environment available - application responding! ")));
                        statusMessage.add("ADL Integration QA-V1 environment available - application responding!");
                    }else if(name.contains("INTQAEnvironmentV2")) {
                        passedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("ADL Integration QA-V2 environment available - application responding! ")));
                        statusMessage.add("ADL Integration QA-V2 environment available - application responding!");
                    }else if(name.contains("STGEnvironment")) {
                        passedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("STG environment available - application responding! ")));
                        statusMessage.add("STG environment available - application responding!");
                    } else if(name.contains("UATEnvironment")) {
                        passedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("UAT environment available - application responding! ")));
                        statusMessage.add("UAT environment available - application responding!");
                    } else if(name.contains("QAEnvironment")) {
                        passedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("QA environment available  - application responding! ")));
                        statusMessage.add("QA environment available - application responding!");
                    }
                    if(!passedFlag) passedFlag = true;
                    break;
                case FAILED://name,className, owner, costtime, error message, screenshot
                    flagFailed = true;
                    if(name.contains("PRODEnvironment")){
                        failedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("PROD environment - application not responding! ")));
                        statusMessage.add("PROD environment - application not responding!");
                    } else if(name.contains("INTQAEnvironmentV1")) {
                        failedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("ADL Integration QA-V1 environment - application not responding! ")));
                        statusMessage.add("ADL Integration QA-V1 environment - application not responding!");
                    }else if(name.contains("INTQAEnvironmentV2")) {
                        failedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("ADL Integration QA-V2 environment - application not responding! ")));
                        statusMessage.add("ADL Integration QA-V2 environment - application not responding!");
                    }else if(name.contains("STGEnvironment")) {
                        failedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("STG environment - application not responding! ")));
                        statusMessage.add("STG environment - application not responding!");
                    } else if(name.contains("UATEnvironment")) {
                        failedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("UAT environment - application not responding! ")));
                        statusMessage.add("UAT environment - application not responding!");
                    } else if(name.contains("QAEnvironment")) {
                        failedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("QA environment - application not responding! ")));
                        statusMessage.add("QA environment - application not responding!");
                    }
                    /*errMessage = fiterHtmlTag(StringEscapeUtils.unescapeXml(tcrb.getErrorMessage()));
                    errMessage = errMessage.replaceAll("Timed out after \\d* seconds:", "");
                    errMessage = errMessage.replaceAll("Build info.*", "");
                    screenLinkStr = StringUtils.isEmpty(ftpScreenshotFilePath) ? "":getScreenshotLinksByCase(name, ftpScreenshotFilePath);
                    failedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn(name)
                            + DefaultReportStyle.createColumn(owner)
                            + DefaultReportStyle.createColumn(numFormat.format(tcrb.getCostTime()))
                            + DefaultReportStyle.createColumn(errMessage)
                            + DefaultReportStyle.createColumn(screenLinkStr)
                    ));*/
                    if(!failedFlag) failedFlag = true;
                    break;
                case SKIPPED://column: name, owner, costtime, retry status
                    flagSkipped = true;
                    if(name.contains("PRODEnvironment")){
                        skippedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("PROD environment - application not responding! ")));
                        statusMessage.add("PROD environment / application not responding!");
                    } else if(name.contains("INTQAEnvironmentV1")) {
                        skippedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("ADL Integration QA-V1 environment - application not responding! ")));
                        statusMessage.add("ADL Integration QA-V1 environment / application not responding!");
                    }else if(name.contains("INTQAEnvironmentV2")) {
                        skippedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("ADL Integration QA-V2 environment - application not responding! ")));
                        statusMessage.add("ADL Integration QA-V2 environment / application not responding!");
                    }else if(name.contains("STGEnvironment")) {
                        skippedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("STG environment - application not responding! ")));
                        statusMessage.add("STG environment / application not responding!");
                    } else if(name.contains("UATEnvironment")) {
                        skippedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("UAT environment - application not responding! ")));
                        statusMessage.add("UAT environment / application not responding!");
                    } else if(name.contains("QAEnvironment")) {
                        skippedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn("QA environment - application not responding! ")));
                        statusMessage.add("QA environment / application not responding!");
                    }
                    /*skippedContent.append(DefaultReportStyle.createRow(DefaultReportStyle.createColumn(name)
                            + DefaultReportStyle.createColumn(owner)
                            + DefaultReportStyle.createColumn(numFormat.format(tcrb.getCostTime()))
                            + DefaultReportStyle.createColumn(String.valueOf(tcrb.getRetryStatus()))
//                    ));*/
                    if(!skippedFlag) skippedFlag = true;
                    break;
            }
        }

        if (failedFlag) {
//            content.append(DefaultReportStyle.createHeader("Failed List" + getDefaultPDFLink()) );
            content.append(DefaultReportStyle.createTable(failedContent.toString(),"failed-table"));
        }

        if (passedFlag) {
//            content.append(DefaultReportStyle.createHeader("Passed List"));
            content.append(DefaultReportStyle.createTable(  passedContent.toString(), "passed-table"));
        }

        if (skippedFlag) {
//            content.append( DefaultReportStyle.createHeader("Skipped List"));
            content.append(DefaultReportStyle.createTable(skippedContent.toString(), "failed-table"));
        }
    }

    private boolean isSkippedCasev1(Element testcaseEl) {
        List<Element> list = XmlUtil.getElementList(testcaseEl, "skipped");
        return list.size() > 0;
    }

    private boolean isFailedCasev1(Element testcaseEl) {
        List<Element> list = XmlUtil.getElementList(testcaseEl, "failure");
        return list.size() > 0;
    }

    private boolean isDisableCasev1(Element testcaseEl) {
        List<Element> list = XmlUtil.getElementList(testcaseEl, "ignored");
        return list.size() > 0;
    }

    private boolean isSkippedCase(Element testcaseEl) {
		/*List<Element> list = XmlUtil.getElementList(testcaseEl, "skipped");
		return list.size() > 0;*/
        return StringUtils.equalsIgnoreCase(XmlUtil.getElementAttributeValue(testcaseEl, "status", ""),"SKIP");

    }

    private boolean isFailedCase(Element testcaseEl) {
		/*List<Element> list = XmlUtil.getElementList(testcaseEl, "failure");
		return list.size() > 0;*/
        return StringUtils.equalsIgnoreCase(XmlUtil.getElementAttributeValue(testcaseEl, "status", ""),"FAIL");
    }

    private boolean isPassedCase(Element testcaseEl) {
        return StringUtils.equalsIgnoreCase(XmlUtil.getElementAttributeValue(testcaseEl, "status", ""),"PASS");
    }

    /*
     * parse xml
     */
    private List<TestCaseResultBean> parseTestResults(Document allDoc) {
        List<TestCaseResultBean> results = new ArrayList<TestCaseResultBean>();
        //List<Element> testcaseList = XmlUtil.getElementList(allDoc, "/testsuites/testsuite/testcase");
        List<Element> testcaseList = XmlUtil.getElementList(allDoc, "//suite/test/class/test-method");
        String name="", signature = "", dataProvider="", className ="", errorMessage = "", stackInfo = "";
        float costTime = 0;
        Element failureEl, msgEl, stackEl;
        for (Element testcaseEl : testcaseList) {
            name = XmlUtil.getElementAttributeValue(testcaseEl, "name", "");
            signature = XmlUtil.getElementAttributeValue(testcaseEl, "signature", "");
            dataProvider =  XmlUtil.getElementAttributeValue(testcaseEl, "data-provider", "");
            if(StringUtils.isNotBlank(dataProvider))
                name = name + "#" + getProviderInfo(testcaseEl);
            //if(signature.contains("org.testng.ITestContext")) continue;
            if(testcaseEl.attribute("is-config") != null) continue;

            TestCaseResultBean tcrb = new TestCaseResultBean();
            ResultStatusEnum status = null;
            costTime = Float.parseFloat(XmlUtil.getElementAttributeValue(testcaseEl, "duration-ms", ""))/1000;

            className = XmlUtil.getElementAttributeValue(testcaseEl.getParent(), "name", "");
            errorMessage = null;
            stackInfo = null;

            if (isFailedCase(testcaseEl)) {
                // failed testcase
                status = ResultStatusEnum.FAILED;
			/*	Element failureEl = XmlUtil.getSingleElement(testcaseEl, "failure");
				errorMessage = XmlUtil.getElementAttributeValue(failureEl, "message", "");
				stackInfo = XmlUtil.getElementValue(failureEl, "");*/
                failureEl = XmlUtil.getSingleElement(testcaseEl, "exception");
                msgEl = XmlUtil.getSingleElement(failureEl, "message");
                errorMessage = XmlUtil.getElementCDataValue(msgEl, "");
                stackEl = XmlUtil.getSingleElement(failureEl, "full-stacktrace");
                stackInfo = XmlUtil.getElementCDataValue(stackEl, "");
            } else if (isSkippedCase(testcaseEl)) {
                // skipped testcase
                status = ResultStatusEnum.SKIPPED;
            } else if(isPassedCase(testcaseEl)){
                // passeded testcase
                status = ResultStatusEnum.PASSED;
            }

            tcrb.setName(name);
            tcrb.setStatus(status);
            tcrb.setCostTime(costTime);
            tcrb.setClassName(className);
            tcrb.setErrorMessage(errorMessage);
            tcrb.setStackInfo(stackInfo);
            tcrb.dataProviderFlag = StringUtils.isNotBlank(dataProvider);
            if(tcrb.dataProviderFlag){
                List<Element> paramEls = XmlUtil.getElementList(testcaseEl, "params/param/value");
                List<String> params = new ArrayList<>();
                for (Element paramel : paramEls) {
                    params.add(XmlUtil.getElementCDataValue(paramel, ""));
                }
                tcrb.params = params;
            }
            results.add(tcrb);

        }
        return filterData(results);
    }

    private String getProviderInfo(Element testcaseEl) {
        String res = "", paraVal = "";
        Element valEl;
        List<Element> paramList = XmlUtil.getElementList(testcaseEl, "params/param");
        for (Element paramEl : paramList) {
            valEl = XmlUtil.getSingleElement(paramEl, "value");
            paraVal = XmlUtil.getElementCDataValue(valEl, "");
            if(paraVal.contains("java")) continue;
            else
                res = res + "param["+ paraVal+"]";
        }
        return res;
    }

    private List<TestCaseResultBean> filterData(List<TestCaseResultBean> results) {
        //update cost time
        resetCostTime(results);

        List<TestCaseResultBean> resultList = new ArrayList<TestCaseResultBean>();

        HashSet<TestCaseResultBean> passedSet = DefaultStatisticUtil.getPassedResultSet(results);
        HashSet<TestCaseResultBean> failedSet = DefaultStatisticUtil.getFailedResultSet(results, passedSet);
        HashSet<TestCaseResultBean> skippedSet = DefaultStatisticUtil.getSkippedResultSet(results,passedSet,failedSet);

        if(passedSet.size() > 3) { //if there are more than 3 test cases, then enable it
            markTop3TestCasesByTime(DefaultStatisticUtil.sortSet(passedSet));
        }

        resultList.addAll(failedSet);
        resultList.addAll(passedSet);
        resultList.addAll(skippedSet);
        updateResultLst(failedSet, passedSet, skippedSet);
        return resultList;

    }

    private void updateResultLst(HashSet<TestCaseResultBean> failedList, HashSet<TestCaseResultBean> passedList,
                                 HashSet<TestCaseResultBean> skippedList) {
        ReportBean bean = this.getReportData();
        bean.setFailed(failedList.size());
        bean.setPassed(passedList.size());
        bean.setSkipped(skippedList.size());
        HashMap<String, String> stackInfo = null;
        HashMap<String, String> localScreenInfo = null;
        if (failedList.size() > 0) {
            stackInfo = new HashMap<>();
            localScreenInfo = new HashMap<>();
            for (TestCaseResultBean tcrb : failedList) {
                stackInfo.put(tcrb.getName(), tcrb.getStackInfo());
                localScreenInfo.put(tcrb.getName(), getLocalScreenshotFileByCase(tcrb.getName()));
            }
        }
        bean.setStackInfo(stackInfo);
        bean.setLocalScreenInfo(localScreenInfo);
    }
    //sorted list should be max first and min last
    private void markTop3TestCasesByTime(List<TestCaseResultBean> sortedList) {
        float costTime;
        int intCompare;
        int currentImportance = 99;
        costTime = -1;
        TestCaseResultBean tcrb;
        for (int i = 0;i < sortedList.size();i++){
            if(currentImportance < -1) break;
            tcrb = sortedList.get(i);
            if(i == 0) {
                tcrb.setTimeLevel(currentImportance);
            }
            else{
                intCompare = Float.compare(costTime, tcrb.getCostTime());
                if(intCompare != 0) {
                    if(currentImportance == 99) {
                        currentImportance = 0;
                    }
                    else break; //less than last time and current importance is 0 or -1
                }
                tcrb.setTimeLevel(currentImportance);
            }
            costTime = tcrb.getCostTime();
        }
    }

    /*
   go through all the bean and update its cost time as the total number according to the case name
    */
    private void resetCostTime(List<TestCaseResultBean> result){
        ArrayList<Integer> skipIndexLst = new ArrayList<Integer>();
        ArrayList<Integer> updatingIndexLst = new ArrayList<Integer>();
        String caseName = "";
        TestCaseResultBean bean;
        float costTime = 0;
        for(int i=0;i<result.size();i++){
            if(skipIndexLst.contains(i)) continue;
            updatingIndexLst.clear();
            costTime = 0;
            bean = result.get(i);
            caseName = bean.getName();
            costTime = bean.getCostTime();
            skipIndexLst.add(i);
            updatingIndexLst.add(i);
            for(int j= result.size()-1;j>i;j--){
                if(skipIndexLst.contains(j)) continue;
                bean = result.get(j);
                if(StringUtils.equals(caseName, bean.getName())){
                    skipIndexLst.add(j);
                    updatingIndexLst.add(j);
                    costTime = costTime + bean.getCostTime();
                }
            }
            if(!updatingIndexLst.isEmpty()){
                for(int index : updatingIndexLst){
                    result.get(index).setCostTime(costTime);
                }
            }
        }
    }

    String getLocalScreenshotFileByCase(String caseName){
        String screeshotBase = suite.getOutputDirectory() + File.separator + BaseCons.SCREENSHOT_FOLDER;

        String res = "";
        String fileName = "";
        long lastModified = 0;
        File[] files = new File(screeshotBase).listFiles();
        for (File screenFile:files) {
            fileName = screenFile.getName();
            if(StringUtils.containsIgnoreCase(fileName, caseName)){
                if(lastModified < screenFile.lastModified()) {
                    lastModified = screenFile.lastModified();
                    res = screenFile.getAbsolutePath();
                }
            }
        }

        return res;
    }

    /*
    according to the case name to get the path of the latest screenshot file.
     */
    private String getScreenshotLinksByCase(String caseName, String ftpFolderPath){
        String screeshotBase = suite.getOutputDirectory() + File.separator + BaseCons.SCREENSHOT_FOLDER;

        String res = "";
        String filePath = "";
        String fileName = "";
        long lastModified = 0;
        File[] files = new File(screeshotBase).listFiles();
        for (File screenFile:files) {
            fileName = screenFile.getName();

            if(StringUtils.containsIgnoreCase(fileName, caseName)){
                if(lastModified < screenFile.lastModified()) {
                    filePath = ftpFolderPath + File.separator + fileName;
                    lastModified = screenFile.lastModified();
                    res = DefaultReportStyle.createScreenshotLink(filePath);
                }
            }
        }
        return res;
    }


    /*
     * if folder is not exists, create the folder.
     */
    private void initDestPath() {
        File file = new File(suite.getOutputDirectory());
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private String getCurtimeStr() {
        DateFormat format = new SimpleDateFormat("yyyy/M/d HH:mm:ss");
        return format.format(Calendar.getInstance().getTime());
    }

    /*
    send the report
     */
    public void sendEmail(String content) {
        MailSenderInfo mailInfo = new MailSenderInfo();
        mailInfo.setMailServerHost(Environment.getEmailSenderInfo().getHost());
        mailInfo.setMailServerPort(Environment.getEmailSenderInfo().getPort());
        mailInfo.setValidate(true);
        mailInfo.setUserName(Environment.getEmailSenderInfo().getUserName());
        mailInfo.setPassword(Environment.getEmailSenderInfo().getPassword());
        mailInfo.setFromAddress(Environment.getEmailSenderInfo().getUserName());
        mailInfo.setFromName(Environment.getEmailSenderInfo().getFromName());
        mailInfo.setToAddress(Environment.getEmailRecipients());
        mailInfo.setSubject("<"+ Environment.getTeamName()+">" + " Environment Readiness Daily Check --- triggered by QA");


        logger.info("_content=" + content);
        mailInfo.setContent(content);

        try {
            EmailUtil.sendHtmlMail(mailInfo);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        sendTeamsNotification();

    }
    /*Modification Records:
     * Ocean.zhou, 2017/1/15, improve the UI of Report*/
    public String getContent() {
        ReportBean bean = getReportData();
        String targetFTPFolderName = getFtpFolderName();
        String ftpPath = "ftp://" + BaseCons.FTP_HOST + File.separator + bean.getTeam() + File.separator
                + targetFTPFolderName + File.separator + bean.getSuite();
        String relativeSceenshotFilePath = bean.getTeam() + File.separator
                + targetFTPFolderName + File.separator + bean.getSuite() + File.separator + BaseCons.SCREENSHOT_FOLDER;
        logger.info("ftpPath=" + ftpPath);

        StringBuffer result = new StringBuffer();
        String emailTemplateFilePath = suite.getOutputDirectory() + File.separator + bean.getSuite() + ".html";
        File file = new File(emailTemplateFilePath);

        try {
            String detailContent = FileUtils.readFileToString(file, Charset.defaultCharset());

            if (checkFTPFolderPath(relativeSceenshotFilePath)) detailContent.replace("Screenshot Link", "");

            result.append(detailContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
//		logger.info(result);
        return result.toString();
    }


    String getDefaultPDFLink(){
        ReportBean bean = getReportData();
        String targetFTPFolderName = getFtpFolderName();
        String ftpPath = "ftp://" + BaseCons.FTP_HOST + File.separator + bean.getTeam() + File.separator
                + targetFTPFolderName + File.separator + bean.getSuite();
        String pdfFilePath = ftpPath + File.separator + BaseCons.PDFREPORT_FOLDER + File.separator + getPdfFileName();
        return "   [<a href='" + pdfFilePath + "'>Details</a>]";
    }

    String getDefaultBuildInfoTable(ReportBean bean){
        String commonMatter = "<p><strong>Hello Team,</strong></p> <p><strong>Please find below the Summary of the Automated Check for Environment Readiness:</strong>";
        String res = commonMatter;
        res += "<p><strong>Build Information</strong></p>";
        res += "<p>Team: " + bean.getTeam() + "</p>";
        res += "<p>Environment Verified: " + bean.getEnvironmentVerified() + "</p>";
        res += "<p>Total Script Execution Time: " + bean.getExecutionTime() + "</p>";
        res += "<p>Browser: " + bean.getBrowser() + "</p>";
        res += "<p>Timestamp when the check was done: " + bean.getTimeStamp() + "</p>";
        res += "<p>URLs used for the request are as follows: </p>";
        res += "<p>PROD: https://www.adviserlogic.com/Default.aspx </p>";
        res += "<p>QA: https://qa.adviserlogic.com/Default.aspx </p>";
        //res += "<p>ADL Integration QA v1: https://adviserlogicintsquad-qa.adviserlogic.com/v1/AdviserLogic/Login/Login.aspx </p>";
        res += "<p>ADL Integration QA v2: https://adviserlogicintsquad-qa.adviserlogic.com/Login/Login.aspx </p>";
        res += "<p></P><p></P>";

        return DefaultReportStyle.createHeader("") + DefaultReportStyle.createTable( new String[]{},
                DefaultReportStyle.createRow(DefaultReportStyle.createColumn(String.valueOf(res))), "build");
    }
    /*Modification Records:
     * Ocean.zhou, 2017/1/15, improve the UI of Report*/
    String getDefaultSummaryTable(ReportBean bean){
        int passed = bean.getPassed();
        int failed = bean.getFailed();
        int skipped = bean.getSkipped();
        int totalCases = passed + failed + skipped;
        double passedPercent = ((double)passed) /totalCases;
        double failedPercent = ((double)failed) /totalCases;
        double skippedPercent = ((double)skipped) / totalCases;
        DecimalFormat dFormat = new DecimalFormat("#.#%");
        return DefaultReportStyle.createTable( new String[]{"Total", "Passed", "Failed", "Skipped"},
                DefaultReportStyle.createRow( DefaultReportStyle.createColumn(String.valueOf(totalCases))
                        + DefaultReportStyle.createColumn(String.valueOf(passed) + "[" + dFormat.format(passedPercent) + "]")
                        + DefaultReportStyle.createColumn(String.valueOf(failed) + "[" + dFormat.format(failedPercent) + "]")
                        + DefaultReportStyle.createColumn(String.valueOf(skipped) + "[" + dFormat.format(skippedPercent) + "]")
                ), "summary");
    }

    /**
     * Author: pli3
     * Created Date: 2015.12.28 21:00:32
     * Description: Check if the relative path of FTP folder exist
     * Modification Records:
     * eg.Park Li, 2015/10/10 15:23, Add new parameter "rowCount".
     */
    private static boolean checkFTPFolderPath(String path) {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(BaseCons.FTP_HOST, BaseCons.FTP_PORT);
            ftp.login(BaseCons.FTP_USER_NAME, BaseCons.FTP_USER_PWD);

            ftp.changeWorkingDirectory(path);
            if (ftp.getReplyCode() == 550) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    private static String GetJobInf() {
        String jobInf = System.getProperty(BaseCons.PROPERTY_JOBINF_KEY);
//		String jobInf = "{'JobInf':{'JobName':'HG/Mercury_Automation_BranchDebug_Tests','BuildId':'#821','BuildURL':'https://jenkins.morningstar.com/job/HG/job/Mercury_Automation_BranchDebug_Tests/821/'}}";
        if(jobInf != null) {
            JSONObject jsonObj = JSONObject.parseObject(JSONObject.parseObject(jobInf).getString("JobInf"));

            @SuppressWarnings("unchecked")
            Iterator<String> keys = jsonObj.keySet().iterator();
            String temp = "";

            while(keys.hasNext()){
                String key = keys.next();
                temp += "<p>" + key + " : " + jsonObj.get(key) + "</p>";
            }

            return temp;
        }else{
            return null;
        }
    }

    /**
     * Author: pramod karande
     * Created Date: 2020.11.03 20:02:32
     * Description: Send notification on teams.
     */
    public void sendTeamsNotification(){
        String color ="";

        if(flagPassed && !flagFailed && !flagSkipped){
            color = "#00FF00";  //Green
        }else if(flagPassed && (flagFailed || flagSkipped)){
            color = "#0000FF";  //Blue
        }else{
            color = "#FF0000";  //Red
        }

        RequestSpecification teamsNotify = RestAssured.given();
        teamsNotify.baseUri("https://outlook.office.com/webhook/")
                .body("{\n" +
                        "    \"@type\": \"MessageCard\",\n" +
                        "    \"@context\": \"https://schema.org/extensions\",\n" +
                        "    \"themeColor\": \""+color+"\",\n" +
                        "    \"summary\": \"Test Automation Execution Result\",\n" +
                        "    \"sections\": [\n" +
                        "        {\n" +
                        "            \"activityTitle\": \"** Environment Readiness Daily Check **\",\n" +
                        "            \"facts\": [\n" +
                        "                {\n" +
                        "                    \"name\": \"Team\",\n" +
                        "                    \"value\": \""+Environment.getTeamName()+"\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"Environment Verified\",\n" +
                        "                    \"value\": \""+Environment.getEnvironmentVerified()+"\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"Total Script Execution Time\",\n" +
                        "                    \"value\": \""+getExcutiontimeStr()+"\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"Browser\",\n" +
                        "                    \"value\": \""+Environment.getDefaultBrowser()+"\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"Executed at\",\n" +
                        "                    \"value\": \""+getTimeStamp()+"\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"Summary\",\n" +
                        "                    \"value\": \""+statusMessage+"\"\n" +
                        "                },\n" +
                        "            ],\n" +
                        "            \"markdown\": true\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        Response response = teamsNotify.post("6444e293-b129-4407-9e1a-41c097eac8e4@d2d302fb-0aef-4773-94a5-7950c6f64a35/IncomingWebhook/bab197af33874b86a7b0fa60dac1f325/5d4cc289-d57c-4fa1-8789-fe7a316c82ba");
        Assert.assertEquals(response.getStatusCode(),200);
    }
    /*
     * just write for test in local machine
     */
    public static void main(String[] args) {
        // String path1 =
        // "Mercury\\Debug_Park_FTP_Issue_firefox_2015-12-28-17-50-57_rnd_244\\Debug_Park_FTP_Issue\\screenshot";
        // String path2 =
        // "Mercury\\Debug_Park_FTP_Issue_firefox_2015-12-28-17-50-57_rnd_244\\Debug_Park_FTP_Issue\\";
        // String path3 =
        // "Mercury\\Debug_Lzhang7-15668_chrome_2015-12-27-23-59-10_rnd_262\\Debug_Lzhang7-15668\\screenshot\\";
        // String path4 =
        // "Mercury\\Debug_Lzhang7-15668_chrome_2015-12-27-23-59-10_rnd_262\\Debug1_Lzhang7-15668\\11";
        String path5 = "C:\\Users\\ozhou\\Desktop\\testng-results.xml";

        // System.out.println(checkFTPFolderPath(path1));
        // System.out.println(checkFTPFolderPath(path2));
        // System.out.println(checkFTPFolderPath(path3));
        // System.out.println(checkFTPFolderPath(path4));
        Document allDoc = null;
        try {
            File file = new File(path5);
            SAXReader saxReader = new SAXReader();
            allDoc = saxReader.read(file);
            // logger.info(doc.getRootElement().asXML());
            // rootEl.add(doc.getRootElement().createCopy());
        } catch (DocumentException e) {
            e.printStackTrace();
            logger.error(e);
        }
        // }
        DefaultReportSolution rs = new DefaultReportSolution();
        if (allDoc != null) {
            List<TestCaseResultBean> results = rs.parseTestResults(allDoc);
            // clean up the list and combine passed,failed and skipped test
            // cases
            String reportContent = rs.getReportContent(results);
        }
        /*
         * System.out.println(GetJobInf());
         * System.out.println((double)(Math.round(3*100/(3+4+0))) + "%");
         * System.out.println(String.format("%10.2f%%",
         * (double)(3*100)/(3+4+2)));
         */
    }
}

