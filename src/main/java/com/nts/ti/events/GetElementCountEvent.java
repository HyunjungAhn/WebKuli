package com.nts.ti.events;

import java.util.List;

import org.openqa.selenium.WebElement;

public class GetElementCountEvent implements EventStrategy {

	public void execute(List<WebElement> element, int index) {
	}

	public Object executeWithReturnValue(List<WebElement> element, int index) {
		if(null!=element){
			return element.size();
		}
		return 0;
	}

}
