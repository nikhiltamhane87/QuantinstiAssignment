package com.morningstar.automation.base.core.configurations;

import com.alibaba.fastjson.JSONObject;
import com.morningstar.automation.base.cons.BaseCons;
import com.morningstar.automation.base.core.beans.*;
import com.morningstar.automation.base.core.utils.JsonUtil;
import com.morningstar.automation.base.core.utils.Logger;
import com.morningstar.automation.base.core.utils.Util;
import com.morningstar.automation.base.core.utils.XmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.*;

public class Environment {

    private static String mode;

    private static String defaultBrowser;

    private static String defaultBrowserVersion;

    private static String defaultPlatform;

    private static String defaultEnv;

    private static boolean useGrid;

    private static String teamName;

    private static String environmentVerified;
    
    private static boolean sendResultToTestRail;

    private static String testRailUserName;

    private static String testRailPW;

    private static List<EnvironmentBean> environments = new ArrayList<EnvironmentBean>();

    private static HashMap<String, String> teamSetting = new HashMap<String, String>();

    private static String filePath = Util.getClassPath() + "config" + File.separator + "environment.xml";

    private static final Logger logger = Logger.getLogger(Environment.class);

    static {
        parse();
    }

    private static void parse() {
        try {
            logger.debug("start parse the file");;
            File file = new File(filePath);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(file);
            logger.debug("to parse debug options");
            parseDebugOptions(document);
            logger.debug("to parse team options");
            parseTeamOptions(document);
            logger.debug("to parse test rail options");
            parseTestRailUserInfo(document);
            List<Element> elList = XmlUtil.getElementList(document, "/root/environment");
            for (Element el : elList) {
                parseEnvironment(el);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    private static void parseTeamOptions(Document document) {
        Element elRoot = XmlUtil.getSingleElement(document, "/root/teamSetting");
        if (elRoot == null) {
            teamName = XmlUtil.getElementValue(XmlUtil.getSingleElement(document, "/root/teamName"), "");
            environmentVerified = XmlUtil.getElementValue(XmlUtil.getSingleElement(document, "/root/environmentVerified"), "");
        } else {
            teamName = XmlUtil.getElementValue(XmlUtil.getSingleElement(elRoot, "teamName"), "");
            environmentVerified = XmlUtil.getElementValue(XmlUtil.getSingleElement(elRoot, "environmentVerified"), "");
            for(Iterator it = elRoot.elementIterator();it.hasNext();) {
                Element element = (Element)it.next();
                teamSetting.put(element.getName(), element.getText());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void parseTestRailUserInfo(Document document) {
        Element elTestRailUser = XmlUtil.getSingleElement(document, "/root/testRailUserName");
        Element elTestRailPW = XmlUtil.getSingleElement(document, "/root/testRailPW");
        if (elTestRailUser != null && elTestRailPW != null) {
            setTestRailUserName(XmlUtil.getElementValue(elTestRailUser, ""));
            setTestRailPW(XmlUtil.getElementValue(elTestRailPW, ""));
        } else {
            setTestRailUserName(null);
            setTestRailPW(null);
        }
    }


    private static void parseDebugOptions(Document document) {
        Element root = document.getRootElement();
        mode = XmlUtil.getElementAttributeValue(root, "mode", "");
        defaultBrowser = XmlUtil.getElementAttributeValue(root, "defaultBrowser", "");
        defaultBrowserVersion = XmlUtil.getElementAttributeValue(root, "defaultBrowserVersion", "");
        defaultPlatform = XmlUtil.getElementAttributeValue(root, "defaultPlatform", "WINDOWS");
        defaultEnv = XmlUtil.getElementAttributeValue(root, "defaultEnv", "");
        useGrid = Boolean.parseBoolean(XmlUtil.getElementAttributeValue(root, "useGrid", ""));
        sendResultToTestRail = Boolean.parseBoolean(XmlUtil.getElementAttributeValue(root, "sendResultToTestRail", ""));
    }

    private static void parseEnvironment(Element el) {
        EnvironmentBean env = new EnvironmentBean();

        String type = XmlUtil.getElementAttributeValue(el, "type", "");
        String homePageUrl = XmlUtil.getElementValue(XmlUtil.getSingleElement(el, "homePageUrl"), "");
        String sleepInMillis = XmlUtil.getElementValue(XmlUtil.getSingleElement(el, "sleepInMillis"), "240");
        String timeOutInSeconds = XmlUtil.getElementValue(XmlUtil.getSingleElement(el, "timeOutInSeconds"), "60");

        Element serverEl = XmlUtil.getSingleElement(el, "server");
        Element versionModelEl = XmlUtil.getSingleElement(el, "versionModel");
        Element profileEl = XmlUtil.getSingleElement(el, "profile");

        List<Element> userElList = XmlUtil.getElementList(el, "users/user");

        ServerBean server = parseServer(serverEl);

        if (versionModelEl != null) {
            VersionModelBean versionModel = parseVersionModel(versionModelEl);
            env.setVersionModel(versionModel);
        }

        ProfileBean profile = parseProfile(profileEl);
        List<UserBean> users = parseUsers(userElList);

        Element retryCountEl = XmlUtil.getSingleElement(el, "retryCount");
        int retryCount = Integer.parseInt(XmlUtil.getElementValue(retryCountEl, "0").trim());

        Element reportOptionsEl = (Element) el.selectSingleNode("report");
        ReportOptionsBean reportOptions = parseReportOptions(reportOptionsEl);

        env.setType(type);
        env.setHomePageUrl(homePageUrl);
        env.setServer(server);
        env.setProfile(profile);
        env.setUsers(users);
        env.setSleepInMillis(Long.parseLong(sleepInMillis));
        env.setTimeOutInSeconds(Long.parseLong(timeOutInSeconds));
        env.setRetryCount(retryCount);
        env.setReportOptions(reportOptions);

        environments.add(env);
    }

    private static ReportOptionsBean parseReportOptions(Element reportOptionsEl) {
        ReportOptionsBean bean = new ReportOptionsBean();
        List<Element> emailRecipientsElList = XmlUtil.getElementList(reportOptionsEl, "emailRecipients/recipient");
        Element emailSenderEl = XmlUtil.getSingleElement(reportOptionsEl, "emailSender");
        EmailSenderBean senderInfo = parseSenderInfo(emailSenderEl);
        List<String> emailRecipients = parseRecipients(emailRecipientsElList);
        bean.setEmailRecipients(emailRecipients);
        bean.setSenderInfo(senderInfo);
        return bean;
    }

    private static EmailSenderBean parseSenderInfo(Element emailSenderEl) {
        EmailSenderBean bean = new EmailSenderBean();
        String userName = XmlUtil.getElementValue(XmlUtil.getSingleElement(emailSenderEl, "userName"), "");
        String password = XmlUtil.getElementValue(XmlUtil.getSingleElement(emailSenderEl, "password"), "");
        String fromName = XmlUtil.getElementValue(XmlUtil.getSingleElement(emailSenderEl, "fromName"), "");
        String host = XmlUtil.getElementValue(XmlUtil.getSingleElement(emailSenderEl, "host"), "");
        String port = XmlUtil.getElementValue(XmlUtil.getSingleElement(emailSenderEl, "port"), "");
        bean.setUserName(userName);
        bean.setPassword(password);
        bean.setFromName(fromName);
        bean.setHost(host);
        bean.setPort(port);
        return bean;
    }

    private static List<String> parseRecipients(List<Element> recipientElList) {
        List<String> result = new ArrayList<String>();
        for (Element el : recipientElList) {
            String email = XmlUtil.getElementValue(el, "");
            result.add(email);
        }
        return result;
    }

    private static List<UserBean> parseUsers(List<Element> userElList) {
        List<UserBean> list = new ArrayList<UserBean>();
        for (Element el : userElList) {
            String name = XmlUtil.getElementAttributeValue(el, "name", "");
            String pwd = XmlUtil.getElementAttributeValue(el, "pwd", "");
            String type = XmlUtil.getElementAttributeValue(el, "type", "");
            UserBean user = new UserBean();
            user.setName(name);
            user.setPwd(pwd);
            user.setType(type);
            list.add(user);
        }
        return list;
    }

    private static ServerBean parseServer(Element serverEl) {
        ServerBean server = new ServerBean();

        Element appiumHubEl = XmlUtil.getSingleElement(serverEl, "appiumHub");
        String appiumHub = (appiumHubEl==null? "": XmlUtil.getElementValue(appiumHubEl, ""));

        Element CIHubEl = XmlUtil.getSingleElement(serverEl, "CIHub");
        String CIHub = (CIHubEl==null?"":XmlUtil.getElementValue(CIHubEl, ""));

        Element debugHubEl = XmlUtil.getSingleElement(serverEl, "debugHub");
        String debugHub = (debugHubEl==null?"":XmlUtil.getElementValue(debugHubEl, ""));

        List<Element> nodeElList = XmlUtil.getElementList(serverEl, "node");
        List<String> nodes = new ArrayList<String>();
        for (Element el : nodeElList) {
            nodes.add(XmlUtil.getElementValue(el, ""));
        }

        Element ftpHostEl = XmlUtil.getSingleElement(serverEl, "FTPHost");
        String ftpHost = XmlUtil.getElementValue(ftpHostEl, "szmercurytest1");

        Element ftpPortEl = XmlUtil.getSingleElement(serverEl, "FTPPort");
        String ftpPort = XmlUtil.getElementValue(ftpPortEl, "21");

        server.setappiumHub(appiumHub);
        server.setCIHub(CIHub);
        server.setDebugHub(debugHub);
        server.setFTPHost(ftpHost);
        server.setFTPPort(ftpPort);

        return server;
    }

    private static ProfileBean parseProfile(Element profileEl) {
        ProfileBean profile = new ProfileBean();

        Element firefoxEl = XmlUtil.getSingleElement(profileEl, "firefox");
        String firefox = (firefoxEl==null?"":XmlUtil.getElementValue(firefoxEl, ""));

        Element chromeEl = XmlUtil.getSingleElement(profileEl, "chrome");
        String chrome = (chromeEl == null?"":XmlUtil.getElementValue(chromeEl, ""));

        Element safariEl = XmlUtil.getSingleElement(profileEl, "safari");
        String safari = (safariEl==null?"":XmlUtil.getElementValue(safariEl, ""));

        Element androidEl = XmlUtil.getSingleElement(profileEl, "android");
        String android = (androidEl==null?"":XmlUtil.getElementValue(androidEl, ""));

        Element iosEl = XmlUtil.getSingleElement(profileEl, "ios");
        String ios = (iosEl==null?"":XmlUtil.getElementValue(iosEl, ""));

        profile.setFirefoxProfile(firefox);
        profile.setChromeProfile(chrome);
        profile.setSafariProfile(safari);
        profile.setAndroidProfile(android);
        profile.setIosProfile(ios);
        return profile;
    }

    private static VersionModelBean parseVersionModel(Element versionModelEl) {
        VersionModelBean versionModel = new VersionModelBean();

        @SuppressWarnings("rawtypes")
        Iterator iter = versionModelEl.elementIterator();
        while (iter.hasNext()) {
            Element element = (Element) iter.next();
            versionModel.set(element.getName(), element.getText());
        }

        return versionModel;
    }

    public static EnvironmentBean getEnvironmentBean() {
        EnvironmentBean envBean = null;
        String env = Util.getEnvStr();
        //logger.debug("===current use environment:" + env);
        for (EnvironmentBean eb : environments) {
            String type = eb.getType();
            if (StringUtils.equalsIgnoreCase(type,env)) {
                envBean = eb;
                break;
            }
        }

        String environmentStr = System.getProperty(BaseCons.PROPERTY_CONFIG_KEY);
        if (environmentStr != null) {
            //	logger.info("===***New configuration key-set string: " + environmentStr);
            JSONObject jsonA = envBean.toJSON();
            JSONObject jsonB = JSONObject.parseObject(environmentStr);
            jsonA = JsonUtil.extend(jsonA, jsonB);
            envBean = EnvironmentBean.fromJSON(jsonA);
        }

        return envBean;
    }

    // pli3 - get default Environment bean by customize environment
    public static EnvironmentBean getEnvironmentBean(String env) {
        EnvironmentBean envBean = null;
        for (EnvironmentBean eb : environments) {
            String type = eb.getType();
            if (StringUtils.equalsIgnoreCase(type,env)) {
                envBean = eb;
                break;
            }
        }

        return envBean;
    }

    public static String getHub() {
        String hub;
        if (Util.getMode().equals(BaseCons.DEBUG_MODE)) {
            hub = getEnvironmentBean().getServer().getDebugHub();
        } else {
            String newHub = System.getProperty(BaseCons.PROPERTY_CIHUB_KEY);
            if(newHub != null) {
                hub = newHub;
            }else {
                hub = getEnvironmentBean().getServer().getCIHub();
            }
        }
        return hub;
    }

    public static String getFTPHost() {
        return getEnvironmentBean().getServer().getFTPHost();
    }

    public static int getFTPPort() {
        return Integer.parseInt(getEnvironmentBean().getServer().getFTPPort());
    }

    public static String getHomePageUrl() {
        return getEnvironmentBean().getHomePageUrl();
    }

    public static long getSleepInMillis() {
        return getEnvironmentBean().getSleepInMillis();
    }

    public static long getTimeOutInSeconds() {
        return getEnvironmentBean().getTimeOutInSeconds();
    }

    public static List<UserBean> getAllUser() {
        return getEnvironmentBean().getUsers();
    }

    public static String getMode() {
        return mode;
    }

    public static String getDefaultBrowser() {
        return defaultBrowser;
    }

    public static String getDefaultEnv() {
        return defaultEnv;
    }

    public static boolean isUseGrid() {
        return useGrid;
    }

    public static List<String> getEmailRecipients() {
        String newEmailRecipients = System.getProperty(BaseCons.PROPERTY_RECIPIENTS_KEY);
        if (newEmailRecipients != null && !newEmailRecipients.equals("")) {
            List<String> list = new LinkedList<String>();
            String[] temp = newEmailRecipients.split(",");
            for (String s : temp) {
                list.add(s.trim());
            }
            return list;
        } else {
            return getEnvironmentBean().getReportOptions().getEmailRecipients();
        }
    }

    public static EmailSenderBean getEmailSenderInfo() {
        return getEnvironmentBean().getReportOptions().getSenderInfo();
    }

    public static int getRetryCout() {
        return getEnvironmentBean().getRetryCount();
    }

    public static String getTeamName() {
        return teamName;
    }

    public static String getEnvironmentVerified() {
        return environmentVerified;
    }

    public static void setTeamName(String teamName) {
        Environment.teamName = teamName;
    }

    public static void setEnvironmentVerified(String environmentVerified) {
        Environment.environmentVerified = environmentVerified; }

    public static HashMap<String, String> getTeamSetting() {
        return teamSetting;
    }

    public static String getDefaultPlatform() {
        return defaultPlatform;
    }

    public static void setDefaultPlatform(String defaultPlatform) {
        Environment.defaultPlatform = defaultPlatform;
    }

    public static String getDefaultBrowserVersion() {
        return defaultBrowserVersion;
    }

    public static void setDefaultBrowserVersion(String defaultBrowserVersion) {
        Environment.defaultBrowserVersion = defaultBrowserVersion;
    }

    public static String getTestRailUserName() {
        return testRailUserName;
    }

    public static void setTestRailUserName(String testRailUserName) {
        Environment.testRailUserName = testRailUserName;
    }

    public static String getTestRailPW() {
        return testRailPW;
    }

    public static void setTestRailPW(String testRailPW) {
        Environment.testRailPW = testRailPW;
    }

    public static boolean isSendResultToTestRail() {
        return sendResultToTestRail;
    }

    // public static void main(String[] args) {
    // String str =
    // "{\"vesionModel\":{\"qa\":\"4.4\",\"staging\":\"4.3.1\",\"uat\":\"4.1.1\",\"live\":\"4.2.19\"}}";
    // JSONObject jsonA = getEnvironmentBean().toJSON();
    // JSONObject jsonB = JSONObject.fromObject(str);
    // System.out.println(jsonA);
    // jsonA = JsonUtil.extend(jsonA, jsonB);
    // System.out.println(jsonA);
    // }

}

