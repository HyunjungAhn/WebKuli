package com.nts.ti.exception;

/**
 * 엘리먼트를 찾지 못했을때 작용하는 Exception클래스
 * 
 * @author NHN
 *
 */
public class ElementNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = -3664720842897875951L;	
	
	public ElementNotFoundException(String message){
		super(message, null, false, false);
	}
	
	public ElementNotFoundException(String message, String screenshot){
		super(message + "<p>" + screenshot, null, false, false);
	}
}
