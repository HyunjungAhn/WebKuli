package com.nts.ti.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ResourceManager {

	final static String SEPERATOR = File.separator;
	final static String DEFAULT_RESOURCE_PATH = System.getProperty("user.dir") + SEPERATOR + "properties";
	final static Properties keywordProperties = new Properties();
	static ResourceBundle imageResource;
	static String resourcePath = null;
	
	public static String getKeyword(String resourceName){
		if(!keywordProperties.isEmpty() && keywordProperties.containsKey(resourceName)){
			return keywordProperties.getProperty(resourceName);
		}		
		return resourceName;
	}
	
	public static String getImage(String resourceName){
		if(null!=imageResource && imageResource.containsKey(resourceName)){
			return resourcePath + SEPERATOR + imageResource.getString(resourceName);
		}		
		return resourceName;
	}
	
	public static void loadKeyword(String resource) {
		if(isExists(resource)){		
			try {
				String resourceFile = DEFAULT_RESOURCE_PATH + SEPERATOR + resource + ".properties";
				FileInputStream fileInputStream = new FileInputStream(resourceFile);
				keywordProperties.load(new InputStreamReader(fileInputStream, "UTF-8"));
				fileInputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void loadImage(String imagePath){
		
		resourcePath = imagePath;
		
		if(new File(resourcePath).exists()){
			if(new File(resourcePath + SEPERATOR + "SikuliImageList.properties").exists()){
				new File(resourcePath + SEPERATOR + "SikuliImageList.properties").delete();
				try {
					String resourceFile = resourcePath + SEPERATOR + "SikuliImageList" + ".properties";
					OutputStreamWriter osw;
					osw = new OutputStreamWriter(new FileOutputStream(resourceFile), "UTF-8");
		
					for(File img:new File(resourcePath).listFiles()){
						if(img.getName().lastIndexOf(".")!=-1 && (img.getName().contains("png") || img.getName().contains("PNG"))){
							String rName = img.getName().substring(0, img.getName().lastIndexOf("."));
	//						rName = new String(rName.getBytes(),"UTF-8");
							String rValue = img.getName();
	//						rValue = new String(rValue.getBytes(),"UTF-8");
							osw.write(rName+"="+rValue+"\n");
						}
					}
					osw.close();
					imageResource = new PropertyResourceBundle(new InputStreamReader(new FileInputStream(resourceFile), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}	catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				try {
					String resourceFile = resourcePath + SEPERATOR + "SikuliImageList" + ".properties";
					OutputStreamWriter osw;
					osw = new OutputStreamWriter(new FileOutputStream(resourceFile), "UTF-8");
		
					for(File img:new File(resourcePath).listFiles()){
						if(img.getName().lastIndexOf(".")!=-1 && (img.getName().contains("png") || img.getName().contains("PNG"))){
							String rName = img.getName().substring(0, img.getName().lastIndexOf("."));
	//						rName = new String(rName.getBytes(),"UTF-8");
							String rValue = img.getName();
	//						rValue = new String(rValue.getBytes(),"UTF-8");
							osw.write(rName+"="+rValue+"\n");
						}
					}
					osw.close();
					imageResource = new PropertyResourceBundle(new InputStreamReader(new FileInputStream(resourceFile), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}	catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String insertKeyword(String resource, String key, String value){
		String returnString = key + "::(" + value + ") already exists!";
		String resourceFile = DEFAULT_RESOURCE_PATH + SEPERATOR + resource + ".properties";
		if (isExists(resource)) {
			Properties prop = new Properties();
			FileInputStream fileInputStream;
			try {
				fileInputStream = new FileInputStream(resourceFile);
				prop.load(new InputStreamReader(fileInputStream, "UTF-8"));
				fileInputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (prop.containsKey(key) && !prop.get(key).equals(value)) {
				prop.setProperty(key, value);
				changeResource(resourceFile, prop);
				returnString = key + "::(" + prop.getProperty(key) + ") is changed to (" + value + ")!";
			} else if(!prop.containsKey(key)){
				prop.setProperty(key, value);
				changeResource(resourceFile, prop);
				returnString = key + "::(" + value + ") is added!";
			} else {
				return returnString;
			}
		}
		return returnString;
	}

	private static boolean isExists(String resource) {
		if (!new File(DEFAULT_RESOURCE_PATH).exists()) {
			new File(DEFAULT_RESOURCE_PATH).mkdir();
		}
		String resourceFile = DEFAULT_RESOURCE_PATH + SEPERATOR + resource + ".properties";
		if (!new File(resourceFile).exists()) {
			try {
				return new File(resourceFile).createNewFile();
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}
	
	private static void changeResource(String resourceFile, Properties prop){
		try {
			new File(resourceFile).delete();
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(resourceFile, true), "UTF-8");
			prop.store(osw, "Resource updated");
			osw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected static void printResource(){
		keywordProperties.list(System.out);
	}
}
