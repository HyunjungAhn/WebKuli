/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.runner;

import java.io.File;

import fitlibrary.DoFixture;
import fitlibrary.log.Logging;
import fitlibrary.utility.StringUtility;

public class FolderRunnerFixture extends DoFixture {
    public static final String MY_DOCUMENTS;
    public static final String CREATIONS;
    
    static {
        MY_DOCUMENTS = System.getProperty("user.home")+"\\My Documents";
        CREATIONS = MY_DOCUMENTS+File.separator+"Creations";
    }
    
    public void log() {
        Logging.setLogging(true);
    }
	public String runGiving(String testDirectoryName, String reportDirectoryName) throws Exception {
        long start = System.currentTimeMillis();
		String separator = File.separator;
		String TESTFILES = CREATIONS+separator+"MyOwnFitReleases"+separator;
		String theCounts = new FolderRunner().run(TESTFILES+testDirectoryName,TESTFILES+reportDirectoryName).getCounts();
		String fileName = TESTFILES+reportDirectoryName+separator+"reportIndex.html";
        long end = System.currentTimeMillis();
        return urlFile(fileName,theCounts)+" in "+(end-start)/1000+" seconds";
	}
    private String urlFile(String fileName, String title) {
        String url = StringUtility.replaceString("file:///"+fileName," ","%20");
        url = StringUtility.replaceString(url,"\\","/");
        return "<a href=\"" + url+ "\">"+title+"</a>";
    }
}
