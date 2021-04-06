package com.nts.ti.events;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class SelectEvent implements EventStrategy{

	int optIdx;

	public SelectEvent(int optIdx){
		this.optIdx = optIdx;
	}

	public void execute(List<WebElement> element, int index) {
		new Select(element.get(index)).selectByIndex(optIdx);		
	}

	public Object executeWithReturnValue(List<WebElement> element, int index) {
		return null;
	}

}
