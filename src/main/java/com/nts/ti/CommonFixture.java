package com.nts.ti;

import fit.FitServer;
import fitnesse.testutil.FitNesseUtil;
import io.selendroid.client.SelendroidDriver;
import io.selendroid.client.SelendroidKeys;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.nhncorp.ntaf.annotation.CellIgnoreFirst;
import com.nts.ti.common.DownloadFileUrl;
import com.nts.ti.common.NativeViewHelper;
import com.nts.ti.common.ResourceManager;
import com.nts.ti.common.WebViewHelper;
import com.nts.ti.events.ClearTextEvent;
import com.nts.ti.events.ClickAtEvent;
import com.nts.ti.events.ClickEvent;
import com.nts.ti.events.DoubleClickEvent;
import com.nts.ti.events.EventTemplate;
import com.nts.ti.events.ExistsEvent;
import com.nts.ti.events.FlickEvent;
import com.nts.ti.events.GetElementCountEvent;
import com.nts.ti.events.GetLocationEvent;
import com.nts.ti.events.GetTextEvent;
import com.nts.ti.events.LongTapEvent;
import com.nts.ti.events.RightClickEvent;
import com.nts.ti.events.SelectEvent;
import com.nts.ti.events.SendKeyEvent;
import com.nts.ti.events.SingleTapEvent;
import com.nts.ti.exception.ElementNotFoundException;

/**
 * @author  NHN
 */
public abstract class CommonFixture implements CommonEvents {

	Map<String, RemoteWebDriver> devices = new HashMap<String, RemoteWebDriver>();
	RemoteWebDriver driver;

	EventTemplate template;
	List<WebElement> elementList;
	
	
	public List<WebElement> getElementList() {
		return elementList;
	}

	public void setElementList(List<WebElement> elementList) {
		this.elementList = elementList;
	}

	String XPATH_PATTERN = "^/?/(\\w+)\\[([\\W|\\w]+)='((\\W|\\w)+)']";

	public boolean needScreenShot = false;
	final static String SEPERATOR = File.separator;

	public String currentDriver = "NATIVE_APP";

	int TIMEOUT = 5000;
	int RETRY_INTERVAL = 50;

	int OFFSET_X = 0;
	int OFFSET_Y = 0;

