package com.nts.ti.events;

import java.util.List;
import org.openqa.selenium.WebElement;

/**
 * @author  NHN
 */
public class EventTemplate {		
		
	/**
	 * 이벤트를 수행할 element의 List와, 그에 해당하는 인덱스, 이벤트 전략을
	 * 파라메터로 받아 이벤트를 수행하는 메서드
	 * 리턴값이 없이 바로 화면에 이벤트를 수행한다
	 * 
	 * @param elements 찾아낸 element의 List
	 * @param index List 가운데에서 몇번째 element를 찾을건지에 관한 index
	 * @param strategy 이벤트 strategy
	 * @throws Exception
	 */
	public void execute(List<WebElement> elements, int index, EventStrategy strategy) throws Exception{
		
		if (elements != null && index < elements.size()) {
			strategy.execute(elements, index);
		} else if(strategy instanceof SendKeyEvent){
			strategy.execute(elements, index);
		}
		else {
			throw new Exception("Element fail to find");
		}
	}	
	
	/**
	 * 이벤트를 수행할 element의 List와, 그에 해당하는 인덱스, 이벤트 전략을
	 * 파라메터로 받아 이벤트를 수행하는 메서드
	 * getText, exists 등 '리턴값이 필요한 이벤트를 수행'할때 동작한다.
	 * 
	 * @param elements 찾아낸 element의 List
	 * @param index List 가운데에서 몇번째 element를 찾을건지에 관한 index
	 * @param strategy 이벤트 strategy
	 * @throws Exception
	 */
	public Object executeWithReturnValue(List<WebElement> elements, int index, EventStrategy strategy) throws Exception{		
		if (elements != null && index<elements.size()  
			/* exists이벤트는 요소를찾지못했을때 false를 리턴해야하므로 예외처리하지 않는다 */
			|| strategy instanceof ExistsEvent) {
			
			return strategy.executeWithReturnValue(elements, index); 
		} else {
			throw new Exception("Element fail to find");
		}
	}
}
