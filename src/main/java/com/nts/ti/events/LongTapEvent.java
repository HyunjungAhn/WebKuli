package com.nts.ti.events;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.touch.TouchActions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class LongTapEvent implements EventStrategy {

	RemoteWebDriver driver;

	public LongTapEvent(RemoteWebDriver driver) {
		this.driver = driver;
	}

	public void execute(List<WebElement> element, int index) {
		new TouchActions(driver).longPress(element.get(index)).perform();
	}

	public String executeWithReturnValue(List<WebElement> element, int index) {
		return null;
	}

}
