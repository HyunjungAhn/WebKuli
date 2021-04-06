package com.nts.ti.common;

import fit.FitServer;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.AndroidSdk;
import io.selendroid.standalone.android.JavaSdk;
import io.selendroid.standalone.android.OS;
import io.selendroid.standalone.android.impl.DefaultAndroidApp;
import io.selendroid.standalone.builder.SelendroidServerBuilder;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.exceptions.ShellCommandException;
import io.selendroid.standalone.io.ShellCommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author Administrator
 * 
 */
public class SelendroidBuilder {

	String targetPkgName;
	String targetApkPath;
	String mainActivityName;
//	SelendroidServerBuilder selendroidApkBuilder = new SelendroidServerBuilder();
	private AndroidApp selendroidServer = null;
	private File selendroidServerOutput = null;
	private DefaultAndroidApp applicationUnderTest = null;
	private File apkOutput = null;

	final String user_dir = System.getProperty("user.dir");
	final String user_home = System.getProperty("user.home");
	final String temp_dir = System.getProperty("java.io.tmpdir");
	final String selendriodApkPath = "prebuild/" + PREBUILD_SELENDROID_SERVER;
	final String androidDriverApkPath = "prebuild/" + PREBUILD_ANDROID_SERVER;
	final String separator = File.separator;

	public static final String PREBUILD_SELENDROID_SERVER = "selendroid-server-0.16.0-SNAPSHOT.apk";
	public static final String PREBUILD_ANDROID_SERVER = "android-driver-app-0.16.0-SNAPSHOT.apk";

	private static final String ADB_PATH = AndroidSdk.adb().getAbsolutePath(); 
	private static final Logger log = Logger.getLogger(SelendroidBuilder.class.getName());

	/**
	 * resigning apk -> install selendriod -> install apk -> instrument ->
	 * forward -> start app
	 * 
	 * @param targetApkPath
	 * @throws
	 */
	public void launchApk(String targetApkPath) throws Exception {
		launchApk(null, targetApkPath);
	}

	public void launchApk(String serial, String targetApkPath) throws Exception {
		reclaimApk(serial);
		regenSelendroidServer(targetApkPath);
		apkOutput = new File(resignApp(new File(targetApkPath))
				.getAbsolutePath());
		installApk(serial, selendroidServerOutput.getAbsolutePath());
		installApk(serial, apkOutput.getAbsolutePath());
		instrument(serial, targetApkPath);
//		startApp(serial, targetApkPath);
	}
	
	public void LaunchAndroidDriverApk(String serial) throws Exception{
		File customizedServer = File.createTempFile("android-server", ".apk");
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(androidDriverApkPath);
		IOUtils.copy(is, new FileOutputStream(customizedServer));
		IOUtils.closeQuietly(is);

		String androidServerApk = customizedServer.getAbsolutePath();
		
		reclaimApk(serial);
		regenSelendroidServer(androidServerApk);
		apkOutput = new File(resignApp(new File(androidServerApk))
				.getAbsolutePath());
		installApk(serial, selendroidServerOutput.getAbsolutePath());
		installApk(serial, apkOutput.getAbsolutePath());
		instrument(serial, androidServerApk);
//		startApp(serial, androidServerApk);
	}

	/**
	 * reboot device
	 * 
	 * @throws AndroidSdkException
	 * @throws ShellCommandException
	 */
	public void reboot() throws AndroidSdkException, ShellCommandException {			
		CommandLine commandline = new CommandLine(ADB_PATH);
		deviceList = getDeviceList();
		for(String serial:deviceList){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
			commandline.addArgument("reboot", false);
			log.info(ShellCommand.exec(commandline));			
		}
	}

	/**
	 * 디바이스에
	 * 
	 * @param targetApkPath
	 * @throws AndroidSdkException
	 * @throws ShellCommandException
	 */
	public void startApp(String targetApkPath) throws AndroidSdkException,
	ShellCommandException {
		startApp(null, targetApkPath);
	}

