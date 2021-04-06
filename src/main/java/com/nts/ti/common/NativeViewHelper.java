package com.nts.ti.common;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fit.FitServer;

public class NativeViewHelper {

	private static Map<String, Integer> indexMap = new TreeMap<String, Integer>();
	private static Map<String, String> typeMap = new TreeMap<String, String>();
	private static List<String> elementsList = new ArrayList<String>();
	
	public static void clearCache(){
		typeMap = new TreeMap<String, String>();
		indexMap = new TreeMap<String, Integer>();
		elementsList = new ArrayList<String>();
	}
	
	public static boolean hasElement(String find){
		return typeMap.containsKey(find);
	}
	
	public static Map<String, String> getTypeMap(){
		return typeMap;
	}
	
	private static NodeList tmpNodeList(String xml, String expression){

		InputSource is = new InputSource(new StringReader(xml));
        Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		
        XPath xPath = XPathFactory.newInstance().newXPath();

        NodeList nodeList = null;
	
        try {
          nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {

        }
		
		return nodeList;
	}
	
	public static void parsingXml(String xml){
		
		NodeList nodeList = null;
		
		nodeList = tmpNodeList(xml, "//*[@id]");	
        if (nodeList != null && nodeList.getLength() > 0) {
          for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

			String type = node.getNodeName().toString();
			String id = node.getAttributes().getNamedItem("id").getNodeValue();
			String value = node.getAttributes().getNamedItem("value").getNodeValue();
			String name = node.getAttributes().getNamedItem("name").getNodeValue();
			String label = node.getAttributes().getNamedItem("label").getNodeValue();
			String shown = node.getAttributes().getNamedItem("shown").getNodeValue();
			
			String x = node.getChildNodes().item(0).getAttributes().getNamedItem("x").getNodeValue();
			String y = node.getChildNodes().item(0).getAttributes().getNamedItem("y").getNodeValue();
			String height = node.getChildNodes().item(0).getAttributes().getNamedItem("height").getNodeValue();
			String width = node.getChildNodes().item(0).getAttributes().getNamedItem("width").getNodeValue();
			
			typeMap.put(type, "tag");
			putIndexNumber(type);
			
			if (!id.isEmpty()) {
				typeMap.put(id, "id");
				putIndexNumber(id);
			}
			
			if (!value.isEmpty()) {
				typeMap.put(value, "value");
				putIndexNumber(value);
			}
			if (!name.isEmpty()) {
				typeMap.put(name, "name");
				putIndexNumber(name);
			}
			
			if (!label.isEmpty()) {
				typeMap.put(label, "label");
				putIndexNumber(label);
			}
			
			if(shown.equalsIgnoreCase("true")){
				StringBuilder element = new StringBuilder();
				element.append(type+"["+indexMap.get(type)+"]"+"::");
				if(!id.isEmpty()){
					element.append(id + "["+ indexMap.get(id)+"]::");
				}else{
					element.append("-::");
				}
				if(!value.isEmpty()){
					if(null!=FitServer.getHost()){
						element.append("<span style='color:blue'>"+value+"</span>");
					}else{
						element.append(""+value+"");
					}
					element.append("["+indexMap.get(value)+"]::");
				}else{
					element.append("-::");
				}
				if(!name.isEmpty()){
					if(!name.equalsIgnoreCase("null")){
						element.append(name + "["+ indexMap.get(name)+"]::");
					}
				}else{
					element.append("-::");
				}
				if(!label.isEmpty()){
					element.append(label + "["+ indexMap.get(label)+"]::");
				}else{
					element.append("-::");
				}
				element.append("("+ x +","+ y +")");
				element.append(",");
				element.append("("+ width +"x"+ height +")");

				elementsList.add(element.toString());
			}
			
          }  
           
        }
    }
	
	public static void parsingJSON(JSONObject source){
		
		try {
			String type = "";
			String id = "";
			String value = "";
			String name = "";
			String label = "";
			
			if(source.has("type")){
				type = source.getString("type");
			}
			if(source.has("id")){
				id = source.getString("id");
			}
			if(source.has("value")){
				value = source.getString("value");
			}
			if(source.has("name")){
				name = source.getString("name");
			}
			if(source.has("label")){
				label = source.getString("label");
			}
			
			if(source.has("source")){
				WebViewHelper.parsingHtml(source.getString("source"));
			}
			
			JSONObject origin = source.getJSONObject("rect").getJSONObject("origin");
			JSONObject size = source.getJSONObject("rect").getJSONObject("size");
			
			typeMap.put(type, "tag");
			putIndexNumber(type);
			
			if (!id.isEmpty()) {
				typeMap.put(id, "id");
				putIndexNumber(id);
			}
			
			if (!value.isEmpty()) {
				typeMap.put(value, "value");
				putIndexNumber(value);
			}
			if (!name.isEmpty()) {
				typeMap.put(name, "name");
				putIndexNumber(name);
			}
			
			if (!label.isEmpty()) {
				typeMap.put(label, "label");
				putIndexNumber(label);
			}
			
			if(false == source.has("shown") || true == source.getBoolean("shown")){
				StringBuilder element = new StringBuilder();
				element.append(type+"["+indexMap.get(type)+"]"+"::");
				if(!id.isEmpty()){
					element.append(id + "["+ indexMap.get(id)+"]::");
				}else{
					element.append("-::");
				}
				if(!value.isEmpty()){
					if(null!=FitServer.getHost()){
						element.append("<span style='color:blue'>"+value+"</span>");
					}else{
						element.append(""+value+"");
					}
					element.append("["+indexMap.get(value)+"]::");
				}else{
					element.append("-::");
				}
				if(!name.isEmpty()){
					if(!name.equalsIgnoreCase("null")){
						element.append(name + "["+ indexMap.get(name)+"]::");
					}
				}else{
					element.append("-::");
				}
				if(!label.isEmpty()){
					element.append(label + "["+ indexMap.get(label)+"]::");
				}else{
					element.append("-::");
				}
				element.append("("+origin.getInt("x")+","+origin.getInt("y")+")");
				element.append(",");
				element.append("("+size.getInt("width")+"x"+size.getInt("height")+")");

				elementsList.add(element.toString());
			}
			
			if(source.has("children")){
				JSONArray children = source.getJSONArray("children");
				for(int i=0, cnt=children.length();i<cnt;i++) {
					parsingJSON((JSONObject)children.get(i));
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static String printElement(){
		StringBuilder sb = new StringBuilder();
		String[] list = new String[elementsList.size()];
		elementsList.toArray(list);
		Arrays.sort(list);
		for (String elem : list) {
			sb.append(elem);
			if(null!=FitServer.getHost()){
				sb.append("<br/>");
			}else{
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	public static boolean hasText(String text){
		for(String element:elementsList){
			if(element.contains(text)){
				return true;
			}
		}
		return false;
	}
	
	private static void putIndexNumber(String key) {
		if (indexMap.containsKey(key)) {
			indexMap.put(key, indexMap.get(key) + 1);
		} else {
			indexMap.put(key, 0);
		}
	}
}
