package com.morningstar.automation.base.core.utils;

import com.morningstar.automation.base.cons.BaseCons;
import com.morningstar.automation.base.core.beans.BrowserTypeEnum;
import com.morningstar.automation.base.core.configurations.Environment;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("deprecation")
public class Util {

	private static final Logger logger = Logger.getLogger(Util.class);

	static public int getRandInt(int t) {
		int tmp = RandomUtils.nextInt(0,4);
		if (tmp == t) {
			tmp = getRandInt(tmp);
		}
		return tmp;
	}

	public static String getClassPath() {
		String result = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		try {
			result = URLDecoder.decode(result, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<String> getNextMonthString(int offSet) {
		List<String> nextMonth = new ArrayList<String>();
		Date date = new Date();
		date = DateUtils.addMonths(date, -1);
		for (int i = 0; i < offSet; i++) {
			date = DateUtils.addMonths(date, 1);
			String dateString = date.toString();
			nextMonth.add(dateString.substring(4, 7));
		}
		return nextMonth;
	}

	public static int getCurrentYear() {
		Calendar calendar = new GregorianCalendar();
		return calendar.get(Calendar.YEAR);
	}

	public static String getMode() {
		String result = System.getProperty(BaseCons.PROPERTY_MODE_KEY);
		if (result == null) {
			result = Environment.getMode();
		}
		//logger.info("Util===mode: " + result);
		return result;
	}

	public static Boolean isUseGrid() {
		boolean result = true;
		String useGrid = System.getProperty(BaseCons.PROPERTY_USEGRID_KEY);
		if (useGrid == null) {
			result = Environment.isUseGrid();
		} else {
			result = Boolean.parseBoolean(useGrid);
		}
		//logger.info("Util===useGrid: " + result);
		return result;
	}
	
	public static Boolean isSendResultToTestRail() {
		boolean result = true;
		String useGrid = System.getProperty(BaseCons.PROPERTY_TESTRAIL_KEY);
		if (useGrid == null) {
			result = Environment.isSendResultToTestRail();
		} else {
			result = Boolean.parseBoolean(useGrid);
		}
		//logger.info("Util===useGrid: " + result);
		return result;
	}

	public static String getEnvStr() {
		String env = System.getProperty(BaseCons.PROPERTY_ENV_KEY);
		if (env == null) {
			env = Environment.getDefaultEnv();
		}
		return env;
	}

	public static String getBrowserType() {
		String result = System.getProperty(BaseCons.PROPERTY_BROWSER_TYPE_KEY);
		if (result == null) {
			result = Environment.getDefaultBrowser();
		}
		//logger.info("Util===BrowserType: " + result);
		return result;
	}

	public static String getBrowserVersion() {
		String result = System.getProperty(BaseCons.PROPERTY_BROWSER_VERSION_KEY);
		if (result == null) {
			result = Environment.getDefaultBrowserVersion();
		}
		//logger.info("Util===BrowserVersion: " + result);
		return result;
	}

	public static String getTeamName() {
		String result = Environment.getTeamName();
		return result;
	}
	
	/**
	 * Get the map of all team settings, you can get each value of team settings by key
	 *
	 * @author pli3
	 * @since 2016.6.1 7:51:10
	 *
	 * @return the map of all team settings
	 */
	public static HashMap<String, String> getTeamSettings(){
		return Environment.getTeamSetting();
	}

	public static String getPlatform() {
		String result = System.getProperty(BaseCons.PROPERTY_PLATFORM_KEY);
		if (result == null) {
			result = Environment.getDefaultPlatform();
		}
		//logger.info("Util===Platform: " + result);
		return result;
	}

	public static boolean isNASDAQMarketOpen() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("America/NewYork"));
		logger.info(cal.getTime());
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w >= 1 && w <= 5) {
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);
			int second = cal.get(Calendar.SECOND);
			int curSecond = hour * 60 * 60 + minute * 60 + second;
			int openSecond = 9 * 60 * 60 + 30 * 60;
			int closeSecond = 16 * 60 * 60;
			return (curSecond > openSecond && curSecond < closeSecond);
		}
		return false;
	}

	public static boolean isChineseMarketOpen() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w >= 1 && w <= 5) {
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);
			int second = cal.get(Calendar.SECOND);
			int curSecond = hour * 60 * 60 + minute * 60 + second;
			int amOpenSecond = 9 * 60 * 60 + 30 * 60;
			int amCloseSecond = 11 * 60 * 60 + 30 * 60;

			int pmOpenSecond = 13 * 60 * 60;
			int pmCloseSecond = 15 * 60 * 60;
			return (curSecond > amOpenSecond && curSecond < amCloseSecond)
					|| (curSecond > pmOpenSecond && curSecond < pmCloseSecond);
		}
		return false;
	}

	public static String getDriverPath(String browserType) {
		String result = "";
		BrowserTypeEnum browserTypeEnum = BrowserTypeEnum.valueOf(browserType.toUpperCase());
		switch (browserTypeEnum) {
			case IE:
				if (Platform.getCurrent().is(Platform.WINDOWS)) {
					result = Util.getClassPath() + "driver" + File.separator + "IEDriverServer.exe";
					logger.info("OS=Windows  browserType=" + browserType);
				}

				break;
			case CHROME:
				if (Platform.getCurrent().is(Platform.WINDOWS)) {
					result = Util.getClassPath() + "driver" + File.separator + "chromedriver.exe";
					logger.info("OS=Windows  browserType=" + browserType);
				} else if (Platform.getCurrent().is(Platform.MAC)) {
					//result = Util.getClassPath() + "driver" + File.separator + "chromedriver";
					result = System.getProperty("user.dir") + "/src/test/resources/driver/chromedriver";
					logger.info("OS=Mac browserType=" + browserType);
				}
				else if(Platform.getCurrent().is(Platform.LINUX))
				{
					result = System.getProperty("user.dir") + "/src/test/resources/driver/chromedriverLinux";
					logger.info("OS=Linux browserType=" + browserType);
				}else {
					logger.info("Undefined Platform");
				}
				break;
			case EDGE:
				if (Platform.getCurrent().is(Platform.WINDOWS)) {
					result = Util.getClassPath() + "driver" + File.separator + "MicrosoftWebDriver.exe";
					logger.info("OS=Windows  browserType=" + browserType);
				}
				break;
			case FIREFOX:
				if (Platform.getCurrent().is(Platform.WINDOWS)) {
					result = Util.getClassPath() + "driver" + File.separator + "geckodriver.exe";
					logger.info("OS=Windows  browserType=" + browserType);
				}
				break;
			case SAFARI://nothing
			default:
				break;
		}
		return result;
	}

	/**
	 * compare 2 version and give the compare result
	 *
	 * @author pli3
	 * @since 2015.10.23 11:33:38
	 * @throws null
	 *
	 * @param sourceVersion String, the version of source, e.g. 1.2.11
	 * @param targetVersion String, the version of target, e.g. 1.3.4
	 * @return int, if positive it means source is bigger than target version
	 */
	public static int compareVersion(String sourceVersion, String targetVersion) {
		String[] targetArray = targetVersion.split("\\.");
		String[] sourceArray = sourceVersion.split("\\.");

		int idx = 0;
		int minLength = Math.min(sourceArray.length, targetArray.length);
		int diff = 0;
		while (idx < minLength && (diff = sourceArray[idx].length() - targetArray[idx].length()) == 0
				&& (diff = sourceArray[idx].compareTo(targetArray[idx])) == 0) {
			++idx;
		}

		diff = (diff != 0) ? diff : sourceArray.length - targetArray.length;

		return diff;
	}

	/**
	 * Check if the feature has been deployed, it'll return true if feature has been deployed
	 *
	 * @author pli3
	 * @since 2015.10.23 11:28:54
	 * @throws null
	 *
	 * @param modelName String, the name of your model inside versionModel bean
	 * @param targetVersion String, the version of target, e.g. 1.3.4
	 * @return boolean, it'll return true if feature has been deployed
	 */
	public static boolean isFeatureDeployed(String modelName, String targetVersion) {
		String sourceVersion = Environment.getEnvironmentBean().getVersionModel().get(modelName);
		int result = compareVersion(sourceVersion, targetVersion);

		return result >= 0 ? true : false;
	}

	/**
	 * check if the bug has been fixed by comparing current version with bugVersion and fixedVersion
	 *
	 * @author pli3
	 * @since 2015.10.23 11:28:50
	 * @throws null
	 *
	 * @param id String, the bug ID
	 * @return boolean, it'll return True if all the bug have been closed and (current version is higher/equal to fixVersion or lower than bugVersion).
	 */
	public static boolean isBugFixed(String id) {
		return JiraUtil.isIssueClose(id, null, null, null);
	}

	/**
	 * check if the bug has been fixed by comparing current version with bugVersion and fixedVersion
	 *
	 * @author pli3
	 * @since 2015.10.23 11:28:50
	 * @throws null
	 *
	 * @param id String, the bug ID
	 * @param modelName String, the name of your model inside versionModel bean
	 * @return boolean, it'll return True if all the bug have been closed and (current version is higher/equal to fixVersion or lower than bugVersion).
	 */
	public static boolean isBugFixed(String id, String modelName) {
		return JiraUtil.isIssueClose(id, modelName, null, null);
	}

	/**
	 * Verify if the bug has been fixed
	 * 
	 * @author pli3
	 * @since 2016.5.24 2:29:45
	 * @throws null
	 * 
	 * @param id String, the bug id
	 * @param modelName String, the name of the model
	 * @param bugVersion String, the version when the bug found
	 * @param fixedVersion String, the version when the bug is fixed
	 * @return boolean, it will be true if the bug has been fixed
	 */
	public static boolean isBugFixed(String id, String modelName, String bugVersion, String fixedVersion) {
		return JiraUtil.isIssueClose(id, modelName, bugVersion, fixedVersion);
	}

	/**
	 * Get the current node where browser in when running with Grid
	 * 
	 * @author pli3
	 * @since 2015.10.23 11:28:44
	 * @throws null
	 * 
	 * @param driver WebDriver, the selenium web driver you need
	 * @return String, it'll be the host name of the node
	 */
	public static String GetNodeName(WebDriver driver) {
		String node = null;
		try {
			String strTemp = Environment.getHub();
			String hub = strTemp.split("/")[2].split(":")[0];
			int port = Integer.parseInt(strTemp.split("/")[2].split(":")[1]);

			HttpHost host = new HttpHost(hub, port);

			@SuppressWarnings({ "resource" })
			HttpClientBuilder builder = HttpClientBuilder.create();
			CloseableHttpClient client = builder.build();
			String sessionUrl = "http://" + hub + ":" + port + "/grid/api/testsession?session=";
			URL session = new URL(sessionUrl + ((RemoteWebDriver) driver).getSessionId());

			BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("POST", session.toExternalForm());
			org.apache.http.HttpResponse response = client.execute(host, req);

			JSONObject object = new JSONObject(EntityUtils.toString(response.getEntity()));

			String proxyID = (String) object.get("proxyId");
			node = (proxyID.split("//")[1].split(":")[0]);
		} catch (Exception ex) {
			logger.error("[Util--getNodeName]" + ex.getMessage());
			ex.printStackTrace();
		}
		return node;
	}
	
	/**
	 * Get the path of image folder
	 *
	 * @author pli3
	 * @since 2015.12.23 4:51:03
	 * @throws null
	 *
	 * @return String, the path of the image folder
	 */
	public static String getImageFolderPath() {
		return Util.getClassPath() + File.separator + "testImage" + File.separator;
	}
	
}
