package com.nts.ti.events;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ClickEvent implements EventStrategy{

	RemoteWebDriver driver;

	public ClickEvent(RemoteWebDriver driver){
		this.driver = driver;
	}

	public void execute(List<WebElement> element, int index) {
		element.get(index).click();
	}

	public String executeWithReturnValue(List<WebElement> element, int index) {
		return null;
	}

}
