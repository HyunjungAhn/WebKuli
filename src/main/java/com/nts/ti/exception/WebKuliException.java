package com.nts.ti.exception;

public class WebKuliException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WebKuliException(String message){
		super(message, null, false, false);
	}
	
	public WebKuliException(String message, String screenshot){
		super(message + "<p>" + screenshot, null, false, false);
	}
	
}
