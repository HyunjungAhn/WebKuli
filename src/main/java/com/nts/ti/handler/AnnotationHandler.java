package com.nts.ti.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.nts.ti.AndroidFixture;
import com.nts.ti.CommonFixture;
import com.nts.ti.annotation.FindResource;
import com.nts.ti.annotation.FindSimilarResource;
import com.nts.ti.annotation.ScreenShot;
import com.nts.ti.exception.ElementNotFoundException;
import com.nts.ti.exception.WebKuliException;

import fit.FitServer;

public class AnnotationHandler implements InvocationHandler {

	CommonFixture fixture;

	String XPATH_PATTERN = "^/?/(\\w+)\\[([\\W|\\w]+)='((\\W|\\w)+)']";

	private static final Logger log = Logger.getLogger(AnnotationHandler.class.getName());

	public AnnotationHandler(CommonFixture fixture){
		this.fixture = fixture;
	}

	/**
	 * Fixture에 메서드를 실행하기전에 Hooking하여
	 * FindResource 어노테이션이 적용된 메서드를 구분한뒤
	 * findResource 메서드를 수행한후 Fixture의 메서드를 수행하게하는
	 * 프록시 패턴의 메서드
	 * @throws Exception 
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Exception{		
		Object retVal = null;
		fixture.setElementList(null);
		try {		
			if(args != null){
				if(fixture instanceof AndroidFixture && (args[0].toString().equals("") || CommonFixture.keySeq.containsKey(args[0].toString().toLowerCase()))){
					return method.invoke(fixture, args);
				}
				
				if(fixture instanceof AndroidFixture){
					if(method.isAnnotationPresent(FindResource.class)){
						String resourceName = args[0].toString();
						int index = 0;
						if(args.length > 1 && args[1] instanceof Integer){
							index = (Integer)args[1];
						} else if(method.getName().equals("getElementCount")){
							index = 1;
						}
						fixture.findResource(resourceName, index);						
					}
				}else{	
					if(method.isAnnotationPresent(FindResource.class)){
						String resourceName = args[0].toString();
						int index = 0;
						if(args.length > 1 && args[1] instanceof Integer){
							index = (Integer)args[1];
						} else if(method.getName().equals("getElementCount")){
							index = 1;
						}
						fixture.findResource(resourceName, index);						
					}
				}
			}

			if(method.isAnnotationPresent(ScreenShot.class) && fixture.needScreenShot){
				fixture.screenShot("before", null, method.getName() + "_" + args[0].toString());
			}

			retVal = method.invoke(fixture, args);
			String resourceName = "";
			if(args != null){
				resourceName = args[0].toString();
			}

			log.info(method.getName()+"::"+resourceName+"::"+FitServer.getSymbol("$elapsed$"));
			
			if(method.isAnnotationPresent(ScreenShot.class) && fixture.needScreenShot){
				fixture.screenShot("after", null, method.getName() + "_" + args[0].toString());
			}
		} catch (Exception e) {	
			if(e.getCause() != null){
				if(e.getCause().toString().contains("Element fail to find")){
					String screenshot = null;
					screenshot = fixture.screenShot("exception", null, method.getName() + "_" + args[0].toString());
					throw new ElementNotFoundException("'"+args[0]+"'을(를) 화면에서 찾을 수 없습니다.", screenshot);
				}else{
					System.out.println("예외 발생");
					e.printStackTrace();
				}
			}else{
				String screenshot = null;
				screenshot = fixture.screenShot("exception", null, method.getName() + "_" + args[0].toString());
				throw new WebKuliException("테스트앱과 서버와의 연결이 끊겼을 수 있습니다.<br>instrument 후 테스트 수행해주세요.", screenshot);
			}
		}
		
		return retVal;
	}
}
