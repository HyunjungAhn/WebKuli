package com.nts.ti;

import java.util.List;

import org.openqa.selenium.WebElement;

import com.nts.ti.annotation.FindResource;
import com.nts.ti.annotation.FindSimilarResource;
import com.nts.ti.annotation.ScreenShot;

public interface CommonEvents {	
	
	@ScreenShot @FindResource public void clearText(String resourceName) throws Exception;
	@ScreenShot @FindResource public void clearText(String resourceName, int index) throws Exception;
	@ScreenShot @FindResource @FindSimilarResource public void click(String resourceName) throws Exception;
	@ScreenShot @FindResource @FindSimilarResource public void click(String resourceName, int index) throws Exception;
	@ScreenShot @FindResource public void clickAt(String resourceName) throws Exception;
	@ScreenShot @FindResource public void clickAt(String resourceName, int index) throws Exception;	
	@ScreenShot @FindResource @FindSimilarResource public boolean exists(String resourceName) throws Exception;
	@ScreenShot @FindResource @FindSimilarResource public boolean exists(String resourceName, int index) throws Exception;	
	@ScreenShot @FindResource public void flick(String resourceName, String direction) throws Throwable;
	@ScreenShot @FindResource public void flick(String resourceName, int index, String direction) throws Throwable;
	@ScreenShot @FindResource public String getLocation(String resourceName) throws Exception;
	@ScreenShot @FindResource public String getLocation(String resourceName, int index) throws Exception;
	@ScreenShot @FindResource @FindSimilarResource public String getText(String resourceName) throws Exception;
	@ScreenShot @FindResource @FindSimilarResource public String getText(String resourceName, int index) throws Exception;
	@ScreenShot @FindResource public void longTap(String resourceName) throws Exception;
	@ScreenShot @FindResource public void longTap(String resourceName, int index) throws Exception;
	@ScreenShot @FindResource public void scroll(String resourceName, String direction) throws Throwable;
	@ScreenShot @FindResource public void scroll(String resourceName, int index, String direction) throws Throwable;
	@ScreenShot public void sendKey(String keys) throws Exception;
	@ScreenShot @FindResource @FindSimilarResource public void sendKey(String resourceName, String keys) throws Exception;
	@ScreenShot @FindResource @FindSimilarResource public void sendKey(String resourceName, int index, String keys) throws Exception;
	@ScreenShot @FindResource public void singleTap(String resourceName) throws Exception;
	@ScreenShot @FindResource public void singleTap(String resourceName, int index) throws Exception;	
	@ScreenShot @FindResource public void select(String resourceName, int optIdx) throws Exception;
	@ScreenShot @FindResource public void select(String resourceName, int index, int optIdx) throws Exception;
	@ScreenShot @FindResource public boolean hasText(String compareText);
	@FindResource public int getElementCount(String resourceName);
	@ScreenShot public void isDisappear(String resourceName, int index, int retry);
	@ScreenShot public void isDisappear(String resourceName, int retry);

	public String deviceInfo();
	public void close();
	public String printElement();
	public String screenShot(String message) throws Exception;	
	public String screenShot(String serial, String message) throws Exception;
	public String adbScreenShot(String fileName) throws Exception;
	public String adbScreenShot(String serial, String fileName) throws Exception;
	public List<WebElement> findResource(String resourceName, int index) throws Exception;
	public String getDeviceList();
	
	public String getDate();
	public boolean containsString(String text1, String text2);
	public int changeToInteger(String text);
	
	// Android
	public void launchApp(String appPath);
	public void launchApp(String serial, String appPath);
	public void updateApp(String serial, String appPath);
	public void updateApp(String appPath);
	public void reclaimApp();
	public void reclaimApp(String serial);
	public void forward(int port);
	public void forward(String serial, int port);
	public boolean isReady();
	public void instrument(String appPath);
	public void instrument(String serial, String appPath);
	public void startApp(String targetApkPath);
	public void startApp(String serial, String targetApkPath);
	public void clearAppData(String appPath);
	public void clearAppData(String serial, String appPath);
	public void setOrientation(String orientation);
	
	public void addDevice(String deviceSerial, String url, int port) throws Exception;
	public void addDevice(String deviceSerial, String url, int port, String appPath) throws Exception;
	public void switchDevice(String deviceSerial) throws Exception;
	
	public void openBrowser(String url) throws Exception;
	public void openBrowser(String serial, String host, int port, String url) throws Exception;	
	public String getCurrentUrl() throws Exception;
	public String getTitle() throws Exception;	
	
	@ScreenShot public void goToUrl(String url);
	@ScreenShot public void goBack();
	@ScreenShot public void goForward();
	@ScreenShot public void refresh();
	public void closeBrowser();
	
	public void setTimeOut(int timeOut);
	public void setRetryInterval(int retryInterval);
	public void captureOn();
	public void captureOff();

	
	// Web
	public void setDriverPath(String driverPath);
	public void setBrowser(String browserName, String host);
	public void setBrowser(String browserName);
	public String encryptString(String txt) throws Exception;
	@ScreenShot @FindResource public void secureInputText(String xpath, String msg) throws Exception;
	public void clearPopup();
	public int countElements(String xpath);
	public void printCookies();
	public void selectValue(String xpath, String optValue) throws Exception;
	public void selectValue(String xpath, int idx, String optValue) throws Exception;
	public boolean existsText(String text);
	public void alertCancel();
	public void alertOk();
	public String getAlertText();
	
	public String downloadFile(String url, String dir);
	public String getPageSource();
	
}