	public void startApp(String serial, String targetApkPath) throws AndroidSdkException,
	ShellCommandException {		
		applicationUnderTest = new DefaultAndroidApp(new File(targetApkPath));
		CommandLine commandline = new CommandLine(ADB_PATH);
	
		if(null!=serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("shell", false);
		commandline.addArgument("am", false);
		commandline.addArgument("start", false);
		commandline.addArgument("-n", false);
		commandline.addArgument(applicationUnderTest.getBasePackage() + "/"
				+ applicationUnderTest.getMainActivity());
		ShellCommand.exec(commandline);		
	}

	public void startApp(String serial, String packageName, String activity) throws AndroidSdkException, ShellCommandException {		
		CommandLine commandline = new CommandLine(ADB_PATH);
		if(null!=serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("shell", false);
		commandline.addArgument("am", false);
		commandline.addArgument("start", false);
		commandline.addArgument("-n", false);
		commandline.addArgument(packageName + "/" + activity);
		ShellCommand.exec(commandline);
	}

	private void regenSelendroidServer(String targetApkPath)
			throws IOException, ShellCommandException, AndroidSdkException {
		applicationUnderTest = new DefaultAndroidApp(new File(targetApkPath));
		File customizedServer = File
				.createTempFile("selendroid-server", ".apk");
		log.info("Creating customized Selendroid-server: "
				+ customizedServer.getAbsolutePath());

		String prebuild_server = user_dir + separator + "prebuild" + separator
				+ PREBUILD_SELENDROID_SERVER;
		if (new File(prebuild_server).exists()) {
			InputStream is = getResourceAsStream(prebuild_server);
			IOUtils.copy(is, new FileOutputStream(customizedServer));
			IOUtils.closeQuietly(is);
		} else {
			InputStream is = this.getClass().getClassLoader()
					.getResourceAsStream(selendriodApkPath);
			IOUtils.copy(is, new FileOutputStream(customizedServer));
			IOUtils.closeQuietly(is);
		}

		selendroidServer = new DefaultAndroidApp(customizedServer);
		cleanUpPrebuildServer();
		File selendroidServer = createAndAddCustomizedAndroidManifestToSelendroidServer();
		selendroidServerOutput = new File(user_dir + separator
				+ "selendroid-server-" + applicationUnderTest.getBasePackage()
				+ ".apk");
		resigningApk(selendroidServer, selendroidServerOutput);
	}

	public void installApk(String apkPath) throws ShellCommandException {
		installApk(null,apkPath);
	}

	public void installApk(String serial, String apkPath) throws ShellCommandException {
		CommandLine commandline = new CommandLine(ADB_PATH);
		if(null!=serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("install", false);
		commandline.addArgument("-r", false);
		commandline.addArgument(apkPath, false);
		String result = ShellCommand.exec(commandline);
		log.info("\"Installed -" + apkPath + "\" : " + result);
	}

	public void installAndroidDriver() throws ShellCommandException, IOException, AndroidSdkException{
		installAndroidDriver(null);
	}

	public void installAndroidDriver(String serial) throws ShellCommandException, IOException, AndroidSdkException{
		List<String> pkgList = getInstalledPackage(serial);
		if(!pkgList.contains("package:io.selendroid.androiddriver")){
			File customizedServer = File.createTempFile("android-server", ".apk");
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(androidDriverApkPath);
			IOUtils.copy(is, new FileOutputStream(customizedServer));
			IOUtils.closeQuietly(is);

			String androidServerApk = customizedServer.getAbsolutePath();

			CommandLine commandline = new CommandLine(ADB_PATH);
			if(null!=serial && !serial.isEmpty()){
				commandline.addArgument("-s", false);
				commandline.addArgument(serial, false);
			}
			commandline.addArgument("install", false);
			commandline.addArgument("-r", false);
			commandline.addArgument(androidServerApk, false);
			String result = ShellCommand.exec(commandline);
			log.info("\"Installed -" + androidServerApk + "\" : " + result);
		}
	}

	/**
	 * 디바이스에 패키지명 또는 apk경로를 이용해서 앱을 삭제함
	 * 
	 * @param pkgNameOrApkPath
	 * @throws ShellCommandException
	 * @throws AndroidSdkException
	 */
	public void uninstallApk(String pkgNameOrApkPath) throws ShellCommandException, AndroidSdkException {
		uninstallApk(null, pkgNameOrApkPath);
	}

	public void uninstallApk(String serial, String pkgNameOrApkPath) throws ShellCommandException, AndroidSdkException {
		if (new File(pkgNameOrApkPath).exists()) {
			applicationUnderTest = new DefaultAndroidApp(new File(
					pkgNameOrApkPath));
			pkgNameOrApkPath = applicationUnderTest.getBasePackage();
		}
		
		CommandLine commandline = new CommandLine(ADB_PATH);
		if(null!=serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("shell", false);
		commandline.addArgument("pm", false);
		commandline.addArgument("uninstall", false);
		commandline.addArgument(pkgNameOrApkPath, false);
		String result = ShellCommand.exec(commandline);
		log.info("\"Uninstalled -" + pkgNameOrApkPath + "\" : " + result);
	}

	public void instrument(String apkPath) throws ShellCommandException, AndroidSdkException {
		instrument(null, apkPath);
	}

	public void instrument(String serial, String apkPath) throws ShellCommandException, AndroidSdkException {
		applicationUnderTest = new DefaultAndroidApp(new File(apkPath));
		
		CommandLine commandline = new CommandLine(ADB_PATH);
		if(null!=serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("shell", false);
		commandline.addArgument("am", false);
		commandline.addArgument("instrument", false);
		commandline.addArgument("-e", false);
		commandline.addArgument("main_activity", false);
		commandline.addArgument(applicationUnderTest.getMainActivity(), false);
		commandline.addArgument("-e", false);
		commandline.addArgument("server_port", false);
		commandline.addArgument("9090", false);
		commandline.addArgument("io.selendroid.server/.ServerInstrumentation", false);
		String result = ShellCommand.exec(commandline);
		log.info(result);
	}
	
	public boolean portListening(String serial, int port) throws ShellCommandException{
		CommandLine commandline = new CommandLine(ADB_PATH);
		if(null!=serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("shell", false);
		commandline.addArgument("netstat", false);
		
		int retryCnt = 30;
		while(retryCnt > 0){
			String result = ShellCommand.exec(commandline);
			if(result.contains(":::9090"))	return true;
			retryCnt--;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 디바이스에 instrumentation 리스트를 출력함.
	 * 
	 * @return
	 * @throws ShellCommandException
	 */
	public List<String> getInstrumentPackage() throws ShellCommandException {
		return getInstrumentPackage(null);
	}

	public List<String> getInstrumentPackage(String serial) throws ShellCommandException {
		CommandLine commandline = new CommandLine(ADB_PATH);
		if(null!=serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("shell", false);
		commandline.addArgument("pm", false);
		commandline.addArgument("list", false);
		commandline.addArgument("instrumentation", false);
		List<String> results = Arrays.asList(ShellCommand.exec(commandline).split("\n"));
		return results;
	}

	public  List<String> getInstalledPackage(String serial) throws ShellCommandException {
		CommandLine commandline = new CommandLine(ADB_PATH);
		if(null!=serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("shell", false);
		commandline.addArgument("pm", false);
		commandline.addArgument("list", false);
		commandline.addArgument("package", false);
		commandline.addArgument("-3", false);
		List<String> results = Arrays.asList(ShellCommand.exec(commandline).split("\n"));
		return results;
	}

	public boolean isInstrumented() throws ShellCommandException {
		return isInstrumented(null);
	}

	public boolean isInstrumented(String serial) throws ShellCommandException {
		List<String> inst = getInstrumentPackage(serial);
		String tgPkg = "";
		for (String targetPkg : inst) {
			if (targetPkg.contains("selendroid"))
				tgPkg = targetPkg.substring(targetPkg.indexOf("=") + 1,
						targetPkg.lastIndexOf(")"));
		}
		if (tgPkg.isEmpty())
			return false;
		else
			return true;
	}

	public String getInstrumentedPkg() throws ShellCommandException {
		return getInstrumentedPkg(null);
	}

	public String getInstrumentedPkg(String serial) throws ShellCommandException {
		List<String> inst = getInstrumentPackage(serial);
		String tgPkg = "";
		for (String targetPkg : inst) {
			if (targetPkg.contains("selendroid"))
				tgPkg = targetPkg.substring(targetPkg.indexOf("=") + 1,
						targetPkg.lastIndexOf(")"));
		}
		return tgPkg;
	}

	/**
	 * 디바이스에 instrumentation된 Selendriod 서버와 target 앱을 삭제함
	 * 
	 * @throws ShellCommandException
	 * @throws AndroidSdkException
	 */
	public void reclaimApk() throws ShellCommandException, AndroidSdkException {
		List<String> inst = getInstrumentPackage();
		String tgPkg = "";
		for (String targetPkg : inst) {
			if (targetPkg.contains("selendroid"))
				tgPkg = targetPkg.substring(targetPkg.indexOf("=") + 1,
						targetPkg.lastIndexOf(")"));
		}
		uninstallApk(tgPkg);
		uninstallApk("io.selendroid.server");
	}

	public void reclaimApk(String serial) throws ShellCommandException, AndroidSdkException {
		List<String> inst = getInstrumentPackage(serial);
		String tgPkg = "";
		for (String targetPkg : inst) {
			if (targetPkg.contains("selendroid"))
				tgPkg = targetPkg.substring(targetPkg.indexOf("=") + 1,
						targetPkg.lastIndexOf(")"));
		}
		uninstallApk(serial, tgPkg);
		uninstallApk(serial, "io.selendroid.server");
		uninstallApk(serial, "io.selendroid.androiddriver");
	}

	public void forward(int port) throws ShellCommandException {
		forward(null,port);
	}

	public void forward(String serial,  int port) throws ShellCommandException {
		CommandLine commandline = new CommandLine(ADB_PATH);
		if(null!=serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("forward", false);
		commandline.addArgument("tcp:" + port, false);
		commandline.addArgument("tcp:" + port, false);
		ShellCommand.exec(commandline);
	}

	public void forward(String serial, int localPort, int remotePort) throws ShellCommandException {
		CommandLine commandline = new CommandLine(ADB_PATH);
		if(null!=serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("forward", false);
		commandline.addArgument("tcp:" + localPort, false);
		commandline.addArgument("tcp:" + remotePort, false);
		ShellCommand.exec(commandline);
	}

	static List<String> deviceList = new ArrayList<String>();

	/**
	 * ADB를 이용한 Device List 획득
	 * 
	 * @return
	 * @throws ShellCommandException
	 */
	public static List<String> getDeviceList() {
		CommandLine commandline = new CommandLine(ADB_PATH);
		commandline.addArgument("devices", false);
		try {
			deviceList = Arrays.asList(ShellCommand.exec(commandline).split("\n"));
		} catch (ShellCommandException e) {
			e.printStackTrace();
		}
		List<String> devices = Collections.emptyList();
		for (String device : deviceList) {
			if (!device.trim().isEmpty()
					&& !device.contains("devices attached"))
				devices.add(device.trim().split("\t")[0]);
		}
		return devices;
	}

	public static String getDeviceInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("<table border='1'>");
		sb.append("<tr class='ignore'>");
		sb.append("<td>serial</td><td>brand</td><td>model</td><td>carrier</td><td>3G/LTE</td><td>ip</td><td>sdk</td><td>release</td></tr>");
		List<String> deviceList = getDeviceList();
		for (Object serial : deviceList.toArray()) {
			CommandLine commandline = new CommandLine(ADB_PATH);
			commandline.addArgument("-s", false);
			commandline.addArgument((String) serial, false);
			commandline.addArgument("shell", false);
			commandline.addArgument("getprop", false);

			sb.append("<tr class='error'>");
			try {
				Map<String, String> p = new TreeMap<String, String>();
				List<String> deviceInfo = Arrays.asList(ShellCommand.exec(commandline).trim().split("\n"));
				for (Object o : deviceInfo.toArray()) {
					String info = ((String) o).trim();
					if (info.contains("ro.product.brand")) {
						p.put("brand", info.split(": ")[1].replace("[", "")
								.replace("]", ""));
					}
					if (info.contains("ro.product.model")) {
						p.put("model", info.split(": ")[1].replace("[", "")
								.replace("]", ""));
					}
					if (info.contains("ro.csc.sales_code")) {
						p.put("carrier", info.split(": ")[1].replace("[", "")
								.replace("]", ""));
					}
					if (info.contains("ril.currentsystem")) {
						p.put("3G/LTE", info.split(": ")[1].replace("[", "")
								.replace("]", ""));
					}
					if (info.contains("dhcp.wlan0.ipaddress")) {
						p.put("ip", info.split(": ")[1].replace("[", "")
								.replace("]", ""));
					}
					if (info.contains("wlan.driver.status")) {
						if (info.split(": ")[1].replace("[", "")
								.replace("]", "").equalsIgnoreCase("unloaded")) {
							p.put("ip", "not connected");
						}
					}
					if (info.contains("ro.build.version.sdk")) {
						p.put("sdk",
								"SDK-"
										+ info.split(": ")[1].replace("[", "")
										.replace("]", ""));
					}
					if (info.contains("ro.build.version.release")) {
						p.put("version", info.split(": ")[1].replace("[", "")
								.replace("]", ""));
					}
				}
				sb.append("<td>" + serial + "</td>" + "<td>" + p.get("brand")
						+ "</td>" + "<td>" + p.get("model") + "</td>" + "<td>"
						+ p.get("carrier") + "</td>" + "<td>" + p.get("3G/LTE")
						+ "</td>" + "<td>" + p.get("ip") + "</td>" + "<td>"
						+ p.get("sdk") + "</td>" + "<td>" + p.get("version")
						+ "</td>");
			} catch (ShellCommandException e) {
				e.printStackTrace();
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	private InputStream getResourceAsStream(String resource) {
		InputStream is = null;

		is = getClass().getResourceAsStream(resource);
		// switch needed for testability
		if (is == null) {
			try {
				is = new FileInputStream(new File(resource));
			} catch (FileNotFoundException e) {
				// do nothing
			}
		}
		if (is == null) {
			throw new SelendroidException("The resource '" + resource
					+ "' was not found.");
		}
		return is;
	}

	private void cleanUpPrebuildServer() throws ShellCommandException,
	AndroidSdkException {
		selendroidServer.deleteFileFromWithinApk("META-INF/CERT.RSA");
		selendroidServer.deleteFileFromWithinApk("META-INF/CERT.SF");
		selendroidServer.deleteFileFromWithinApk("AndroidManifest.xml");
	}

	private File createAndAddCustomizedAndroidManifestToSelendroidServer()
			throws IOException, ShellCommandException, AndroidSdkException {
		String targetPackageName = applicationUnderTest.getBasePackage();
		File tempdir = new File(temp_dir + File.separatorChar
				+ targetPackageName + System.currentTimeMillis());
		if (!tempdir.exists()) {
			tempdir.mkdirs();
		}

		File customizedManifest = new File(tempdir, "AndroidManifest.xml");
		log.info("Adding target package '" + targetPackageName + "' to "
				+ customizedManifest.getAbsolutePath());

		StringBuilder content = new StringBuilder();
		content.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		content.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n");
		content.append("    package=\"io.selendroid.server\"\n");
		content.append("    android:versionCode=\"1\"\n");
		content.append("    android:versionName=\"0.16.0-SNAPSHOT\" >\n");
		content.append("\n");
		content.append("    <uses-sdk android:minSdkVersion=\"10\" />\n");
		content.append("\n");
		content.append("    <instrumentation\n");
		content.append("        android:name=\"io.selendroid.server.ServerInstrumentation\"\n");
		content.append("        android:targetPackage=\"" + targetPackageName
				+ "\" />\n");
		content.append("    <instrumentation\n");
		content.append("        android:name=\"io.selendroid.server.LightweightInstrumentation\"\n");
		content.append("        android:targetPackage=\"" + targetPackageName
				+ "\" />\n");
		content.append("\n");
		content.append("    <uses-permission android:name=\"android.permission.INTERNET\" />\n");
		content.append("    <uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\" />\n");
		content.append("    <uses-permission android:name=\"android.permission.ACCESS_MOCK_LOCATION\" />\n");
		content.append("    <uses-permission android:name=\"android.permission.INJECT_EVENTS\" />\n");
		content.append("    <uses-permission android:name=\"android.permission.WAKE_LOCK\" />\n");
		content.append("    <uses-permission android:name=\"android.permission.WRITE_CALL_LOG\" />\n");
		content.append("\n");
		content.append("    <application\n");
		content.append("        \n");
		content.append("        android:label=\"Selendroid\" >\n");
		content.append("        <uses-library android:name=\"android.test.runner\" />\n");
		content.append("    </application>\n");
		content.append("\n");
		content.append("</manifest>\n");

		OutputStream outputStream = new FileOutputStream(customizedManifest);
		IOUtils.write(content.toString(), outputStream, Charset
				.defaultCharset().displayName());
		IOUtils.closeQuietly(outputStream);

		// adding the xml to an empty apk	
		CommandLine createManifestApk = new CommandLine(AndroidSdk.aapt().getAbsolutePath());
		createManifestApk.addArgument("package", false);
		createManifestApk.addArgument("-M", false);
		createManifestApk.addArgument(customizedManifest.getAbsolutePath(), false);
		createManifestApk.addArgument("-I", false);
		createManifestApk.addArgument(AndroidSdk.androidJar(), false);
		createManifestApk.addArgument("-F", false);
		createManifestApk.addArgument(tempdir.getAbsolutePath() + File.separatorChar + "manifest.apk", false);
		createManifestApk.addArgument("-f", false);
		log.info(ShellCommand.exec(createManifestApk, 3000));
		
		ZipFile manifestApk = new ZipFile(new File(tempdir.getAbsolutePath()
				+ File.separatorChar + "manifest.apk"));
		ZipArchiveEntry binaryManifestXml = manifestApk
				.getEntry("AndroidManifest.xml");

		File finalSelendroidServerFile = new File(tempdir.getAbsolutePath()
				+ "selendroid-server.apk");
		ZipArchiveOutputStream finalSelendroidServer = new ZipArchiveOutputStream(
				finalSelendroidServerFile);
		finalSelendroidServer.putArchiveEntry(binaryManifestXml);
		IOUtils.copy(manifestApk.getInputStream(binaryManifestXml),
				finalSelendroidServer);

		ZipFile selendroidPrebuildApk = new ZipFile(
				selendroidServer.getAbsolutePath());
		Enumeration<ZipArchiveEntry> entries = selendroidPrebuildApk
				.getEntries();
		for (; entries.hasMoreElements();) {
			ZipArchiveEntry dd = entries.nextElement();
			finalSelendroidServer.putArchiveEntry(dd);

			IOUtils.copy(selendroidPrebuildApk.getInputStream(dd),
					finalSelendroidServer);
		}

		finalSelendroidServer.closeArchiveEntry();
		finalSelendroidServer.close();
		manifestApk.close();
		log.info("file: " + finalSelendroidServerFile.getAbsolutePath());
		return finalSelendroidServerFile;
	}

	public AndroidApp resigningApk(File customSelendroidServer,
			File outputFileName) throws ShellCommandException,
			AndroidSdkException {
		if (outputFileName == null) {
			throw new IllegalArgumentException(
					"outputFileName parameter is null.");
		}
		File androidKeyStore = androidDebugKeystore();

		if (androidKeyStore.isFile() == false) {
			// create a new keystore
			CommandLine commandline = new CommandLine(new File(JavaSdk.keytool().getAbsolutePath()));

			commandline.addArgument("-genkey", false);
			commandline.addArgument("-v", false);
			commandline.addArgument("-keystore", false);
			if(OS.isWindows()){
				commandline.addArgument("\""+androidKeyStore.toString()+"\"", false);
			}else{
				commandline.addArgument(androidKeyStore.toString(), false);
			}

			commandline.addArgument("-storepass", false);
			commandline.addArgument("android", false);
			commandline.addArgument("-alias", false);
			commandline.addArgument("androiddebugkey", false);
			commandline.addArgument("-keypass", false);
			commandline.addArgument("android", false);
			commandline.addArgument("-dname", false);
			commandline.addArgument("CN=Android Debug,O=Android,C=US", false);
			commandline.addArgument("-storetype", false);
			commandline.addArgument("JKS", false);
			commandline.addArgument("-sigalg", false);
			commandline.addArgument("MD5withRSA", false);
			commandline.addArgument("-keyalg", false);
			commandline.addArgument("RSA", false);
			commandline.addArgument("-validity", false);
			commandline.addArgument("9999", false);

			String output = ShellCommand.exec(commandline, 20000);
			log.info("A new keystore has been created: " + output);
		}

		// Sign the jar
		CommandLine commandline = null;
		if (System.getProperty("JAVA_HOME") != null) {
			commandline = new CommandLine(new File(
					System.getProperty("JAVA_HOME") + File.separator + "bin"
							+ File.separator + "jarsigner"));
		} else {
			commandline = new CommandLine(new File(JavaSdk.jarsigner().getAbsolutePath()));
		}
		commandline.addArgument("-sigalg", false);
		commandline.addArgument("MD5withRSA", false);
		commandline.addArgument("-digestalg", false);
		commandline.addArgument("SHA1", false);
		commandline.addArgument("-signedjar", false);
		commandline.addArgument(outputFileName.getAbsolutePath(), false);
		commandline.addArgument("-storepass", false);
		commandline.addArgument("android", false);
		commandline.addArgument("-keystore", false);
		if(OS.isWindows()){
			commandline.addArgument("\""+androidKeyStore.toString()+"\"", false);
		}else{
			commandline.addArgument(androidKeyStore.toString(), false);
		}
		commandline
		.addArgument(customSelendroidServer.getAbsolutePath(), false);
		commandline.addArgument("androiddebugkey", false);
		String output = ShellCommand.exec(commandline, 20000);
		log.info("App signing output: " + output);
		log.info("The app has been signed: " + outputFileName.getAbsolutePath());
		return new DefaultAndroidApp(outputFileName);
	}

	private File androidDebugKeystore() {
		return new File(user_home, File.separatorChar + ".android"
				+ File.separatorChar + "debug.keystore");
	}

	public AndroidApp resignApp(File appFile) throws ShellCommandException,
	AndroidSdkException {
		AndroidApp app = new DefaultAndroidApp(appFile);
		// Delete existing certificates
		deleteFileFromAppSilently(app, "META-INF/MANIFEST.MF");
		deleteFileFromAppSilently(app, "META-INF/CERT.RSA");
		deleteFileFromAppSilently(app, "META-INF/CERT.SF");
		deleteFileFromAppSilently(app, "META-INF/ANDROIDD.SF");
		deleteFileFromAppSilently(app, "META-INF/ANDROIDD.RSA");

		File outputFile = new File(appFile.getParentFile(), "resigned-"
				+ appFile.getName());
		return signTestServer(appFile, outputFile);
	}

	private void deleteFileFromAppSilently(AndroidApp app, String file)
			throws AndroidSdkException {
		if (app == null) {
			throw new IllegalArgumentException(
					"Required parameter 'app' is null.");
		}
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException(
					"Required parameter 'file' is null or empty.");
		}
		try {
			app.deleteFileFromWithinApk(file);
		} catch (ShellCommandException e) {
			// don't care, can happen if file does not exist
		}
	}

	private AndroidApp signTestServer(File customSelendroidServer,
			File outputFileName) throws ShellCommandException,
			AndroidSdkException {
		if (outputFileName == null) {
			throw new IllegalArgumentException(
					"outputFileName parameter is null.");
		}
		File androidKeyStore = androidDebugKeystore();
//		List<String> command = Lists.newArrayList();
		
		CommandLine commandline;

		if (androidKeyStore.isFile() == false) {
			// create a new keystore
			commandline = new CommandLine(JavaSdk.keytool().getAbsolutePath());
			commandline.addArgument("-genkey", false);
			commandline.addArgument("-v", false);
			commandline.addArgument("-keystore", false);
			if(OS.isWindows()){
				commandline.addArgument("\""+androidKeyStore.toString()+"\"", false);
			}else{
				commandline.addArgument(androidKeyStore.toString(), false);
			}
			commandline.addArgument("-storepass", false);
			commandline.addArgument("android", false);
			commandline.addArgument("-alias", false);
			commandline.addArgument("androiddebugkey", false);
			commandline.addArgument("-keypass", false);
			commandline.addArgument("android", false);
			commandline.addArgument("-dname", false);
			commandline.addArgument("CN=Android Debug,O=Android,C=US", false);
			commandline.addArgument("-storetype", false);
			commandline.addArgument("JKS", false);
			commandline.addArgument("-sigalg", false);
			commandline.addArgument("MD5withRSA", false);
			commandline.addArgument("-keyalg", false);
			commandline.addArgument("RSA", false);
			commandline.addArgument("-validity", false);
			commandline.addArgument("9999", false);

			String output = ShellCommand.exec(commandline, 20000);
			log.info("A new keystore has been created: " + output);
		}

		// Sign the jar
		commandline = new CommandLine(JavaSdk.jarsigner().getAbsolutePath());
		commandline.addArgument("-sigalg", false);
		commandline.addArgument("MD5withRSA", false);
		commandline.addArgument("-digestalg", false);
		commandline.addArgument("SHA1", false);
		commandline.addArgument("-signedjar", false);
		commandline.addArgument(outputFileName.getAbsolutePath(), false);
		commandline.addArgument("-storepass", false);
		commandline.addArgument("android", false);
		commandline.addArgument("-keystore", false);
		if(OS.isWindows()){
			commandline.addArgument("\""+androidKeyStore.toString()+"\"", false);
		}else{
			commandline.addArgument(androidKeyStore.toString(), false);
		}
		commandline.addArgument(customSelendroidServer.getAbsolutePath(), false);
		commandline.addArgument("androiddebugkey", false);
		String output = ShellCommand.exec(commandline, 20000);
		log.info("App signing output: " + output);
		log.info("The app has been signed: " + outputFileName.getAbsolutePath());
		return new DefaultAndroidApp(outputFileName);
	}

	/**
	 * 앱 데이터 삭제 (캐시도 함께 삭제)
	 * @param pkgNameOrApkPath
	 * @throws ShellCommandException
	 * @throws AndroidSdkException
	 */
	public void clearAppData(String pkgNameOrApkPath) throws ShellCommandException, AndroidSdkException {
		clearAppData(null, pkgNameOrApkPath);
	}

	public void clearAppData(String serial, String pkgNameOrApkPath) throws ShellCommandException, AndroidSdkException {
		if (new File(pkgNameOrApkPath).exists()) {
			applicationUnderTest = new DefaultAndroidApp(new File(
					pkgNameOrApkPath));
			pkgNameOrApkPath = applicationUnderTest.getBasePackage();
		}
		CommandLine commandline = new CommandLine(ADB_PATH);
		if(null != serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("shell", false);
		commandline.addArgument("pm", false);
		commandline.addArgument("clear", false);
		commandline.addArgument(pkgNameOrApkPath, false);
		String result = ShellCommand.exec(commandline);
		log.info("\"Clear App Data -" + pkgNameOrApkPath + "\" : " + result);
	}

	/**
	 * 기존의 앱을 삭제하지 않고, 입력한 apk로 업데이트
	 * @param targetApkPath
	 * @throws Exception
	 */
	public void updateApk(String targetApkPath) throws Exception {
		updateApk(null, targetApkPath);
	}

	public void updateApk(String serial, String targetApkPath) throws Exception {
		apkOutput = new File(resignApp(new File(targetApkPath))
				.getAbsolutePath());
		installApk(serial, apkOutput.getAbsolutePath());
		instrument(serial, targetApkPath);
		startApp(serial, targetApkPath);
	} 

	/**
	 * adb를 사용한 스크린샷
	 * @param serial
	 * @param fileName
	 * @throws ShellCommandException
	 * @throws IOException 
	 */
	public String adbScreenShot(String when, String serial, String fileName) throws ShellCommandException, IOException{
		String serialNo = null;
		if(serial == null || serial.isEmpty()) {
			serialNo = "local_device";
		} else if(serial.contains(":")) {
			serialNo = serial.replace(":", "_");
		} else {
			serialNo = serial;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String timeStamp = sdf.format(Calendar.getInstance().getTime());
		String dateDir = timeStamp.substring(0, 10);
		
		String nameScreenshot;
		String newFileNamePath;
		File storeDirectory;
		File currentDirectory = new File(".");
		
		if(when != null){
			nameScreenshot = timeStamp + "_" + when + "_" + fileName + ".png";
			if(when.equals("exception")){
				newFileNamePath = currentDirectory.getCanonicalPath() + File.separator + "FitNesseRoot" + File.separator + "files" + File.separator + 
						"screenShots" + File.separator + dateDir + File.separator + serialNo + File.separator + "exception";
			}else{
				newFileNamePath = currentDirectory.getCanonicalPath() + File.separator + "FitNesseRoot" + File.separator + "files" + File.separator + 
						"screenShots" + File.separator + dateDir + File.separator + serialNo + File.separator + "histoty";
			}
		}else{
			nameScreenshot = timeStamp + "_" + fileName + ".png";
			newFileNamePath = currentDirectory.getCanonicalPath() + File.separator + "FitNesseRoot" + File.separator + "files" + File.separator + 
					"screenShots" + File.separator + dateDir + File.separator + serialNo;
		}
		
		storeDirectory = new File(newFileNamePath);
		
		if(!storeDirectory.exists()){
			storeDirectory.mkdirs();
		}

		CommandLine commandline;
		commandline = new CommandLine(ADB_PATH);
		if(null!=serial && !serial.isEmpty()){
			commandline.addArgument("-s", false);
			commandline.addArgument(serial, false);
		}
		commandline.addArgument("shell", false);
		commandline.addArgument("screencap", false);
		commandline.addArgument("-p", false);
		commandline.addArgument("/sdcard/"+nameScreenshot, false);
		if(!ShellCommand.exec(commandline).contains("error")){
			commandline = new CommandLine(ADB_PATH);
			if(null!=serial && !serial.isEmpty()){
				commandline.addArgument("-s", false);
				commandline.addArgument(serial, false);
			}
			commandline.addArgument("pull", false);
			commandline.addArgument("/sdcard/"+nameScreenshot, false);
			commandline.addArgument(newFileNamePath, false);
			if(ShellCommand.exec(commandline).contains("bytes")){
				commandline = new CommandLine(ADB_PATH);
				if(null!=serial && !serial.isEmpty()){
					commandline.addArgument("-s", false);
					commandline.addArgument(serial, false);
				}
				commandline.addArgument("shell", false);
				commandline.addArgument("rm", false);
				commandline.addArgument("/sdcard/"+nameScreenshot, false);
				ShellCommand.exec(commandline);
			}
		}
		String fitServer = null;
		if(null!=FitServer.getHost() && !FitServer.getHost().isEmpty()){
			if(FitServer.getPort()==80){
				fitServer = "http://"+FitServer.getIp();
			}else{
				fitServer = "http://"+FitServer.getIp()+":"+FitServer.getPort();
			}
		}
		String linkImg = fitServer + "/files/screenShots/" + dateDir + "/";

		if(when == null){
			return "<a href='"+ linkImg + serialNo + "/" + nameScreenshot + "' target='_blank'><img src='" + linkImg + serialNo + "/" + nameScreenshot + "' width=300></a>";
		}else if(when.equals("exception")){
			return "<a href='"+ linkImg  + serialNo + "/exception/" + nameScreenshot + "' target='_blank'><img src='" + linkImg + serialNo + "/exception/" + nameScreenshot + "' width=300></a>";			
		}else{
			return "<a href='"+ linkImg  + serialNo + "/history/" + nameScreenshot + "' target='_blank'><img src='"+ linkImg + serialNo + "/history/" + nameScreenshot + "' width=300></a>";			
		}
	}
}