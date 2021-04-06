package com.nts.ti.events;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.touch.TouchActions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class FlickEvent implements EventStrategy {

	private RemoteWebDriver driver;

	private int xWay;
	private int yWay;
	private int speed;

	public FlickEvent(RemoteWebDriver driver, int xWay, int yWay, int speed) {
		this.driver = driver;
		this.xWay = xWay;
		this.yWay = yWay;
		this.speed = speed;
	}

	public void execute(List<WebElement> element, int index) {
		new TouchActions(driver).flick(element.get(index), xWay, yWay, speed).perform();
	}


	public String executeWithReturnValue(List<WebElement> element, int index) {
		return null;
	}

}
