/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.differences;

import java.io.File;

import fitlibrary.log.Logging;
import fitlibrary.utility.StringUtility;

public class FolderRunnerLocalFile implements LocalFile {
    private File file;
    private static File CONTEXT = new File(".");

    public FolderRunnerLocalFile(File file) {
        this.file = file;
    }
    public FolderRunnerLocalFile(String localFileName) {
        if (!localFileName.startsWith("files/") && !localFileName.startsWith("files\\"))
            localFileName = "files/"+localFileName;
        Logging.log(this,"FolderRunnerLocalFile(): "+localFileName);
        this.file = new File(localFileName);
    }
    public LocalFile withSuffix(String suffix) {
        String name = file.getPath();
        int last = name.lastIndexOf(".");
        if (last >= 0)
            name = name.substring(0,last+1)+suffix;
        Logging.log(this,"withSuffix(): "+name);
        return new FolderRunnerLocalFile(name);
    }
    public File getFile() {
        File absoluteFile = new File(CONTEXT,file.getPath());
        Logging.log(this,"getFile(): "+absoluteFile.getAbsolutePath());
        return absoluteFile;
    }
    public void mkdir() {
        File filesFolder = new File(CONTEXT,"files");
        if (!filesFolder.exists())
            filesFolder.mkdir();
        File diry = new File(filesFolder,getFile().getName());
        Logging.log(this,"mkdir(): "+diry.getAbsolutePath());
        if (!diry.exists())
            diry.mkdir();
    }
    public String htmlImageLink() {  
        return "<img src=\""+StringUtility.replaceString(file.getPath(),"\\","/")+
            "\">";
    }
    public String htmlLink() { 
        return "<a href=\"file:///"+StringUtility.replaceString(file.getAbsolutePath(),"\\","/")+
            "\">"+file.getName()+"</a>";
    }
    public boolean equals(Object object) {
        if (!(object instanceof FolderRunnerLocalFile))
            return false;
        String absolutePath = ((FolderRunnerLocalFile)object).file.getPath();
        String otherAbsolutePath = file.getPath();
        boolean equals = absolutePath.equals(otherAbsolutePath);
        Logging.log(this,"equals(): "+equals+" with: '"+absolutePath+"' and '"+otherAbsolutePath);
        return equals;
    }
    public int hashCode() {
        return file.hashCode();
    }
    public String toString() {
        return "FolderRunnerLocalFile["+file.getName()+"]";
    }
    public static void setContext(File context) {
        CONTEXT  = context;
    }
}
