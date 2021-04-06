package com.nts.ti.events;

import java.util.List;

import org.openqa.selenium.WebElement;

public class ClearTextEvent implements EventStrategy {

	public void execute(List<WebElement> element, int index) {
		element.get(index).clear();
	}

	public String executeWithReturnValue(List<WebElement> element, int index) {
		return null;
	}

}
