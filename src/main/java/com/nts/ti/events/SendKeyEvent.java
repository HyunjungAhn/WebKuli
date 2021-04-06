package com.nts.ti.events;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;


public class SendKeyEvent implements EventStrategy {

	RemoteWebDriver driver;
	Map<String, CharSequence> keySeq;
	String keys;
	String currentView;

	public SendKeyEvent(RemoteWebDriver driver, Map<String, CharSequence> keySeq, String keys, String currentView) {
		super();
		this.driver = driver;
		this.keySeq = keySeq;
		this.keys = keys;
		this.currentView = currentView;
	}

	public void execute(List<WebElement> element, int index) {

		//		if(element==null && keySeq.containsKey(keys.toLowerCase())){
		if(keySeq.containsKey(keys.toLowerCase())){
			Actions action = new Actions(driver);
			CharSequence key = keySeq.get(keys.toLowerCase()); 
			action.sendKeys(key).perform();
			return;
		}

		if(element!=null){
			element.get(index).sendKeys(keys);
		}
	}

	public String executeWithReturnValue(List<WebElement> element, int index) {
		return null;
	}

}