	public static Map<String, CharSequence> keySeq = new HashMap<String, CharSequence>();
	static {
		for (Field f : SelendroidKeys.class.getDeclaredFields()) {
			try {
				keySeq.put(f.getName().toLowerCase(), (CharSequence)f.get(f.getName()));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public CommonFixture() {
		template = new EventTemplate();
	}

	/**
	 * element를 찾는 최대시간을 설정하는 메소드
	 * @param timeOut element 찾는 최대 시간 (단위: ms)
	 */
	public void setTimeOut(int timeOut) {
		this.TIMEOUT = timeOut;
	}

	/**
	 * 위치 기반 click evnet 발생 시 전체적인 offset을 지정할 수 있음.
	 * @param posX
	 * @param posY
	 */
	public void setOffset(int posX, int posY) {
		this.OFFSET_X = posX;
		this.OFFSET_Y = posY;
	}

	/**
	 * element 찾기 실패 시, 다음 시도까지의 시간을 설정하는 메소드
	 * 
	 * @param retryInterval element 찾기 실패 시 다음 시도까지의 시간 간격  
	 */
	public void setRetryInterval(int retryInterval) {
		this.RETRY_INTERVAL = retryInterval;
	}
	
	/**
	 * 수행중인 테스트를 중단시키는 메소드
	 */
	public void stopTest() {
		try {
			FitNesseUtil.stopFitnesse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 화면 캡쳐(수행하는 모든 메소드 전/후)를 활성화 함
	 */
	public void captureOn() {
		needScreenShot = true;
	}

	/**
	 * 화면 캡쳐(수행하는 모든 메소드 전/후)를 비활성화 함
	 */
	public void captureOff() {
		needScreenShot = false;
	}

	/**
	 * 현재 화면을 스크린샷으로 찍어 저장하고, 출력을 위한 HTML태그를 가져오는 메소드
	 * 
	 * @param message 저장 될 파일이름
	 * @return 스크린샷 파일의 출력을 위한 HTML 태그를 return
	 * @throws Exception
	 */
	public String screenShot(String message) throws Exception {
		return screenShot(null, null, message);
	}
	
	public String screenShot(String Serial, String message) throws Exception {
		return screenShot(null, Serial, message);
	}

	/** 
	 * 현재 화면을 스크린샷으로 찍어 저장하고, 출력을 위한 HTML태그를 가져오는 메소드
	 * (capture on 상태라면 각 이벤트 발생 전, 후로 화면을 스크린샷 찍음)
	 * 
	 * @param when 이벤트 발생 전 - before, 후 - after
	 * @param message 저장 될 파일이름
	 * @return 스크린샷 파일의 출력을 위한 HTML 태그를 return
	 * @throws IOException
	 * 
	 */
	public String screenShot(String when, String serial, String message) throws Exception {

		File screenshot;
		
		if(currentDriver.equals("WEBVIEW")){ 
			screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		} else {
			try{
				screenshot = ((SelendroidDriver)driver).getScreenshotAs(OutputType.FILE);
			}catch(Exception e){
				screenshot = ((SelendroidDriver)driver).getScreenshotAs(OutputType.FILE);
			}
		}
		
		File directory = new File(".");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String timeStamp = sdf.format(Calendar.getInstance().getTime());
		String dateDir = timeStamp.substring(0, 10);

		String nameScreenshot;
		String newFileNamePath;
		String defaultFilePath = directory.getCanonicalPath() + SEPERATOR + "FitNesseRoot" + SEPERATOR + "files" + SEPERATOR + 
				"screenShots" + SEPERATOR + dateDir + SEPERATOR;

		if (when != null) {
			nameScreenshot = timeStamp + "_" + when + "_" + message + ".png";
			if(when.equals("exception")){
				newFileNamePath = defaultFilePath + "exception"	+ SEPERATOR + nameScreenshot;
			} else {
				newFileNamePath = defaultFilePath + "history" + SEPERATOR + nameScreenshot;			
			}
		} else {
			nameScreenshot = timeStamp + "_" + message + ".png";
			newFileNamePath = defaultFilePath + nameScreenshot;
		}

		FileUtils.copyFile(screenshot, new File(newFileNamePath));

		String fitServer = "";
		if(null!=FitServer.getHost() && !FitServer.getHost().isEmpty()){
			if(FitServer.getPort()==80){
				fitServer = "http://"+FitServer.getIp();
			}else{
				fitServer = "http://"+FitServer.getIp()+":"+FitServer.getPort();
			}
		}

		String linkImg = fitServer + "/files/screenShots/" + dateDir + "/";
		if(when == null){
			return "<a href='"+ linkImg + nameScreenshot + "' target='_blank'><img src='"+ linkImg + nameScreenshot + "' width=300></a>";			
		} else if(when.equals("exception")) {
			return "<a href='"+ linkImg + "exception/" + nameScreenshot + "' target='_blank'><img src='"+ linkImg + "exception/" + nameScreenshot + "' width=300></a>";
		} else {
			return "<a href='"+ linkImg + "history/" + nameScreenshot + "' target='_blank'><img src='"+ linkImg + "history/" + nameScreenshot + "' width=300></a>";
		}
	}

	/**
	 * element(index = 0)가 화면에 존재하는지 확인하는 메소드
	 * @param resourceName : element의 id, value 또는 tag name
	 * @return : 존재하면 true, 아니면 false
	 */
	public boolean exists(String resourceName) throws Exception {
		return exists(resourceName, 0);
	}

	/**
	 * 해당 index의 element가 화면에 존재하는지 확인하는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @return 존재하면 true, 아니면 false return
	 */
	public boolean exists(String resourceName, int index) throws Exception {
		return (Boolean)template.executeWithReturnValue(elementList, index, new ExistsEvent());	
	}

	/**
	 * 요소의 위치 좌표(x, y)를 출력함.
	 * @param resourceName
	 * @return
	 * @throws Exception
	 */
	public String getLocation(String resourceName) throws Exception {
		return getLocation(resourceName, 0);
	}

	/**
	 * 요소의 위치 좌표(x, y)를 출력함.
	 * @param resourceName
	 * @param index
	 * @return
	 * @throws Exception
	 */
	public String getLocation(String resourceName, int index) throws Exception {
		return (String)template.executeWithReturnValue(elementList, index, new GetLocationEvent());
	}

	/**
	 * element(index = 0)를 click하는 메소드 
	 * 
	 * @param resouceName element의 id, value 또는 tag name
	 * @throws Throwable 
	 */
	@CellIgnoreFirst
	public void click(String resourceName) throws Exception {
		click(resourceName, 0);
	}

	/**
	 * 해당 index의 element를 click하는 메소드
	 * 
	 * @param resouceName element의 id, value 또는 tag name
	 * @param index 해당 element에 대한 index
	 * @throws Throwable 
	 */
	@CellIgnoreFirst
	public void click(String resourceName, int index) throws Exception {
		template.execute(elementList, index, new ClickEvent(driver));
	}

	/**
	 * offset을 반영한 위치에 click event 발생
	 * @param resourceName
	 * @throws Exception
	 */
	public void clickAt(String resourceName) throws Exception {
		clickAt(resourceName, 0);
	}

	/**
	 * @see clickAt
	 * @param resourceName
	 * @param index
	 * @throws Exception
	 */
	public void clickAt(String resourceName, int index) throws Exception {
		template.execute(elementList, index, new ClickAtEvent(driver, OFFSET_X, OFFSET_Y));
	}

	@CellIgnoreFirst
	public void doubleClick(String resourceName) throws Exception{
		doubleClick(resourceName, 0);
	}
	
	@CellIgnoreFirst
	public void doubleClick(String resourceName, int index) throws Exception{
		template.execute(elementList, index, new DoubleClickEvent(driver));
	}
	
	/**
	 * 단말기에 HOME, MENU 키등을 요청
	 * 
	 * @param keys "home", "back", 등
	 * @throws Exception
	 */
	public void sendKey(String keys) throws Exception {
		sendKey("", 0, keys);
	}

	/**
	 * element(EditText, index = 0)에 값을 입력하는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @param keys 입력하려는 문자열
	 * @throws Exception (해당 element를 찾지 못했을 때 예외 발생)
	 */
	@CellIgnoreFirst
	public void sendKey(String resourceName, String keys) throws Exception {
		sendKey(resourceName, 0, keys);
	}

	/**
	 * 해당 인덱스의 element(EditText)에 값을 입력하는 메서드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @param keys 입력하려는 문자열
	 * @param index 해당 element에 대한 index
	 * @throws Exception (해당 element를 찾지 못했을 때 예외 발생)
	 */
	@CellIgnoreFirst
	public void sendKey(String resourceName, int index, String keys) throws Exception {	
		template.execute(elementList, index, new SendKeyEvent(driver, keySeq, keys, currentDriver));
	}

	/**
	 * element(EditText, index = 0)의 text(value)를 지워주는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @throws Exception (해당 element를 찾지 못했을 때 예외 발생)
	 */
	@CellIgnoreFirst
	public void clearText(String resourceName) throws Exception {
		clearText(resourceName, 0);
	}

	/**
	 * 해당 index의 element(EditText)의 text(value)를 지워주는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @param index 해당 element에 대한 index
	 * @throws Exception (해당 element를 찾지 못했을 때 예외 발생)
	 */
	@CellIgnoreFirst
	public void clearText(String resourceName, int index) throws Exception {
		template.execute(elementList, index, new ClearTextEvent());
	}

	public void close() {
		closeBrowser();
	}

	/**
	 * element(index = 0)의 text(value)를 return하는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @return element의 text(value)를 return
	 * @throws Exception (해당 element를 찾지 못했을 때 예외 발생)
	 */
	public String getText(String resourceName) throws Exception {
		return getText(resourceName, 0);
	}

	/**
	 * 해당 index의 element의 text(value)를 return하는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @param index 해당 element에 대한 index
	 * @return element의 text(value)를 return
	 * @throws Exception (해당 element를 찾지 못했을 때 예외 발생)
	 */
	public String getText(String resourceName, int index) throws Exception {
		
		return (String)template.executeWithReturnValue(elementList, index, new GetTextEvent(driver));
	}

	/**
	 * 리소스의 갯수를 출력(isDisplayed 상태인 것들 만)
	 * @param resourceName
	 * @return
	 * @throws Exception 
	 */
	public int getElementCount(String resourceName) {
		try {
			return (Integer)template.executeWithReturnValue(elementList, 0, new GetElementCountEvent());
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * element(index = 0)의 중점을 한번 탭하는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @throws Exception (해당 element를 찾지 못했을 때 예외 발생)
	 */
	@CellIgnoreFirst
	public void singleTap(String resourceName) throws Exception {
		singleTap(resourceName, 0);
	}

	/**
	 * 해당 index의 element의 중점을 한번 탭하는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @param index 해당 element에 대한 index
	 * @throws Exception (해당 element를 찾지 못했을 때 예외 발생)
	 */
	@CellIgnoreFirst
	public void singleTap(String resourceName, int index) throws Exception {
		template.execute(elementList, index, new SingleTapEvent(driver));
	}

	/**
	 * element(index = 0)를 길게 tap하는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @throws Exception (해당 element를 찾지 못했을 때 예외 발생)
	 */
	@CellIgnoreFirst
	public void longTap(String resourceName) throws Exception {
		longTap(resourceName, 0);
	}

	/**
	 * 해당 index의 element를 길게 tap하는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @param index 해당 element에 대한 index
	 * @throws Exception (해당 element를 찾지 못했을 때 예외 발생)
	 */
	@CellIgnoreFirst
	public void longTap(String resourceName, int index) throws Exception {
		template.execute(elementList, index, new LongTapEvent(driver));
	}

	/**
	 * element(index = 0)를 기준으로 flick하는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @param direction 방향(destination) : left/-, right/+
	 * @throws Throwable
	 */
	public void flick(String resourceName, String direction) throws Throwable {
		flick(resourceName, 0, direction);
	}

	/**
	 * 해당 index의 element를 기준으로 flick하는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @param index 해당 element에 대한 index
	 * @param direction 방향(destination) : left/-, right/+
	 * @throws Throwable
	 */
	@CellIgnoreFirst
	public void flick(String resourceName, int index, String direction) throws Throwable {
		if (!direction.isEmpty()) {
			if (direction.matches("(([\\/-]?)\\d{1,10})")) {
				flick(resourceName, index, Integer.parseInt(direction), 0, 1);
			} else {
				if (direction.equalsIgnoreCase("left")) {
					flick(resourceName, index, -300, 0, 1);
				} else if (direction.equalsIgnoreCase("right")) {
					flick(resourceName, index, 300, 0, 1);
				}
			}
		} else {
			flick(resourceName, index, -300, 0, 1);
		}
	}

	/**
	 * element(index = 0)를 기준으로 스크롤
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @param direction 방향(화면 기준) : down/-, up/+
	 * @throws Throwable
	 */
	@CellIgnoreFirst
	public void scroll(String resourceName, String direction) throws Throwable {
		scroll(resourceName, 0, direction);
	}

	/**
	 * 해당 index의 element를 기준으로 스크롤
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @param direction 방향(화면 기준) : down/-, up/+
	 * @param index 해당 element에 대한 index
	 * @throws Throwable
	 */
	@CellIgnoreFirst
	public void scroll(String resourceName, int index, String direction) throws Throwable {
		if (!direction.isEmpty()) {
			if (direction.contains("+")) {
				direction = direction.replace("+", "");
			}
			
			if (direction.matches("(([\\/-]?)\\d{1,10})")) {
				flick(resourceName, index, 0, Integer.parseInt(direction), 1);
			} else {
				if (direction.equalsIgnoreCase("down") || direction.equalsIgnoreCase("dn")) {
					flick(resourceName, index, 0, -300, 1);
				} else if (direction.equalsIgnoreCase("up")) {
					flick(resourceName, index, 0, 300, 1);
				}
			}
		} else {
			flick(resourceName, index, 0, -500, 1);
		}
	}

	/**
	 * 해당 index의 element를 기준으로 xWay, yWay의 벡터합 방향으로 flick하는 메소드
	 * 
	 * @param resourceName element의 id, value 또는 tag name
	 * @param xWay 좌:음수, 우:양수
	 * @param yWay 상:음수, 하:양수
	 * @param speed 0 - Time in milliseconds to provide a speed similar to normal flick
	 *              1 - Time in milliseconds to provide a speed similar to fast flick
	 * @throws Throwable 
	 */
	@CellIgnoreFirst
	public void flick(String resourceName, int index, int xWay, int yWay, int speed) throws Throwable {
		template.execute(elementList, index, new FlickEvent(driver, xWay, yWay, speed));
	}

	/**
	 * 해당 셀렉트박스에서 선택 함(인덱스 기준)
	 * @param xpath 요소(셀렉트박스)의 경로
	 * @param optIdx 셀렉트박스 내 옵션의 인덱스
	 * @throws Exception
	 */
	public void select(String resourceName, int optIdx) throws Exception {
		select(resourceName, 0, optIdx);
	}

	/**
	 * 해당 셀렉트박스에서 선택 함(셀렉트박스 인덱스, 옵션인덱스 기준)
	 * @param xpath 요소(셀렉트박스)이 경로
	 * @param idx 셀렉트박스의 인덱스번호
	 * @param optIdx 셀렉트박스 내 옵션의 값
	 * @throws Exception
	 */
	public void select(String resourceName, int index, int optIdx) throws Exception {
		template.execute(elementList, index, new SelectEvent(optIdx));
	}

	/**
	 * 해당 URL로 이동 함
	 * @param url 접근할 URL
	 */
	public void goToUrl(String url) {
		try {
			driver.navigate().to(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 브라우저 뒤로가기 동작함 
	 */
	public void goBack() {
		try {
			driver.navigate().back();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 브라우저 앞으로 가기 동작함
	 */
	public void goForward() {
		try {
			driver.navigate().forward();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 브라우저 새로고침
	 */
	public void refresh(){
		try {
			driver.navigate().refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 브라우저를 종료 함
	 */
	public void closeBrowser() {
		try {
			driver.quit();

			String[] serials = new String[devices.size()];
			devices.keySet().toArray(serials);
			for(String serial:serials){
				if(driver.equals(devices.get(serial))){
					devices.remove(serial);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void elapsedTime(long t1) {
		FitServer.setSymbol("$elapsed$", (System.currentTimeMillis() - t1));
	}

	
	//TODO : iOS 고려해서 작성한 부분 제외시키기
	/**
	 * 현재 화면에 노출된 element에 대한 tag name, id, value를 print하는 메소드  
	 *  
	 * @return 현재 화면에 노출된 element들의 tag name, id, value를 return
	 */
	public String printElement() {

		WebViewHelper.clearCache();
		NativeViewHelper.clearCache();
		if (currentDriver.equals("ANDROID")) {
			driver.switchTo().window("WEBVIEW");
			WebViewHelper.parsingHtml(driver.getPageSource());
		} else {
				if (!currentDriver.equals("NATIVE_APP")) {
					currentDriver = "NATIVE_APP";
				}
				driver.switchTo().window(currentDriver);
				String source = null;
				source = driver.getPageSource();
				NativeViewHelper.parsingXml(source);
				
				Object[] handle = driver.getWindowHandles().toArray();
				if(handle.length > 1){
					driver.switchTo().window("WEBVIEW");
					WebViewHelper.parsingHtml(driver.getPageSource());
					driver.switchTo().window(currentDriver);
				}
		}
		StringBuilder sb = new StringBuilder();
		sb.append(NativeViewHelper.printElement());
		sb.append(WebViewHelper.printElement());
		return sb.toString();
	}

	/**
	 * 현재 Url을 가져오는 메서드
	 */
	public String getCurrentUrl() throws Exception {
		return driver.getCurrentUrl();
	}

	/**
	 * 현재 페이지 title태그의 내용을 가져오는 메서드
	 */
	public String getTitle() throws Exception {
		return driver.getTitle();
	}

	/**
	 * 현재 페이지의 HTML소스를 출력 함
	 * @return HTML소스 
	 */
	public String getPageSource(){
		return driver.getPageSource();
	}

	public String downloadFile(String fileAddress, String downloadDir){
		return DownloadFileUrl.fileUrlDownload(fileAddress, downloadDir);
	}
	
	/**
	 * 현재 시각 및 날짜를 출력
	 * @return 2013-12-01 일 00:02:59 형식으로 출력
	 */
	public String getDate(){
		Date date = new Date();
		SimpleDateFormat format2 = new SimpleDateFormat("YYYY-MM-dd E HH:mm:ss", Locale.KOREA);
		return format2.format(date);
	}
	
	/**
	 * 두 문자열이 서로 포함 관계인지 확인
	 * @param text1 첫 번째 문자열
	 * @param text2 두 번째 문자열
	 * @return 포함 관계에 있으면 true, 아니면 false
	 */
	public boolean containsString(String text1, String text2){
		return text1.contains(text2) || text2.contains(text1);
	}
	
	/**
	 * 입력받은 문자열을 정수(Integer)로 바꿔줌
	 * @param text 정수형으로 바꾸고 싶은 문자열
	 * @return text를 int형으로 바꾼 값
	 */
	public int changeToInteger(String text){
		return Integer.valueOf(text);
	}
	
	
	//TODO : 입력받은 두 수가 문자열일 수도 있고 changeToInteger를 이용해 바꾼 int형 일 수도 있음. long이나 double이면 어떡하지???
	/**
	 * 입력받은 두 숫자를 더함
	 * @param num1
	 * @param num2
	 * @return num1 + num2
	 */
	public int sum(String num1, String num2){
		int number1 = Integer.valueOf(num1);
		int number2 = Integer.valueOf(num2);
		return number1 + number2;
	}
	
	public int sum(int num1, int num2){
		return num1 + num2;
	}
}
