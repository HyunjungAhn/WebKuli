package com.nts.ti.events;

import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.touch.TouchActions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SingleTapEvent implements EventStrategy {

	RemoteWebDriver driver;

	public SingleTapEvent(RemoteWebDriver driver) {
		this.driver = driver;
	}

	public void execute(List<WebElement> element, int index) {
		TouchActions action = new TouchActions(driver).singleTap(element.get(index));
		action.perform();
	}

	public String executeWithReturnValue(List<WebElement> element, int index) {
		return null;
	}

}
