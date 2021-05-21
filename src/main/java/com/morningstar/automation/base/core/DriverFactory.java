package com.morningstar.automation.base.core;

import com.morningstar.automation.base.cons.BaseCons;
import com.morningstar.automation.base.core.beans.BrowserTypeEnum;
import com.morningstar.automation.base.core.beans.ProfileBean;
import com.morningstar.automation.base.core.configurations.Environment;
import com.morningstar.automation.base.core.utils.ExportUtil;
import com.morningstar.automation.base.core.utils.Logger;
import com.morningstar.automation.base.core.utils.Util;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobilePlatform;
import com.alibaba.fastjson.JSONObject;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DriverFactory {

	private static final Logger LOGGER = Logger.getLogger(DriverFactory.class);
	private static final String SET_UP_LOGGER_PREFIX = "[SetUp]Test Case ID:";

	private static final String BROWSER_TYPE = Util.getBrowserType();
	private static final String BROWSER_VERSION = Util.getBrowserVersion();
	private static final String PLATFORM = Util.getPlatform();
	private static final ProfileBean PROFILE = Environment.getEnvironmentBean().getProfile();

	/**
	 * Defaults to framework implementation for non-chrome browsers.
	 * Chrome browsers have a few enhancements: they disable flash & dns prefetch to deal with known issues; and they add
	 * noteasers and noads query strings to urls.
	 * @param methodName
	 * @return
	 */
	public static WebDriver newDriver(String methodName) {
		LOGGER.info(SET_UP_LOGGER_PREFIX + methodName + "Creating driver...");
		LOGGER.info(SET_UP_LOGGER_PREFIX + methodName + "=====browserType: " + BROWSER_TYPE + ", verison:"
				+ BROWSER_VERSION + ", platform: " + PLATFORM);
		LOGGER.info(SET_UP_LOGGER_PREFIX + methodName + "====Creating driver...");
		WebDriver driver = createDriver(methodName);
		LOGGER.info(SET_UP_LOGGER_PREFIX + methodName + "====Create driver successfully.");
		return driver;
	}

	public static WebDriver createDriver(String testCaseId) {
		if (Util.getMode().equals(BaseCons.DEBUG_MODE) && !Util.isUseGrid()) {
			return createLocalDriver(testCaseId);
		} else {
			return createRemoteDriver(testCaseId);
		}
	}

	/**
	 * because Selenium haven't support mobile UI test for a long time, so we will find another way to integrate 
	 * with other framework or tool to do it
	 * @param mobileProfile
	 * @return
	 */
	@Deprecated
	private static String[] getMobileProfile(String mobileProfile) {
		String[] profile = new String[2];
		if ("".equals(mobileProfile)) {
			return null;
		} else {
			String[] parts = mobileProfile.split(";");
			profile[0] = parts[0];
			profile[1] = parts[1];
			return profile;
		}
	}

	/**
	 * because Selenium haven't support mobile UI test for a long time, so we will find another way to integrate 
	 * with other framework or tool to do it
	 * @param mobileProfile
	 * @return
	 */
	@Deprecated
	private static WebDriver createAndroidDriver() {
		DesiredCapabilities dc = DesiredCapabilities.android();
		String[] profile = getMobileProfile(PROFILE.getAndroidProfile());
		// profile[1] is 2nd part of profile(emulator or real device)
		// profile[0] is 1st part of profile(device name)
		dc.setCapability("deviceName", profile[1]);
		dc.setCapability("platformName", MobilePlatform.ANDROID);
		dc.setCapability(CapabilityType.BROWSER_NAME, "Chrome");
		dc.setCapability("appPackage", "com.android.chrome");
		dc.setCapability("appActivity", "com.google.android.apps.chrome.Main");
		String hud = Environment.getEnvironmentBean().getServer().getappiumHub();
		URL url = null;
		try {
			url = new URL(hud);
		} catch (MalformedURLException e) {
			LOGGER.error(e);
		}
		WebDriver driver = new AndroidDriver(url, dc);
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		Set<String> contextNames = ((AndroidDriver) driver).getContextHandles();
		for (String contextName : contextNames) {
			if (contextName.contains("WEBVIEW_1")) {
				((AndroidDriver) driver).context(contextName);
			}
		}
		return driver;
	}

	/**
	 * because Selenium haven't support mobile UI test for a long time, so we will find another way to integrate 
	 * with other framework or tool to do it
	 * @param mobileProfile
	 * @return
	 */
	@Deprecated
	private static WebDriver createIOSDriver(String xcodeVersion) {
		DesiredCapabilities dc = new DesiredCapabilities();
		String[] profile = getMobileProfile(PROFILE.getIosProfile());
		// profile[0] is 1st part of profile(device name)
		dc.setCapability("deviceName", profile[0] + " " + profile[1]);
		dc.setCapability("platformVersion", xcodeVersion);
		dc.setCapability("platformName", MobilePlatform.IOS);
		dc.setCapability("browserName", "safari");
		dc.setCapability("launchTimeout", "120000");
		// profile[1] is 2nd part of profile(simulator or UDID)
		if (!profile[1].toLowerCase().contains("simulator")) {
			dc.setCapability("udid", profile[1]);
		}

		String hud = Environment.getEnvironmentBean().getServer().getappiumHub();
		URL url = null;
		try {
			url = new URL(hud);
			Thread.sleep(1000);
		} catch (MalformedURLException | InterruptedException e) {
			LOGGER.error(e);
		}
		WebDriver driver = new IOSDriver(url, dc);
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);

		Set<String> contextNames = ((IOSDriver) driver).getContextHandles();
		for (String contextName : contextNames) {
			if (contextName.contains("WEBVIEW_1")) {
				((IOSDriver) driver).context(contextName);
			}
		}
		return driver;
	}

	private static WebDriver createDesktopDriver(String platform,
												 String testCaseId) {
		if (BROWSER_TYPE == null) {
			throw new RuntimeException("Browser Type is Null");
		}

		BrowserTypeEnum browserTypeEnum = BrowserTypeEnum.valueOf(BROWSER_TYPE.toUpperCase());
		DesiredCapabilities capabilities = browserTypeEnum.getCapabilities(testCaseId, BROWSER_VERSION);

		if (capabilities == null) {
			String str = String.format("Could not find browser type: %s", BROWSER_TYPE);
			throw new RuntimeException(str);
		}

		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setPlatform(Platform.valueOf(platform.toUpperCase()));

		return new RemoteWebDriver(getHubUrl(), capabilities);
	}

	private static URL getHubUrl() {
		String hub = Environment.getHub();
		try {
			return new URL(hub);
		} catch (MalformedURLException e) {
			LOGGER.error(e);
			throw new AssertionError("Malformed Hub URL", e);
		}
	}

	private static WebDriver createRemoteDriver(String testCaseId) {
		WebDriver driver = null;
		if (Util.getPlatform().toUpperCase().contains("ANDROID")) {
			driver = createAndroidDriver();
		} else if (Util.getPlatform().toUpperCase().contains("IOS")) {
			driver = createIOSDriver(BROWSER_VERSION);
		} else {
			driver = createDesktopDriver(PLATFORM, testCaseId);
		}
		return driver;
	}

	private static WebDriver createLocalDriver(String testCaseId) {
		BrowserTypeEnum browserTypeEnum = BrowserTypeEnum.valueOf(BROWSER_TYPE.toUpperCase());
		System.setProperty(browserTypeEnum.getPropertyName(), Util.getDriverPath(BROWSER_TYPE));
		return browserTypeEnum.createLocalDriver(PROFILE, testCaseId, BROWSER_VERSION);
	}

	public static SafariOptions getSafariProfile(String safariOption) {
		SafariOptions options = new SafariOptions();
		if (safariOption.equals("")) {
			return options;
		} else {  
			//JSONObject jsonObject = JSONObject.parseObject(safariOption);  
			//options.setPort(Integer.parseInt(temp[0])); 
			//options.setUseTechnologyPreview(jsonObject.getBoolean("technologyPreview"));
		}
		return options;
	}

	public static ChromeOptions getChromeProfile(String chromeProfile, String... testCaseId) {
		ChromeOptions options = new ChromeOptions();

		// Disable develop mode
		options.addArguments("chrome.switches", "--disable-extensions");

		if (chromeProfile.equals("")) {
			return options;
		} else {
			File file = new File(chromeProfile);
			if (!file.exists() && !file.isDirectory()) {
				HashMap<String, Object> chromePrefs = new HashMap<>();
				JSONObject jsonObj = JSONObject.parseObject(chromeProfile);

				@SuppressWarnings("unchecked")
				Iterator<String> keys = jsonObj.keySet().iterator();
				while (keys.hasNext()) {
					String key = keys.next();
					String val = jsonObj.getString(key);

					if (key.equals("download.default_directory")) {
						// Update the download directory
						val = val + "\\rnd_" + testCaseId + "_" + System.currentTimeMillis();
						if (testCaseId.length > 0) {
							ExportUtil.addDownloadDIR(testCaseId[0], val);
						}
						LOGGER.info("***===downloadDIR: " + val);
					}

					if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
						chromePrefs.put(key, Boolean.parseBoolean(val));
					} else if (val.matches("\\d+")) {
						chromePrefs.put(key, Integer.parseInt(val));
					} else {
						chromePrefs.put(key, val);
					}
				}

				options.setExperimentalOption("prefs", chromePrefs);
				options.addArguments("--test-type");
			} else {
				options.addArguments("user-data-dir=" + chromeProfile);
			}
			return options;
		}
	}

	public static FirefoxProfile getFirefoxProfile(String firefoxProfile, String... testCaseId) {
		ProfilesIni allProfiles = new ProfilesIni();
		FirefoxProfile profile = new FirefoxProfile();

		// Check if it has already initialized the authentication setting
		boolean init = false;

		switch (firefoxProfile.toLowerCase()) {
			case "":
				break;
			case "webdriver":
			case "default":
				profile = allProfiles.getProfile(firefoxProfile);
				break;
			default:
				File file = new File(firefoxProfile);
				if (!file.exists() && !file.isDirectory()) {
					JSONObject jsonObj = JSONObject.parseObject(firefoxProfile);

					@SuppressWarnings("unchecked")
					Iterator<String> keys = jsonObj.keySet().iterator();
					while (keys.hasNext()) {
						String key = keys.next();
						String val = jsonObj.getString(key);

						if (key.equals("browser.download.dir")) {
							// Update the download directory
							val = val + "\\rnd_" + testCaseId + "_" + System.currentTimeMillis();
							if (testCaseId.length > 0) {
								ExportUtil.addDownloadDIR(testCaseId[0], val);
							}
							LOGGER.info("***===downloadDIR: " + val);
						}

						if (key.equals("network.automatic-ntlm-auth.trusted-uris")) {
							init = true;
						}

						if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
							profile.setPreference(key, Boolean.parseBoolean(val));
						} else if (val.matches("\\d+")) {
							profile.setPreference(key, Integer.parseInt(val));
						} else {
							profile.setPreference(key, val);
						}
					}
				} else {
					File profileDir = new File(firefoxProfile);
					profile = new FirefoxProfile(profileDir);
				}

				break;
		}

		// Add support to solve Firefox Authentication issue
		if (!init) {
			profile.setPreference("network.automatic-ntlm-auth.trusted-uris", BaseCons.TRUSTEDSITES);
			profile.setPreference("network.negotiate-auth.delegation-uris", BaseCons.TRUSTEDSITES);
			profile.setPreference("network.negotiate-auth.trusted-uris", BaseCons.TRUSTEDSITES);
			profile.setPreference("signon.autologin.proxy", true);
		}

		return profile;
	}

}
