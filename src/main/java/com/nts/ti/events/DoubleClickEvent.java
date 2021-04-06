package com.nts.ti.events;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DoubleClickEvent implements EventStrategy{

	RemoteWebDriver driver;
	
	public DoubleClickEvent(RemoteWebDriver driver){
		this.driver = driver;
	}
	
	public void execute(List<WebElement> element, int index) {
		Actions builder = new Actions(driver);
		Action doubleClick = builder.doubleClick(element.get(index)).build();
		doubleClick.perform();		
	}

	public Object executeWithReturnValue(List<WebElement> element, int index) {
		return null;
	}

}
