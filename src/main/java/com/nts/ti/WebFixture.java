package com.nts.ti;


import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.Select;

import com.nts.ti.common.ResourceManager;
import com.nts.ti.events.SendKeyEvent;
import com.nts.ti.util.HostUtil;
import com.nts.ti.util.TextEncrypt;

import fit.FitServer;


public class WebFixture extends CommonFixture {

	TextEncrypt encrypt;

	String driverPath = System.getProperty("user.dir")+File.separator+"driver";
	String libPath = System.getProperty("user.dir")+File.separator+"libs";

	private static int FIND_TIMEOUT = 1;
	private static int DIALOG_TIMEOUT = 500;
	// latch: 리소스 retry
	CountDownLatch latch = new CountDownLatch(3);

	// kyes : sikuli key를 접근하기 위한 map(key,value모음)객체
	Map<String,Object> keys = new HashMap<String,Object>();

	// modifiers : tab, alt 등의 키를 정의해 주는 구분자
	Map<String,Integer> modifiers = new HashMap<String, Integer>();
	
	public WebFixture(){
		// screen : sikuli 구동 객체
//		screen = new Screen();

		encrypt = new TextEncrypt();

		System.setProperty("java.library.path", System.getProperty("java.library.path")+";"+libPath);

	}

	/**
	 * 드라이버의 경로를 지정함
	 * @param resourcePath 리소스의 경로
	 */
	public void setDriverPath(String driverPath){
		this.driverPath = driverPath;
	}

	public void setDialogTimeOut(int ms){
		DIALOG_TIMEOUT = ms;
	}

	public void setFindTimeOut(int sec){
		FIND_TIMEOUT = sec;
	}

	/**
	 * local Test시 Test할 browser 선택
	 * 
	 * @param browserName
	 */
	public void setBrowser(String browserName) {
		setBrowser(browserName, "");
	}

