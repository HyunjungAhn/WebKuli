package com.nts.ti;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;

import com.nts.ti.common.ResourceManager;
import com.nts.ti.handler.AnnotationHandler;

/**
 * @author  NHN
 */
public class WebKuliFixture extends AbstractFixture {
	
	/**
	 * @uml.property  name="fixture"
	 * @uml.associationEnd  
	 */
	CommonEvents fixture;
	
	String driverName = "";
	
	/**
	 * 테스트할 OS 선택
	 * 
	 * @param driverName
	 * @throws Exception
	 */	
	public void selectDriver(String driverName) throws Exception{		
		this.driverName = driverName.substring(0,1).toUpperCase()+driverName.substring(1).toLowerCase();
		
		try {
			Class<?> fixtureClass = Class.forName("com.nts.ti."+this.driverName+"Fixture");			
			// 프록시 설정			
			fixture = (CommonEvents)Proxy.newProxyInstance(
									getClass().getClassLoader(), 
									new Class[]{CommonEvents.class}, 
									new AnnotationHandler((CommonFixture)fixtureClass.newInstance()));
			
		} catch (ClassNotFoundException e) {
			throw new Exception("not supported OS");
		}			
	}
	
	/**
	 * .properties 파일을 resources에 load하는 메소드 
	 * 
	 * @param resource properties 파일 이름
	 * @return resource load 성공 시 true, 그렇지 않으면 false return
	 */
	public void loadResource(String resource) {
		ResourceManager.loadKeyword(resource);
		ResourceManager.loadImage(resource);
	}
	
	/**
	 * resource name(id, tag name)을 원하는 키워드로 지정하는 메소드
	 * 
	 * @param resource 생성되는 properties 파일의 이름
	 * @param key 원하는 키워드
	 * @param value 지정하려는 resource name(id, tag name)
	 * @return 이미 존재 시 "key::(value) already exists!", value 변경 시 "key::(old_value) is changed to (new_value)!", 새로 추가 시 "key::(value) is added!" return
	 * @throws IOException, UnsupportedEncodingException 
	 */
	public String insertKeyword(String resource, String key, String value) throws UnsupportedEncodingException, IOException {
		return ResourceManager.insertKeyword(resource, key, value);
	}
	
	public void captureOn(){
		fixture.captureOn();
	}
	
	public void captureOff(){
		fixture.captureOff();
	}
	
	/**
	 * 환경 변수 값을 등록하는 메소드
	 * 
	 * @param envKey JAVA_HOME, ANDROID_HOME
	 * @param envValue 설정하려는 경로 값(path)
	 */
	public void setEnv(String envKey, String envValue) {
		System.setProperty(envKey, envValue);
	}
	
	public void setRetryInterval(int retryInterval){
		fixture.setRetryInterval(retryInterval);
	}
	
	public void setTimeOut(int timeOut){
		fixture.setTimeOut(timeOut);
	}
	
	public void addDevice(String deviceSerial, String url, int port) throws Exception{
		fixture.addDevice(deviceSerial, url, port);
	}
	
	public void addDevice(String deviceSerial, String url, int port, String appPath) throws Exception{
		fixture.addDevice(deviceSerial, url, port, appPath);
	}
	
	public void switchDevice(String deviceSerial) throws Exception{
		fixture.switchDevice(deviceSerial);
	}
	
	public void clearText(String resourceName) throws Exception{
		fixture.clearText(resourceName);
	}
	
	public void clearText(String resourceName, int index) throws Exception{
		fixture.clearText(resourceName, index);	
	}
	
	public void click(String resourceName) throws Exception{
		fixture.click(resourceName);
	}
	
	public void click(String resourceName, int index) throws Exception{
		fixture.click(resourceName, index);
	}
	
	public void clickAt(String resourceName) throws Exception{
		fixture.clickAt(resourceName);
	}
	
	public void clickAt(String resourceName, int index) throws Exception{
		fixture.clickAt(resourceName, index);	
	}
	
	public void close(){
		fixture.close();
	}
	
	public String deviceInfo(){
		return fixture.deviceInfo();
	}
	
	public boolean exists(String resourceName) throws Exception{
		return fixture.exists(resourceName);
	}
	
	public boolean exists(String resourceName, int index) throws Exception{
		return fixture.exists(resourceName, index);	
	}
	
	public void isDisappear(String resourceName, int retry){
		fixture.isDisappear(resourceName, retry);
	}

	public void isDisappear(String resourceName, int index, int retry){
		fixture.isDisappear(resourceName, index, retry);
	}
	
	public void flick(String resourceName, String direction) throws Throwable{
		fixture.flick(resourceName, direction);
	}
	
	public void flick(String resourceName, int index, String direction) throws Throwable{
		fixture.flick(resourceName, index, direction);
	}
	
	public String getDeviceList(){
		return fixture.getDeviceList();
	}
	
	public int getElementCount(String resourceName) throws Exception{
		return fixture.getElementCount(resourceName);
	}
	
	public String getLocation(String resourceName) throws Exception{
		return fixture.getLocation(resourceName);
	}
	
	public String getLocation(String resourceName, int index) throws Exception{
		return fixture.getLocation(resourceName, index);
	}
	
	public String getText(String resourceName) throws Exception{
		return fixture.getText(resourceName);
	}
	
	public String getText(String resourceName, int index) throws Exception{
		return fixture.getText(resourceName, index);
	}
	
	public boolean hasText(String compareText){
		return fixture.hasText(compareText);
	}
	
	public void longTap(String resourceName) throws Exception{
		fixture.longTap(resourceName);
	}
	
