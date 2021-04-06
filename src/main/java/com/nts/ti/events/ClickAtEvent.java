package com.nts.ti.events;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.touch.TouchActions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ClickAtEvent implements EventStrategy{

	RemoteWebDriver driver;

	private int OFFSET_X = 0;
	private int OFFSET_Y = 0;

	public ClickAtEvent(RemoteWebDriver driver, int x, int y) {
		this.driver = driver;
		this.OFFSET_X = x;
		this.OFFSET_Y = y;
	}

	public void execute(List<WebElement> element, int index) {
		TouchActions touch = new TouchActions(driver);
		int posX = element.get(index).getLocation().x+element.get(index).getSize().width/2+OFFSET_X;
		int posY = element.get(index).getLocation().y+element.get(index).getSize().height/2+OFFSET_Y;
		touch.down(posX, posY).up(posX,posY).perform();
	}

	public String executeWithReturnValue(List<WebElement> element, int index) {
		return null;
	}

}
