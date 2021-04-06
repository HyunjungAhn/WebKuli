package com.nts.ti.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fit.FitServer;

public class WebViewHelper {

	private final static String[] TARGET_TAG = {"li", "span", "a", "img", "em", "strong", "input", "select","option"};
	private final static String[] TARGET_ATTRIBUTE = {"name","id","class","title","href", "type", "value"};
	private static Map<String, List<String>> cacheMap = new HashMap<String, List<String>>();
	private static Map<Integer, Boolean> contentCheck = new HashMap<Integer, Boolean>();
	private static int VIEW_COUNT = 0;
	
	public static void clearCache(){
		cacheMap = new HashMap<String, List<String>>();
		contentCheck = new HashMap<Integer, Boolean>();
		VIEW_COUNT=0;
	}
	
	public static int viewCount(){
		return VIEW_COUNT;
	}
	
	public static boolean hasContent(int index){
		return contentCheck.get(index);
	}
	
	public static boolean hasText(String text){
		String[] keyStrings = new String[cacheMap.size()];
		cacheMap.keySet().toArray(keyStrings);
		for(String keyString:keyStrings){
			for(String xpath:cacheMap.get(keyString)){
				if(xpath.contains(text))	return true;
			}
		}
		return false;
	}
	
	public static void parsingHtml(String source){
		Document doc = Jsoup.parse(source);
		if(doc.body().childNodeSize()>0){
			contentCheck.put(VIEW_COUNT, true);
		}else{
			contentCheck.put(VIEW_COUNT, false);
		}
		VIEW_COUNT++;
		for(String tag:TARGET_TAG){
			Elements elements = doc.getElementsByTag(tag);
			for(int i=0, cnt=elements.size();i<cnt;i++){
				Element element = elements.get(i); 
				for(String attr:TARGET_ATTRIBUTE){
					if(element.hasAttr(attr)){
						String xpath = "//"+tag+"[@"+attr+"='"+element.attr(attr)+"']";

						if(elements.get(i).hasText()){
							if(null!=FitServer.getHost()){
								xpath += ",//"+tag+"[text()='<span style='color:blue'>"+element.ownText()+"</span>'],full text:"+ element.text() + "";
							}else{
								xpath += ",//"+tag+"[text()='"+element.ownText()+"'],full text:"+ element.text() + "";
							}
						}
						
						if(cacheMap.containsKey(attr)){
							cacheMap.get(attr).add(xpath);
							break;
						}else{
							List<String> s = new ArrayList<String>();
							s.add(xpath);
							cacheMap.put(attr, s);
							break;
						}
					}
				}
			}
		}
	}
	
	public static String printElement(){
		StringBuilder sb = new StringBuilder();
		if(!cacheMap.isEmpty()){
			String[] attrValue = new String[cacheMap.size()];
			cacheMap.keySet().toArray(attrValue);
			Arrays.sort(attrValue);
			for(String attr:attrValue){
				List<String> xpathList = cacheMap.get(attr);
				String[] pathz = new String[xpathList.size()];
				xpathList.toArray(pathz);
				Arrays.sort(pathz);
				for(String xpath:pathz){
					if(null!=FitServer.getHost()){
						sb.append("</br>"+xpath);
					}else{
						sb.append(xpath+"\n");
					}
				}
			}
		}
		return sb.toString();
	}
}
