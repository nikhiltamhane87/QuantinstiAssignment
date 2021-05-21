package com.morningstar.automation.base.core.beans;

import java.util.HashMap;

/**
 *
 * @Description:
 * @author june.you@morningstar.com
 * @date Nov 19, 2013 5:18:22 AM
 */
public class ReportBean {
    private String team;
    private String environmentVerified;
    private String timeStamp;
    private String environment;
    private String url;
    private String executionTime;
    private String browser;
    private String suite;
    private int passed;
    private int failed;
    private int skipped;
    private HashMap<String,String> stackInfo;
    private HashMap<String,String> localScreenshotInfo;

    public String   getTeam() {
        return team;
    }
    public String getEnvironmentVerified() {
        return environmentVerified;
    }
    public void setTeam(String team) {
        this.team = team;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }


    public void setEnvironmentVerified(String environmentVerified) {this.environmentVerified = environmentVerified;}
    public String getEnvironment() {
        return environment;
    }
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getBrowser() {
        return browser;
    }
    public void setBrowser(String browser) {
        this.browser = browser;
    }
    public String getExecutionTime() {
        return executionTime;
    }
    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }
    public String getSuite() {
        return suite;
    }
    public void setSuite(String suite) {
        this.suite = suite;
    }
    public int getPassed() {
        return passed;
    }
    public void setPassed(int passed) {
        this.passed = passed;
    }
    public int getFailed() {
        return failed;
    }
    public void setFailed(int failed) {
        this.failed = failed;
    }
    public int getSkipped() {
        return skipped;
    }
    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }
    public HashMap<String, String> getStackInfo() {return stackInfo;}
    public void setStackInfo(HashMap<String,String> info){
        stackInfo = info;}
    public HashMap<String, String> getLocalScreenInfo() {return localScreenshotInfo;}
    public void setLocalScreenInfo(HashMap<String,String> info){ localScreenshotInfo = info;}
}

