package com.nts.ti.events;

import java.util.List;

import org.openqa.selenium.WebElement;

public class ExistsEvent implements EventStrategy {
	
	public ExistsEvent(){

	}
	
	public void execute(List<WebElement> element, int index) {
	}

	public Object executeWithReturnValue(List<WebElement> element, int index) {
		if (element != null && index<element.size()) {
			return true;
		} else {
			return false;
		}
	}
}
