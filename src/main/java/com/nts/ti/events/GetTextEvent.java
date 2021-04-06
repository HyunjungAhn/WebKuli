package com.nts.ti.events;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class GetTextEvent implements EventStrategy {

	RemoteWebDriver driver;
	
	public GetTextEvent(RemoteWebDriver driver){
		this.driver = driver;
	}
	
	public void execute(List<WebElement> element, int index) {
	}

	public String executeWithReturnValue(List<WebElement> element, int index) {
		return element.get(index).getText();
	}
}
