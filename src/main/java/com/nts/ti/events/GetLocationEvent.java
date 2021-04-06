package com.nts.ti.events;

import java.util.List;
import org.openqa.selenium.WebElement;

public class GetLocationEvent implements EventStrategy {

	public void execute(List<WebElement> element, int index) {
	}

	public Object executeWithReturnValue(List<WebElement> element, int index) {
		if (element != null && index<element.size()) {
			return element.get(index).getLocation().toString();
		}
		return "";
	}

}
