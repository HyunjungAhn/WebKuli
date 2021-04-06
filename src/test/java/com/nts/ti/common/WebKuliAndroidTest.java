package com.nts.ti.common;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.nts.ti.WebKuliFixture;

public class WebKuliAndroidTest {
	WebKuliFixture wf;
	
	@Before
	public void setUp() throws Exception {
		wf = new WebKuliFixture();
	}
	
	@Ignore
	@Test
	public void instrumentationTest() throws Exception{
		wf.selectDriver("ANDROID");
//		wf.launchApp("7597b295", "C:\\WebKuli\\apk\\NaverSearch_5.1.0.1.apk");
		wf.instrument("7597b295", "C:\\WebKuli\\apk\\NaverSearch_5.1.0.1.apk");
		wf.addDevice("7597b295", "localhost", 9090);
		
		System.out.println("11111111111111");
		wf.click("search_edit_button");
		System.out.println("22222222222222");
		wf.sendKey("search_window_edit", "hello");
		System.out.println("333333333333333");
		wf.click("search_window_search_button");
		System.out.println("444444444444444");
		wf.printElement();
		System.out.println("55555555555555555");
		wf.hasText("hello");
		System.out.println("66666666666666666");
		
	}
	
	@Ignore
	@Test
	public void lineTest() throws Exception{
		wf.selectDriver("ANDROID");
		wf.launchApp("7597b295", "C:\\WebKuli\\apk\\line-3.9.4-beta-20131112140912.apk");
		wf.instrument("7597b295", "C:\\WebKuli\\apk\\line-3.9.4-beta-20131112140912.apk");
		wf.addDevice("7597b295", "localhost", 9090);

		
	}

	@Test
	public void getDateTest() throws Exception{
		wf.selectDriver("web");
		wf.setDriverPath("C:\\WebDriver");
//		wf.setBrowser("ie");
		System.out.println(wf.getDate());
		
		System.out.println(wf.containsString("2013-12-01 일 00:02:59", "일"));
		System.out.println(wf.containsString("2013-12-01 일 00:02:59", "월"));
		System.out.println(wf.containsString("일", "2013-12-01 일 00:02:59"));
		System.out.println(wf.containsString("월", "2013-12-01 일 00:02:59"));
		
		String str = Integer.toString(10);
		if(10 == wf.changeToInteger(str)){
			System.out.println("change to int");
		}
		
		
	
	}
}
