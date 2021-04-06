package com.nts.ti.events;

import java.util.List;

import org.openqa.selenium.WebElement;

public interface EventStrategy {
	public void execute(List<WebElement> element, int index);
	public Object executeWithReturnValue(List<WebElement> element, int index);
}