	/**
	 * 원격 Test시 테스트할 PC의 host와 Browser 선택
	 * IE, Chrome 선택 가능
	 * @param host
	 * @param browserName
	 */
	public void setBrowser(String browserName, String host) {
		currentDriver = "WEBVIEW";
		int port = 5555;
		if (null != host && !host.isEmpty()) {
			if (browserName.equalsIgnoreCase("ie")
					|| (browserName.equalsIgnoreCase("explorer"))) {
				DesiredCapabilities capability = DesiredCapabilities.internetExplorer();
				capability.setJavascriptEnabled(true);
				capability.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
				try {
					driver = new RemoteWebDriver(new URL("http://" + host
							+ ":5555/wd/hub"), capability);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			} else if (browserName.equalsIgnoreCase("ff")
					|| (browserName.equalsIgnoreCase("firefox"))) {
				DesiredCapabilities capability = DesiredCapabilities.firefox();
				try {
					driver = new RemoteWebDriver(new URL("http://" + host
							+ ":5555/wd/hub"), capability);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			} else if (browserName.equalsIgnoreCase("cr")
					|| (browserName.equalsIgnoreCase("chrome"))) {
				DesiredCapabilities capability = DesiredCapabilities.chrome();
				try {
					driver = new RemoteWebDriver(new URL("http://" + host
							+ ":9515"), capability);
					port = 9515;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			} else if (browserName.equalsIgnoreCase("sf")
					|| (browserName.equalsIgnoreCase("safari"))) {
				DesiredCapabilities capability = DesiredCapabilities.safari();
				try {
					driver = new RemoteWebDriver(new URL("http://" + host
							+ ":5555/wd/hub"), capability);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			if (browserName.equalsIgnoreCase("ie")
					|| (browserName.equalsIgnoreCase("explorer"))) {
				System.setProperty("webdriver.ie.driver",
						driverPath + SEPERATOR + "IEDriverServer.exe");
				driver = new InternetExplorerDriver();
			} else if (browserName.equalsIgnoreCase("cr")
					|| (browserName.equalsIgnoreCase("chrome"))) {
				System.setProperty("webdriver.chrome.driver",
						driverPath + SEPERATOR + "chromedriver.exe");
				driver = new ChromeDriver();
				port = 9515;
			} else if (browserName.equalsIgnoreCase("ff")
					|| (browserName.equalsIgnoreCase("firefox"))) {
				driver = new FirefoxDriver();
			} else if (browserName.equalsIgnoreCase("sf")
					|| (browserName.equalsIgnoreCase("safari"))) {
				driver = new SafariDriver();
			}
		}
		try {
			addDevice(browserName, host, port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openBrowser(String url) throws Exception{
		driver.get(url);
	}

	public List<WebElement> findResource(String resourceName, int index) throws Exception {
		resourceName = ResourceManager.getKeyword(resourceName);
		long current = System.currentTimeMillis();
		elementList = null;
		
		while (elementList == null && (System.currentTimeMillis() - current) < TIMEOUT) {
			try {
				Thread.sleep(RETRY_INTERVAL);

				elementList = findElements(resourceName, index);		
				if (index > elementList.size()) {
					elementList = null;
				}
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

	/**
	 * 비정상 종료된 팝업 닫기 
	 */
	public void clearPopup(){
		Object[] handles = driver.getWindowHandles().toArray();
		if(handles.length>1){
			for(int i=1;i<handles.length;i++){
				try{
					driver.switchTo().window((String) handles[i]).close();
				}catch(Exception e){
					continue;
				}
			}
		}
	}

	/**
	 * 입력받은 xPath가 페이지 내 몇 개 존재하는지 return
	 * @param xpath 요소의 경로
	 */
	public int countElements(String xpath){
		int elementCount = 0;
		Map<Object, List<WebElement>> handleList = findAtWindowElements(xpath);
		for(Object handle:handleList.keySet().toArray()){
			elementCount += handleList.get(handle).size();
		}
		return elementCount;
	}

	/**
	 * 암호할 문자열을 입력하여 암호화
	 * @param txt 암호화할 문자열
	 * @return 암호화된 문자열
	 */
	public String encryptString(String txt) throws Exception{
		return encrypt.returnEncryptCode(txt);
	}

	/**
	 * 암호화를 위한 Seed값을 세팅
	 * @param 암호화 할 Seed (8자 이상)
	 */
	public void setSeed(String seed) throws Exception{
		encrypt.getKeyString(seed);
	}


	/**
	 * 해당 요소의 경로에 텍스트가 존재하는지 확인 함
	 * @param text 예상 텍스트
	 * @return 요소의 경로에서 추출한 텍스트와 예상 테스트 비교 결과 리턴
	 */
	public boolean existsText(String text){
		try {
			latch.await(200, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Object[] handles = driver.getWindowHandles().toArray();
		if(handles.length>1){
			for(Object handle:handles){
				if(driver.switchTo().window((String) handle).getPageSource().contains(text)){
					return true;
				}
			}
			return false;
		}else{
			return driver.getPageSource().contains(text);
		}
	}

	/**
	 * 해당 셀렉트박스에서 선택 함(옵션 값 기준)
	 * @param xpath 요소(셀렉트박스)의 경로
	 * @param optValue 셀렉트박스 내 옵션의 값
	 * @throws Exception
	 */
	public void selectValue(String xpath, String optValue) throws Exception{
		selectValue(xpath,0,optValue);
	}

	/**
	 * 해당 셀렉트박스에서 선택 함(셀렉트박스 인덱스, 옵션 값 기준)
	 * @param xpath 요소(셀렉트박스)의 경로
	 * @param idx 셀렉트박스의 인덱스번호
	 * @param optValue 셀렉트박스 내 옵션의 값 
	 * @throws Exception
	 */
	public void selectValue(String xpath, int idx, String optValue) throws Exception{
		elementList = findResource(xpath, idx);
		try{
			new Select(elementList.get(idx)).selectByVisibleText(optValue);
		}catch(Exception e){
			System.out.println(xpath+" Find Fail");
		}		
	}

	/**
	 * 기대 얼럿 문구와 비교
	 * @param text 기대 문구
	 * @return 일치 결과
	 */
	public String getAlertText(){
		Alert alert = findModalDialog();
		if(null!=alert){
			return alert.getText();
		}else{
			return "Fail to get text";
		}
	}

	public void sendKeyToDialog(String key){
		Alert alert = findModalDialog();
		if(null!=alert){
			alert.sendKeys(key);
		}
	}

	/**
	 * Confirm창의 확인 또는 예를 클릭 함(수락)
	 */
	public void alertOk(){
		Alert alert = findModalDialog();
		if(null!=alert){
			alert.accept();
		}
		try {
			latch.await(DIALOG_TIMEOUT,TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Confirm창의 취소 또는 아니오를 클릭 함(거절)
	 */
	public void alertCancel(){
		Alert alert = findModalDialog();
		if(null!=alert){
			alert.dismiss();
		}
		try {
			latch.await(DIALOG_TIMEOUT,TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
	}

	public void printCookies(){
		Cookie[] cookies = new Cookie[driver.manage().getCookies().size()];
		driver.manage().getCookies().toArray(cookies);
		for(Cookie c:cookies){
			System.out.println(c.getName()+":\t"+c.getValue());
		}
	}

	Map<String, String> mapData = new HashMap<String, String>();

	/**
	 * mapData에 테스트 데이터 저장
	 * @param key
	 * @param value
	 */
	public void putMapData(String key, String value){
		mapData.put(key, value);
	}

	/**
	 * mapData에서 key에 해당되는 value 리턴
	 * @param key
	 * @return
	 */
	public String getMapData(String key){
		return mapData.get(key);
	}

	/**
	 * mapData 초기화
	 */
	public void clearMapData(){
		mapData = new HashMap<String, String>();
	}

	/**
	 * txt1과 txt2 문자열 비교
	 * @param txt1
	 * @param txt2
	 * @return
	 */
	public boolean compareString(String txt1, String txt2){
		return txt1.equals(txt2);
	}

	/**
	 * txt1에 txt2 문자열 포함 여부
	 * @param txt1
	 * @param txt2
	 * @return
	 */
	public boolean containString(String txt1, String txt2){
		return txt1.contains(txt2);
	}


	/**
	 * element 찾는 순서(본창 -> iframe(---) -> (팝업 -> iframe(---))(---) 
	 */
	private List<WebElement> findElements(String xpath, int index){
		//		List<WebElement> elements = new ArrayList<WebElement>();
		try{
			Map<Object, List<WebElement>> handleList = findAtWindowElements(xpath);
			Object window = null;
			if(handleList.size()>index){
				List<WebElement> elements = new ArrayList<WebElement>();
				int elementsCount = 0;
				for(Object o:handleList.keySet().toArray()){
					elementsCount += handleList.get(o).size();
					elements.addAll(handleList.get(o));
					if(elementsCount>index){
						window = o;
						if(handleList.size()>1)
							index = (elementsCount-1)-index;
						break;
					}
				}
			}else{
				List<WebElement> elements = new ArrayList<WebElement>();
				int elementsCount = 0;
				for(Object o:handleList.keySet().toArray()){
					elementsCount += handleList.get(o).size();
					elements.addAll(handleList.get(o));
					if(elementsCount>index){
						window = o;

						if(handleList.size()>1)
							index = (elementsCount-1)-index;
					}
				}

			}
			//TODO: 본창의 중첩된 iframe은 처리됨(팝업의 iframe은 좀 고민해보자)
			if(((String)window).contains("iframe")){
				String[] nestedFrames = ((String)window).split(":");
				for(String frame:nestedFrames){
					if(frame.contains("iframe")){
						return driver.switchTo().frame(Integer.parseInt(frame.replace("iframe", ""))).findElements(By.xpath(xpath));
					}
				}
			}

			return driver.switchTo().window((String) window).findElements(By.xpath(xpath));
		}catch(Exception e){
			latch.countDown();
			try {
				latch.await(FIND_TIMEOUT, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
			}
			if(latch.getCount()>0){
				return findElements(xpath,index);
			}else{
				latch = new CountDownLatch(3);
				return null;
			}
		}
	}


	/**
	 * popup등 window handle 관련
	 */
	private Map<Object, List<WebElement>> findAtWindowElements(String xpath){
		Map<Object,List<WebElement>> elementsAtWindowHandle = new HashMap<Object,List<WebElement>>();
		for(Object handle:driver.getWindowHandles().toArray()){
			List<WebElement> elements = new ArrayList<WebElement>();
			elementsAtWindowHandle.put(handle, driver.switchTo().window((String)handle).findElements(By.xpath(xpath)));
			findAtFrameElements(driver, handle, elementsAtWindowHandle, xpath);
			driver.switchTo().defaultContent();
			if(!elementsAtWindowHandle.get(handle).isEmpty()){
				elements.addAll(elementsAtWindowHandle.get(handle));
			}else{
				elementsAtWindowHandle.remove(handle);
			}
		}
		return elementsAtWindowHandle;
	}

	/**
	 * iframe 관련 참고: http://darrellgrainger.blogspot.kr/2012/04/frames-and-webdriver.html
	 */
	private void findAtFrameElements(WebDriver driver, Object handle, Map<Object, List<WebElement>> elements, String xpath){
		int frameCnt = driver.findElements(By.tagName("iframe")).size();
		for(int i=0; i<frameCnt ; i++){
			try{
				elements.put(handle+(":iframe"+i), driver.switchTo().frame(i).findElements(By.xpath(xpath)));
				if(driver.findElements(By.tagName("iframe")).size()>0){
					findAtFrameElements(driver, handle+(":iframe"+i), elements, xpath);
				}
			}catch(Exception e){
				continue;
			}
		}
	}

	private Alert findModalDialog(){
		try{
			return driver.switchTo().alert();
		}catch (Exception e) {
			String[] handles = new String[driver.getWindowHandles().size()];
			driver.getWindowHandles().toArray(handles);
			for(int i=0, winCnt=handles.length;i<winCnt;i++){
				driver.switchTo().window(handles[i]);
				try{
					Alert alert = driver.switchTo().alert();
					if(null!=alert){
						return alert;
					}
				}catch(Exception exception){
					continue;
				}
			}
		}
		return null;

	}

	/**
	 * 윈도우 hosts 파일에 호스트 추가
	 * @param ip 아이피
	 * @param host 도메인
	 * @throws Exception
	 */
	public void addHost(String ip, String host) throws Exception {
		HostUtil.addHost(ResourceManager.getKeyword(ip), ResourceManager.getKeyword(host));
	}

	/**
	 * 윈도우 hosts 파일에 호스트 삭제
	 * @param host 삭제할 도메인
	 * @throws Exception
	 */
	public void deleteHost(String host) throws Exception {
		HostUtil.deleteHost(host);
	}

	public void secureInputText(String resourceName, String keys) throws Exception{
		if(null!=keys && !keys.isEmpty()){
			template.execute(elementList, 0, new SendKeyEvent(driver, keySeq, encrypt.returnDecryptCode(keys), currentDriver));
		}else{
			throw new Exception("input message is empty!");
		}
	}
	
/*	
	public String screenShot(String message) throws Exception{
		return screenShot(null, message);
	}

	public String screenShot(String when, String message) throws Exception{		
		File directory = new File(".");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String timeStamp = sdf.format(Calendar.getInstance().getTime());
		sdf = new SimpleDateFormat("yyyy_MM_dd");
		String dateDir = sdf.format(Calendar.getInstance().getTime());

		String nameScreenshot;
		String newFileNamePath;

		if (when != null) {
			nameScreenshot = timeStamp + "_" + when + "_" + message + ".png";
			if(when.equals("exception")){
				newFileNamePath = directory.getCanonicalPath() + SEPERATOR + "FitNesseRoot" + SEPERATOR + "files" + SEPERATOR + 
						"screenShots" + SEPERATOR + dateDir + SEPERATOR + 
						"exception"	+ SEPERATOR + nameScreenshot;
			}else{
				newFileNamePath = directory.getCanonicalPath() + SEPERATOR + "FitNesseRoot" + SEPERATOR + "files" + SEPERATOR + 
						"screenShots" + SEPERATOR + dateDir + SEPERATOR + 
						"history"	+ SEPERATOR + nameScreenshot;
			}
		} else {
			nameScreenshot = timeStamp + message + "_" + ".png";
			newFileNamePath = directory.getCanonicalPath() + SEPERATOR + "FitNesseRoot" + SEPERATOR + "files" + SEPERATOR + 
					"screenShots" + SEPERATOR + dateDir + SEPERATOR
					+ nameScreenshot;
		}

		File file = new File(newFileNamePath);

		if(currentDriver.equals("WEBVIEW")){ 
			File srcFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(srcFile, file);

		} else {
			File screenshot;

			try{
				screenshot = ((SelendroidDriver)driver).getScreenshotAs(OutputType.FILE);
			}catch(Exception e){
				screenshot = ((AndroidDriver)driver).getScreenshotAs(OutputType.FILE);
			}
			FileUtils.copyFile(screenshot, new File(newFileNamePath));
		}

		String linkImg = dateDir + "/" + nameScreenshot;
		String fitServer = "";
		if(null!=FitServer.getHost() && !FitServer.getHost().isEmpty()){
			if(FitServer.getPort()==80){
				fitServer = "http://"+FitServer.getIp();
			}else{
				fitServer = "http://"+FitServer.getIp()+":"+FitServer.getPort();
			}
		}

		if(when == null){
			return "<a href='"+ fitServer + "/files/screenShots/" + linkImg + "' target='_blank'><img src='"+ fitServer + "/files/screenShots/" + linkImg + "' width=300></a>";			
		} else if(when.equals("exception")) {
			return "<a href='"+ linkImg + "exception/" + nameScreenshot + "' target='_blank'><img src='"+ linkImg + "exception/" + nameScreenshot + "' width=300></a>";
		} else {
			return "<a href='"+ linkImg + "history/" + nameScreenshot + "' target='_blank'><img src='"+ linkImg + "history/" + nameScreenshot + "' width=300></a>";
		}
		
	}
*/


	public String deviceInfo() {
		return null;
	}
	public void launchApp(String appPath) {
	}

	public void launchApp(String serial, String appPath) {
	}

	public void reclaimApp() {
	}

	public void reclaimApp(String serial) {
	}

	public void forward(int port) {
	}

	public void forward(String serial, int port) {
	}

	public boolean isReady() {
		return false;
	}

	public void instrument(String appPath) {
	}

	public void instrument(String serial, String appPath) {
	}

	public void startApp(String targetApkPath) {
	}

	public void startApp(String serial, String targetApkPath) {
	}

	public void setOrientation(String orientation) {
	}

	public void setDriver(String url, int port) throws Exception {
	}

	public void setDriver(String deviceSerial, String url, int port) throws Exception {
	}

	public void addDevice(String deviceSerial, String url, int port) throws Exception {
		devices.put(deviceSerial, driver);
	}
	
	public void addDevice(String deviceSerial, String url, int port, String appPath) throws Exception {
		devices.put(deviceSerial, driver);
	}

	public void switchDevice(String deviceSerial) throws Exception {
		driver = devices.get(deviceSerial);
	}

	public void openBrowser(String serial, String host, int port, String url)
			throws Exception {
	}

	public void setDeviceMode(String deviceMode) throws Exception {

	}	

	public boolean hasText(String compareText) {
		return false;
	}

	public void isDisappear(String resourceName, int index, int retry) {
	}

	public String getDeviceList() {
		return null;
	}

	public void isDisappear(String resourceName, int retry) {
	}

	public void drag(String resourceName, int x, int y) throws Exception {
	}

	public void drag(String resourceName, int index, int x, int y) throws Exception {
	}

	public void updateApp(String serial, String appPath) {
	}

	public void updateApp(String appPath) {	
	}

	public void clearAppData(String appPath) {
	}

	public void clearAppData(String serial, String appPath) {	
	}

	public String adbScreenShot(String fileName) throws Exception {
		return null;
	}

	public String adbScreenShot(String serial, String fileName)throws Exception {
		return null;
	}
	
	public String printElement() {
		return null;
	}
}