	public void longTap(String resourceName, int index) throws Exception{
		fixture.longTap(resourceName, index);
	}
	
	public String screenShot(String message) throws Exception{
		return fixture.screenShot(message);
	}
	
	public String screenShot(String serial, String message) throws Exception{
		return fixture.screenShot(serial, message);
	}

	public String adbScreenShot(String fileName) throws Exception {
		return fixture.adbScreenShot(fileName);
	}
	
	public String adbScreenShot(String serial, String fileName) throws Exception {
		return fixture.adbScreenShot(serial, fileName);
	}
	public void scroll(String resourceName, String direction) throws Throwable{
		fixture.scroll(resourceName, direction);
	}
	
	public void scroll(String resourceName, int index, String direction) throws Throwable{
		fixture.scroll(resourceName, index, direction);
	}
	
	public void sendKey(String keys) throws Exception{
		fixture.sendKey(keys);
	}
	
	public void sendKey(String resourceName, String keys) throws Exception{
		fixture.sendKey(resourceName, 0, keys);
	}
	
	public void sendKey(String resourceName, int index, String keys) throws Exception{
		fixture.sendKey(resourceName, index, keys);
	}
	
	public void singleTap(String resourceName) throws Exception{
		fixture.singleTap(resourceName);
	}
	
	public void singleTap(String resourceName, int index) throws Exception{
		fixture.singleTap(resourceName, index);
	}
	
	public void select(String resourceName, int optIdx) throws Exception{
		fixture.select(resourceName, 0);
	}
	
	public void select(String resourceName, int index, int optIdx) throws Exception{
		fixture.select(resourceName, index, optIdx);
	}
	
	public void setOrientation(String orientation){
		fixture.setOrientation(orientation);
	}
	
	public String printElement(){
		return fixture.printElement();
	}
	
	public void launchApp(String appPath){
		fixture.launchApp(appPath);		
	}
	
	public void launchApp(String serial, String appPath){
		fixture.launchApp(serial, appPath);		
	}
	
	public void updateApp(String appPath){
		fixture.updateApp(appPath);
	}
	
	public void updateApp(String serial, String appPath){
		fixture.updateApp(serial, appPath);
	}
	
	public void clearAppData(String appPath){
		fixture.clearAppData(appPath);
	}
	
	public void clearAppData(String serial, String appPath){
		fixture.clearAppData(serial, appPath);
	}
	
	public void reclaimApp(){
		fixture.reclaimApp();
	}
	
	public void reclaimApp(String serial){
		fixture.reclaimApp(serial);
	}
	
	public void forward(int port){
		fixture.forward(port);
	}
	
	public void forward(String serial, int port){
		fixture.forward(serial, port);
	}
	
	public boolean isReady(){
		return fixture.isReady();
	}
	
	public void instrument(String appPath){
		fixture.instrument(appPath);
	}
	
	public void instrument(String serial, String appPath){
		fixture.instrument(serial, appPath);
	}
	
	public void startApp(String targetApkPath){
		fixture.startApp(targetApkPath);
	}
	
	public void startApp(String serial, String targetApkPath){
		fixture.startApp(serial, targetApkPath);
	}
	
	public void openBrowser(String url) throws Exception{
		fixture.openBrowser(url);
	}
	
	public void openBrowser(String serial, String host, int port, String url) throws Exception {
		fixture.openBrowser(serial, host, port, url);
	}
	
	public String getCurrentUrl() throws Exception {		
		return fixture.getCurrentUrl();
	}
	
	public String getTitle() throws Exception {
		return fixture.getTitle();
	}
	
	public void goToUrl(String url){
		fixture.goToUrl(url);
	}
	
	public void goBack(){
		fixture.goBack();
	}
	
	public void goForward(){
		fixture.goBack();
	}
	
	public void refresh(){
		fixture.refresh();
	}
	
	public void closeBrowser(){
		fixture.closeBrowser();
	}
	

	// WebFixture
	public void setBrowser(String browserName) {
		fixture.setBrowser(browserName);
	}

	public void setBrowser(String browserName, String host) {
		fixture.setBrowser(browserName, host);
	}


	public String encryptString(String txt) throws Exception {
		return fixture.encryptString(txt);
	}


	public void secureInputText(String xpath, String msg) throws Exception {
		fixture.secureInputText(xpath, msg);
	}


	public void clearPopup() {
		fixture.clearPopup();
		
	}

	public int countElements(String xpath) {
		return fixture.countElements(xpath);
	}
	
	public void printCookies(){
		fixture.printCookies();
	}
	
	public void selectValue(String xpath, String optValue) throws Exception{
		fixture.selectValue(xpath, optValue);
	}
	
	public void selectValue(String xpath, int idx, String optValue) throws Exception{
		fixture.selectValue(xpath, idx, optValue);
	}
	
	public boolean existsText(String text){
		return fixture.existsText(text);
	}
	
	public void alertCancel(){
		fixture.alertCancel();
	}
	
	public void alertOk(){
		fixture.alertOk();
	}
	
	public String getAlertText(){
		return fixture.getAlertText();
	}
	
	public String downloadFile(String url, String dir){
		return fixture.downloadFile(url, dir);
	}
	
	public String getPageSource(){
		return fixture.getPageSource();
	}
	
	public String getDate(){
		return fixture.getDate();
	}
	
	public boolean containsString(String text1, String text2){
		return fixture.containsString(text1, text2);
	}
	
	public int changeToInteger(String text){
		return fixture.changeToInteger(text);
	}
	
	public void setDriverPath(String driverPath){
		fixture.setDriverPath(driverPath);
	}
}
