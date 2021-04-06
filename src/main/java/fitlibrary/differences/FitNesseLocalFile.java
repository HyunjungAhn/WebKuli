/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.differences;

import java.io.File;

import fitlibrary.utility.StringUtility;

public class FitNesseLocalFile implements LocalFile {
	protected final static String LOCAL_FILES = "/files";
	protected final static String FITNESSE_FILES_LOCATION = 
		"FitNesseRoot"+LOCAL_FILES;
	private String fileName;
	
	public FitNesseLocalFile(String fileName) {
		setFileName(fileName);
	}
	public FitNesseLocalFile(File file) {
		String path = file.getPath();
		setFileName(StringUtility.replaceString(path,"\\","/"));
	}
	private void setFileName(String fileName) {
		final String prefix = LOCAL_FILES+"/";
		if (fileName.startsWith(prefix))
			this.fileName = fileName.substring(prefix.length());
		else
			this.fileName = fileName;
	}
	public LocalFile withSuffix(String suffix) {
		String name = fileName;
		int last = fileName.lastIndexOf(".");
		if (last >= 0)
			name = name.substring(0,last+1)+suffix;
		return new FitNesseLocalFile(name);
	}
	public File getFile() {
		if (fileName.startsWith("/") || fileName.charAt(1) == ':')
			return new File(fileName);
		return new File(FITNESSE_FILES_LOCATION+"/"+fileName);
	}
	public void mkdir() {
		File file = getFile();
		if (!file.exists())
			file.mkdir();
	}
	public String htmlImageLink() {
		return "<img src=\""+LOCAL_FILES+"/"+fileName+"\">";
	}
	public String htmlLink() {
		String name = fileName;
		int last = fileName.lastIndexOf("/");
		if (last >= 0)
			name = name.substring(last+1);
		return "<a href=\""+LOCAL_FILES+"/"+fileName+"\">"+name+"</a>";
	}
	public boolean equals(Object object) {
		if (!(object instanceof FitNesseLocalFile))
			return false;
		return fileName.equals(((FitNesseLocalFile)object).fileName);
	}
	public int hashCode() {
		return fileName.hashCode();
	}
	public String toString() {
		return "FitNesseLocalFile["+fileName+"]";
	}
}
