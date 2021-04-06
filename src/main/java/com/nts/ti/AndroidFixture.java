package com.nts.ti;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Alert;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.touch.TouchActions;

import com.nhncorp.ntaf.annotation.CellIgnoreFirst;
import com.nts.ti.common.NativeViewHelper;
import com.nts.ti.common.ResourceManager;
import com.nts.ti.common.SelendroidBuilder;
import com.nts.ti.common.WebViewHelper;

import fit.FitServer;
import io.selendroid.client.SelendroidDriver;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.exceptions.ShellCommandException;

/**
 * @author  Administrator
 */
public class AndroidFixture extends CommonFixture {

	/**
	 * @uml.property  name="selendriodBuilder"
	 * @uml.associationEnd  
	 */
	SelendroidBuilder selendroidBuilder;

	public AndroidFixture() {
		selendroidBuilder = new SelendroidBuilder(); // 셀렌드로이드 빌더 생성
	}

	/**
	 * Map에 원격테스트 하려는 단말기를 추가하는 메서드
	 *  
	 * @param serial 추가하려는 device 시리얼
	 * @param url device에 설치되어있는 selendroid 서버의 url
	 * @param port device에 설치되어있는 selendroid 서버의 port
	 * @throws Exception 
	 */
	@CellIgnoreFirst
	public void addDevice(String serial, String url, int port) throws Exception {
		String pkg = selendroidBuilder.getInstrumentedPkg(serial);
		addDevice(serial, url, port, pkg);
	}
	
