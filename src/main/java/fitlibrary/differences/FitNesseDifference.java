/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.differences;

import java.io.File;

/**
 * This manages the compile-time differences between Core Fit and Fit as included with FitNesse
 */
public class FitNesseDifference implements DifferenceInterface {
	protected final static String LOCAL_FILES = "/files";
	protected final static String FITNESSE_FILES_LOCATION = 
		"FitNesseRoot"+LOCAL_FILES;
	
	public File getRelativeFile(String name) {
		return new File(getHomeDirectory(),name);
	}
	public File getHomeDirectory() {
		File fitNesse = new File(FITNESSE_FILES_LOCATION);
		if (!fitNesse.exists() || !fitNesse.isDirectory())
			throw new RuntimeException("The FitNesse directories have changed: "+
					fitNesse.getAbsolutePath());
		return fitNesse;
	}	    
	public String url(String fileName) {
		return LOCAL_FILES+"/"+fileName;
	}
	public LocalFile getLocalFile(String fileName) {
		return new FitNesseLocalFile(fileName);
	}
	public LocalFile getLocalFile(File file) {
		return new FitNesseLocalFile(file);
	}
	public LocalFile getGlobalFile(File file) {
		return new FitNesseGlobalFile(file);
	}
    public void setContext(File file) {
        throw new RuntimeException("This should never be called, as the FolderRunner calls it.");
    }
	public String escapeHtml(String s) {
		return s.replaceAll("<","&lt;").replaceAll(">","&gt;");
	}
}
