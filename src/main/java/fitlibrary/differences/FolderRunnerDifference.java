/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.differences;

import java.io.File;

import fitlibrary.log.Logging;

public class FolderRunnerDifference implements DifferenceInterface{
    private static final String DOT_FILES = "files/";
	private static File CURRENT_TEST_FILE_DIRY = new File(".");
    
	public File getRelativeFile(String name) {
//		if (!name.startsWith("files/")) // Allow for implicit and explicit reference
//			name = "files/"+name;
        File file = new File(CURRENT_TEST_FILE_DIRY,DOT_FILES+name);
		return file;
    }
	public File getHomeDirectory() {
	    return new File(".");
	}
	public String url(File file) {
        return DOT_FILES+file.getName();
    }
//	public String trimFileName(String fileName) {
//		if (fileName.startsWith(DOT_FILES) || fileName.startsWith("files\\"))
//			return fileName.substring(DOT_FILES.length());
//    	return fileName;
//	}
	public static void setCurrentTestDiryFile(File currentTestDiryFile) {
		CURRENT_TEST_FILE_DIRY = currentTestDiryFile;
	}
	public LocalFile getLocalFile(String localFileName) {
        Logging.log(this,"getLocalFile(String): "+localFileName);
		return new FolderRunnerLocalFile(localFileName);
	}
	public LocalFile getLocalFile(File file) {
        Logging.log(this,"getLocalFile(File): "+file.getPath());
        return new FolderRunnerLocalFile(file);
	}
	public LocalFile getGlobalFile(File file) {
        Logging.log(this,"getGlobalFile(File): "+file.getPath());
		return getLocalFile(file);
	}
    public void setContext(File file) {
        Logging.log(this,"setContext(File): "+file.getAbsolutePath());
        FolderRunnerLocalFile.setContext(file);
    }
	public String escapeHtml(String s) {
		return s.replaceAll("<","&lt;").replaceAll(">","&gt;");
	}
}