	/**
	 * Map에 원격테스트 하려는 단말기를 추가하는 메서드
	 *  
	 * @param serial 추가하려는 device 시리얼
	 * @param url device에 설치되어있는 selendroid 서버의 url
	 * @param port device에 설치되어있는 selendroid 서버의 port
	 * @param appPath 테스트 앱의 절대경로 (e.g. D:\apk\Ndrive_Android_2013-08-01_11_48_22.apk)
	 * @throws Exception 
	 */
	public void addDevice(String serial, String url, int port, String appPath) throws Exception {
//		instrument(serial, appPath);
		
		selendroidBuilder.portListening(serial, 9090);
		
		forward(serial, port, 9090);

		SelendroidDriver driver = null;
		try {
			driver = new SelendroidDriver(new URL("http://" + url + ":" + port + "/wd/hub"), new SelendroidCapabilities(appPath));
			devices.put(serial, driver);
			if (devices.size() == 1) {
				this.driver = driver;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Thread.sleep(3000);
	}

	/**
	 * 원격테스트 device를 바꾸는 메소드
	 * 
	 * @param deviceSerial 바꾸려는 device의 시리얼
	 * @throws Exception (단말기 이름을 Map에서 찾지 못했을 때 발생)
	 */

	@CellIgnoreFirst
	public void switchDevice(String serial) throws Exception {
		if(devices.get(serial) instanceof SelendroidDriver){
			driver = (SelendroidDriver)devices.get(serial);
		}else{
//			driver = (AndroidDriver)devices.get(serial);
		}
		if (driver == null) {
			throw new Exception(serial + " fail to find");
		}
	}

	/**
	 * WebDriver를 이용해 해당 url의 주소값대로 모바일 웹 브라우져를 open하는 메서드
	 * 
	 * @param url 열고자 하는 웹의 주소
	 */
	public void openBrowser(String url) throws Exception {
		openBrowser(null, null, 0, url);
	}

	/**
	 * WebDriver를 이용해 해당 url의 주소값대로 모바일 웹 브라우져를 open하는 메서드
	 * 
	 * @param serial 모바일 기기의 시리얼 번호
	 * @param host 모바일 기기의 host 주소
	 * @param port 포워딩할 포트번호
	 * @param url 열고자 하는 웹의 주소
	 */
	public void openBrowser(String serial, String host, int port, String url) throws Exception {
		selendroidBuilder.LaunchAndroidDriverApk(serial);
		selendroidBuilder.portListening(serial, 9090);
		
		forward(serial, port, 9090);

		SelendroidDriver driver = null;
		
		SelendroidCapabilities cap = new SelendroidCapabilities();
		cap.setJavascriptEnabled(true);
		
		try {
			driver = new SelendroidDriver(new URL("http://" + host + ":" + port + "/wd/hub"), cap);
			driver.switchTo().window("WEBVIEW");
			currentDriver = "ANDROID";
			driver.get(url);
			devices.put(serial, driver);
			if (devices.size() == 1) {
				this.driver = driver;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Thread.sleep(3000);
	}

	/**
	 * device 정보(브랜드, 모델명, ip주소 등)를 출력하는 메소드
	 * 
	 * @return device 정보를 return
	 */
	public String deviceInfo() {
		return SelendroidBuilder.getDeviceInfo();
	}

	/**
	 * 연결 가능한 디바이스 리스트를 출력함.
	 * @return
	 */
	public String getDeviceList(){
		StringBuilder device = new StringBuilder();
		for(String d:SelendroidBuilder.getDeviceList()){
			device.append(d+",");
		}
		return device.toString().substring(0, device.toString().lastIndexOf(","));
	}

	/**
	 * 연결된 디바이스에 selendroid-server와 테스트 앱을 install, instrument 까지 자동으로 수행시켜주는 메소드
	 * (연결된 디바이스가 1대 일경우만 이용 가능)
	 * @param appPath 테스트 앱의 절대경로 (e.g. D:\apk\Ndrive_Android_2013-08-01_11_48_22.apk)
	 */
	public void launchApp(String appPath) {
		launchApp(null, appPath);
	}

	/**
	 * 연결된 디바이스에 selendroid-server와 테스트 앱을 install, instrument 까지 자동으로 수행시켜주는 메소드
	 * 
	 * @param serial 연결된 디바이스가 2대 이상일 경우 시리얼을 입력해야 함.(serial은 getDeviceInfo 메소드를 이용해서 얻을 수 있음)
	 * @param appPath 테스트 앱의 절대경로 (e.g. D:\apk\Ndrive_Android_2013-08-01_11_48_22.apk)
	 */
	public void launchApp(String serial, String appPath) {
		try {
			selendroidBuilder.launchApk(serial, appPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 연결된 디바이스에 이미 설치되어있는 앱을 상위 버전으로 업데이트 시키는 메소드
	 * @param appPath 업데이트 시키려는 앱의 full path 
	 */
	public void updateApp(String appPath){
		try{
			selendroidBuilder.updateApk(appPath);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 연결된 디바이스에 이미 설치되어있는 앱을 상위 버전으로 업데이트 시키는 메소드
	 * @param serial 디바이스 시리얼
	 * @param appPath 업데이트 시키려는 앱의 full path 
	 */
	public void updateApp(String serial, String appPath){
		try{
			selendroidBuilder.updateApk(serial, appPath);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 연결된 디바이스에서 Selendroid server와 연결된 앱의 데이터 삭제(캐시도 함께 삭제)
	 * @param appPath 업데이트 시키려는 앱의 full path 또는 패키지 명
	 */
	public void clearAppData(String appPath){
		try {
			selendroidBuilder.clearAppData(appPath);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * 연결된 디바이스에서 Selendroid server와 연결된 앱의 데이터 삭제(캐시도 함께 삭제)
	 * @param serial 디바이스 시리얼
	 * @param appPath 업데이트 시키려는 앱의 full path 또는 패키지 명
	 */
	public void clearAppData(String serial, String appPath){
		try {
			selendroidBuilder.clearAppData(serial, appPath);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * 연결된 디바이스에 selendroid server와 instrument 되어 있는 앱을 삭제하는 메소드 
	 */
	public void reclaimApp() {
		reclaimApp(null);
	}

	/**
	 * 연결된 디바이스에 selendroid server와 instrument 되어 있는 앱을 삭제하는 메소드 
	 * @param serial
	 */
	public void reclaimApp(String serial) {
		try {
			selendroidBuilder.reclaimApk(serial);
		} catch (ShellCommandException e) {
			e.printStackTrace();
		} catch (AndroidSdkException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 디바이스의 테스트 앱과 연결할 로컬 port를 지정.
	 * @param port 로컬포트
	 */
	public void forward(int localPort) {
		try {
			selendroidBuilder.forward(null, localPort);
		} catch (ShellCommandException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 여러 디바이스 중 serial에 해당하는 디바이스의 테스트 앱과 연결할 port를 지정
	 * 
	 * @param serial 단말기 시리얼
	 * @param port 로컬폴드
	 */
	public void forward(String serial, int localPort) {
		try {
			selendroidBuilder.forward(serial, localPort);
		} catch (ShellCommandException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 시리얼에 해당하는 디바이스의 원격 포트를 로컬의 포트로 넘겨주는 기능
	 * @param serial 단말기 시리얼
	 * @param localPort 로컬포트 
	 * @param remotePort 원격포트
	 */
	public void forward(String serial, int localPort, int remotePort){
		try {
			selendroidBuilder.forward(serial, localPort, remotePort);
		} catch (ShellCommandException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 테스트 앱이 selendroid server와 연결된 상태인지 확인하는 메소드
	 * 
	 * @return instrument가 완료된 경우 true, 그렇지 않으면 false return
	 */
	public boolean isReady() {
		try {
			return selendroidBuilder.isInstrumented();
		} catch (ShellCommandException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * selendroid server와 테스트 하려는 앱을 연결시켜주는 메소드
	 * 
	 * @param appPath 테스트 앱의 절대경로 (e.g. D:\apk\Ndrive_Android_2013-08-01_11_48_22.apk)
	 */
	public void instrument(String appPath) {
		instrument(null, appPath);
	}

	/**
	 * selendroid server와 테스트 하려는 앱을 연결시켜주는 메소드
	 * @param serial 디바이스 시리얼
	 * @param appPath 테스트 앱의 절대 경로
	 */
	public void instrument(String serial, String appPath) {
		try {
			if (null != serial && !serial.isEmpty()) {
				selendroidBuilder.instrument(serial, appPath);
			} else {
				selendroidBuilder.instrument(appPath);
			}
		} catch (ShellCommandException e) {
			e.printStackTrace();
		} catch (AndroidSdkException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 앱을 실행시켜주는 메소드
	 * 
	 * @param targetApkPath 실행시키려는 앱의 full path
	 */
	public void startApp(String targetApkPath) {
		try {
			selendroidBuilder.startApp(targetApkPath);
		} catch (AndroidSdkException e) {
			e.printStackTrace();
		} catch (ShellCommandException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 앱을 실행시켜주는 메소드
	 * 
	 * @param serial 테스트 디바이스의 시리얼
	 * @param targetApkPath 실행시키려는 앱의 full path 
	 */
	public void startApp(String serial, String targetApkPath) {
		try {
			selendroidBuilder.startApp(serial, targetApkPath);
		} catch (AndroidSdkException e) {
			e.printStackTrace();
		} catch (ShellCommandException e) {
			e.printStackTrace();
		}
	}	

	/**
	 * 모바일 기기의 화면 회전방향을 설정해주는 메서드
	 * @param orientation 방향 (portrait 세로/ landscape 가로)
	 */
	public void setOrientation(String orientation) {
		if (orientation.equalsIgnoreCase("portrait"))
			((SelendroidDriver) driver).rotate(ScreenOrientation.PORTRAIT);
		else if (orientation.equalsIgnoreCase("landscape")) {
			((SelendroidDriver)driver).rotate(ScreenOrientation.LANDSCAPE);
		} else {
		}
	}


	/**
	 * 안드로이드 모바일 앱, 웹의 엘리먼트를 찾는 메서드
	 * 
	 * @param resourceName
	 * @param index
	 */
	public List<WebElement> findResource(String resourceName, int index) {

		elementList = null;
		resourceName = ResourceManager.getKeyword(resourceName);

		//		List<WebElement> elementList = null;
		long current = System.currentTimeMillis();
		while (elementList == null && (System.currentTimeMillis() - current) < TIMEOUT) {
			printElement();
			try {
				if (resourceName.matches(XPATH_PATTERN)) {
					elementList = findWebResource(resourceName, index);
				} else {
					elementList = findNativeResource(resourceName, index);
				}
				if (index < elementList.size()) {
					if (elementList != null && !elementList.get(index).isEnabled()) {
						elementList = null;
					}
				}
				Thread.sleep(RETRY_INTERVAL);
			} catch (Exception e) {
				elementList = null;
			}
		}

		if (null != elementList) {
			if (null != FitServer.getHost()) {
				FitServer.setSymbol("$elapsed$", (System.currentTimeMillis() - current));
			}
		} else {
			FitServer.setSymbol("$elapsed$", null);
		}
		return elementList;
	}

	private List<WebElement> findWebResource(String resourceName, int index) {
		List<WebElement> elementList = new ArrayList<WebElement>();
		Matcher m = Pattern.compile(XPATH_PATTERN).matcher(resourceName);
		m.find();
		String by = m.group(2).replace("@", "").replace("()", "");
		String using = m.group(3);
		int currentWebView = 0;
		while (elementList.isEmpty() && currentWebView <= WebViewHelper.viewCount()) {
			if(!currentDriver.equals("ANDROID")) {
				if (currentWebView > 0) {
					currentDriver = "WEBVIEW_"+currentWebView;
				} else {
					currentDriver = "WEBVIEW";
				}
			}
			
			if(WebViewHelper.hasContent(currentWebView)) {
				try {
					if (currentDriver.contains("WEBVIEW")) {
						driver.switchTo().window(currentDriver);
					}
					if(index>0){
						if(by.equalsIgnoreCase("name")){
							elementList.addAll(driver.findElementsByName(using));
						}else if(by.equalsIgnoreCase("id")){
							elementList.addAll(driver.findElementsById(using));
						}else if(by.equalsIgnoreCase("class")){
							elementList.addAll(driver.findElementsByClassName(using));
						}else if(by.equalsIgnoreCase("text")){
							elementList.addAll(driver.findElementsByPartialLinkText(using));
						}else{
							elementList.addAll(driver.findElementsByXPath(resourceName));
						}
					}else{
						if(by.equalsIgnoreCase("name")){
							elementList.add(driver.findElementByName(using));
						}else if(by.equalsIgnoreCase("id")){
							elementList.add(driver.findElementById(using));
						}else if(by.equalsIgnoreCase("class")){
							elementList.add(driver.findElementByClassName(using));
						}else if(by.equalsIgnoreCase("text")){
							elementList.add(driver.findElementByPartialLinkText(using));
						}else{
							elementList.add(driver.findElementByXPath(resourceName));
						}
					}
				} catch (Exception e) {
					currentWebView++;
					continue;
				}
			}
			currentWebView++;
		}

		if(elementList.isEmpty()){
			return null;
		}

		return elementList;
	}

	private List<WebElement> findNativeResource(String resourceName, int index) {
		driver.switchTo().window("NATIVE_APP");
		List<WebElement> elementList = new ArrayList<WebElement>();

		if (NativeViewHelper.hasElement(resourceName) && index>0) {
			if (NativeViewHelper.getTypeMap().get(resourceName).equals("value")) {
				elementList.addAll(driver.findElementsByLinkText(resourceName));
			} else if (NativeViewHelper.getTypeMap().get(resourceName).equals("id")) {
				elementList.addAll(driver.findElementsById(resourceName));
			} else if (NativeViewHelper.getTypeMap().get(resourceName).equals("name")) {
				elementList.addAll(driver.findElementsByName(resourceName));
			} else {
				elementList.addAll(driver.findElementsByTagName(resourceName));
			}
		}else{
			if (NativeViewHelper.getTypeMap().get(resourceName).equals("value")) {
				elementList.add(driver.findElementByLinkText(resourceName));
			} else if (NativeViewHelper.getTypeMap().get(resourceName).equals("id")) {
				elementList.add(driver.findElementById(resourceName));
			} else if (NativeViewHelper.getTypeMap().get(resourceName).equals("name")) {
				elementList.add(driver.findElementByName(resourceName));
			} else {
				elementList.add(driver.findElementByTagName(resourceName));
			}
		}

		if(elementList.isEmpty()){
			return null;
		}

		return elementList;
	}

	/**
	 * 입력된 문자열 존재 여부 확인
	 * @param compareText
	 * @return true 문자열 있음, false 문자열이 없음
	 */
	public boolean hasText(String compareText) {
		long current = System.currentTimeMillis();
		while((System.currentTimeMillis()-current)<TIMEOUT){
			printElement();
			if(NativeViewHelper.hasText(compareText) || WebViewHelper.hasText(compareText)){
				if (null != FitServer.getHost()) {
					FitServer.setSymbol("$elapsed$", (System.currentTimeMillis() - current));
				} else {
					FitServer.setSymbol("$elapsed$", null);
				}
				return true;
			}
			try {
				Thread.sleep(RETRY_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (null != FitServer.getHost()) {
			FitServer.setSymbol("$elapsed$", (System.currentTimeMillis() - current));
		} else {
			FitServer.setSymbol("$elapsed$", null);
		}
		return false;
	}

	public void isDisappear(String resourceName, int retry){
		isDisappear(resourceName, 0, retry);
	}

	/**
	 * "로딩"과 같이 시나리오 수행 상 필수적으로 대기해야되는 로딩 요소가 사라질때 까지
	 * @param resourceName
	 * @param index
	 * @param retry 재 탐색 횟수 제한
	 */
	public void isDisappear(String resourceName, int index, int retry){
		List<WebElement> elementList = findResource(resourceName, index);
		while (elementList!=null && retry>0) {
			if(elementList.isEmpty()){
				break;
			}
			elementList = findResource(resourceName, index);
			retry--;
		}
	}

	/**
	 * 
	 * @param resourceName
	 * @param x
	 * @param y
	 * @throws Exception
	 */
	public void drag(String resourceName, int x, int y) throws Exception{
		drag(resourceName, 0, x, y);
	}

	/**
	 * 
	 * @param resourceName
	 * @param index
	 * @param x
	 * @param y
	 * @throws Exception
	 */
	public void drag(String resourceName, int index, int x, int y) throws Exception{
		List<WebElement> element = findResource(resourceName, index);
		if (element != null && index<element.size()) {
			WebElement we = element.get(index);
			int posX = we.getLocation().x+we.getSize().width/2;
			int posY = we.getLocation().y+we.getSize().height/2;
			TouchActions drag = new TouchActions(driver);
			drag.down(posX, posY).move(posX+x, posY+y).up(posX+x, posY+y).perform();
		} else {
			throw new Exception(resourceName + " fail to find");
		}
	}

	/**
	 * adb 명령어를 사용한 스크린샷
	 */
	public String adbScreenShot(String fileName) throws Exception {
		return adbScreenShot(null, fileName);
	}
	
	/**
	 * adb 명령어를 사용한 스크린샷
	 */
	public String adbScreenShot(String serial, String fileName) throws Exception {
		return selendroidBuilder.adbScreenShot(null, serial, fileName);
	}
	
	public String screenShot(String message) throws Exception {
		return screenShot(null, null, message);
	}
	
	public String screenShot(String serial, String message) throws Exception {
		return screenShot(null, serial, message);
	}
	
	public String screenShot(String when, String serial, String message) throws Exception {
		return selendroidBuilder.adbScreenShot(when, serial, message);
	}
	
	public void setDeviceMode(String deviceMode) throws Exception {
		// TODO Auto-generated method stub

	}
	
	public void alertCancel() {
		Alert alert = driver.switchTo().alert();
		alert.dismiss();
	}

	public void alertOk(){
		Alert alert = driver.switchTo().alert();
		alert.accept();
	}

	public String getAlertText() {
		Alert alert = driver.switchTo().alert();
		return alert.getText();
	}

	// Added caused by WebFixture

	public void setBrowser(String browserName, String host) {
	}

	public void setBrowser(String browserName) {
	}

	public String encryptString(String txt) throws Exception {
		return null;
	}

	public void secureInputText(String xpath, String msg) throws Exception {
	}

	public void clearPopup() {
	}

	public int countElements(String xpath) {
		return 0;
	}

	public void printCookies() {
	}

	public void selectValue(String xpath, String optValue) throws Exception {
	}

	public void selectValue(String xpath, int idx, String optValue)
			throws Exception {
	}

	public void focusedWindow() {
	}

	public boolean existsText(String text) {
		return false;
	}

	public void startIosServer(String appPath, int port) throws Exception {
	}

	public void addIosDevice(String appName) throws Exception {
	}

	public void doubleClick(String xpath) {
	}

	public void setDriverPath(String resourcePath) {
	}
}
