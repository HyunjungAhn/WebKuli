/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.runner;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import fit.Counts;
import fit.Parse;
import fitlibrary.differences.FolderRunnerDifference;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.suite.BatchFitLibrary;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.ParseUtility;

/**
 * Runs all the spreadsheet and HTML files in the given directory, and sub-directories.
 * Generates reports in the given directory, and sub-directories.
 */
public class FolderRunner {
	private static final String FILES = "files";
    public static final String INDEX_HTML = "reportIndex.html";
    protected String encoding = ParseUtility.ASCII_ENCODING;
    private static final Set SETUPS = new HashSet(Arrays.asList(
            new String[]{"SETUP.XLS", "SETUP.HTML", "SETUP.HTM"}));
    private static final Set TEARDOWNS = new HashSet(Arrays.asList(
            new String[]{"TEARDOWN.XLS", "TEARDOWN.HTML", "TEARDOWN.HTM"}));
    private Report topReport;
    private List testListeners = new ArrayList();
    private File inDiry;
    private File reportDiry;
	private File suiteFile;
    private BatchFitLibrary batchFitLibrary = new BatchFitLibrary();
    
    public static void main(String[] args) throws ParseException, IOException {
        Report report = new FolderRunner(args).run();
        report.exit();
    }
    public FolderRunner() {
        Traverse.setFolderRunnerStrategy();
    }
    public FolderRunner(String[] args) {
        this();
        int prefixArgs = 0;
        if (args.length > 1 && args[0].equals("-s")) {
        	prefixArgs = 2;
        	setSuiteFile(args[1]);
        }
        switch (args.length-prefixArgs) {
        case 0:
            setTestFile("tests");
            break;
        case 1:
            setTestFile(args[prefixArgs]);
            break;
        case 2:
            setFiles(args[prefixArgs],args[prefixArgs+1]);
            break;
        case 3:
            setFiles(args[prefixArgs],args[prefixArgs+1]);
            this.encoding = args[prefixArgs+2];
            break;
        default:
            System.err.println("Usage: java fitlibrary.runner.FolderRunner\n"+
                    "Or:    java fitlibrary.runner.FolderRunner testFolder\n"+
                    "Or:    java fitlibrary.runner.FolderRunner testFolder reportFolder\n"+
                    "Or:    java fitlibrary.runner.FolderRunner testFolder reportFolder unicodeEncoding"+
                    "Or:    java fitlibrary.runner.FolderRunner -s suiteFileName testFolder\n"+
                    "Or:    java fitlibrary.runner.FolderRunner -s suiteFileName testFolder reportFolder\n"+
                    "Or:    java fitlibrary.runner.FolderRunner -s suiteFileName testFolder reportFolder unicodeEncoding");
            System.exit(-1);
        }
    }
    private void setSuiteFile(String fileName) {
		this.suiteFile = new File(fileName);
	}
	public Report run(String testDiry) throws ParseException, IOException {
        setTestFile(testDiry);
        return run();
    }
    public Report run(String theInDiry, String theReportDiry) throws ParseException, IOException {
        setFiles(theInDiry,theReportDiry);
        return run();
    }
    public Report run() throws ParseException, IOException {
        if (!inDiry.exists() || !inDiry.isDirectory() )
            throw new RuntimeException("Folder is needed for input: "+
                    inDiry.getAbsolutePath());
        if (reportDiry.exists()) {
            if (!reportDiry.isDirectory())
                throw new RuntimeException("File exists but is not a directory: "+reportDiry.getAbsolutePath());		
        } else if (!reportDiry.mkdir())
            throw new RuntimeException("Unable to create folder "+reportDiry.getAbsolutePath());
        if (reportDiry.getAbsolutePath().startsWith(inDiry.getAbsolutePath()))
            throw new RuntimeException("The reports folder can't be inside the tests folder: it'll run forever!");
        copyCssAndImageFilesFromJar(inDiry);
        Parse setUpTables = new Parse("table","",null,null);
        Parse tearDownTables = null;
        String title = "";
        File topReportDiry = reportDiry;
        topReport = new Report("FolderRunner",reportDiry,"",topReportDiry);
        File reportFile = new File(reportDiry,INDEX_HTML); // exposed critical region between this and next
        if (fileIsLocked(reportFile))
        	throw new RuntimeException("Already running");
        if (suiteFile != null)
        	runSuite(suiteFile,reportDiry,topReport,setUpTables,tearDownTables);
        runDiry(title,inDiry,reportDiry,topReport,setUpTables,tearDownTables,"",topReportDiry);
        giveFeedbackToUser();
        suiteFinished();
        topReport.setFinished();
    	writeReport(reportFile,topReport);
        return topReport;
    }
	private void runDiry(String title, File theInDiry, File theReportDiry, Report parentReport,
    		Parse setUpTables, Parse tearDownTables, String path, File topReportDiry) throws ParseException, IOException {
    	FolderRunnerDifference.setCurrentTestDiryFile(theInDiry);
     	Report report = new Report(title,theReportDiry,parentReport,path,topReportDiry);
    	Parse fullSetUpTables = appendSetUp(setUpTables,theInDiry);
    	Parse fullTearDownTables = prependTearDown(tearDownTables,theInDiry);
        
        File filesFile = new File(theInDiry,FILES);
        if (filesFile.exists())
            CopyFiles.copyFilesRecursively(theInDiry,theReportDiry,FILES);
    	
    	File[] files = theInDiry.listFiles();
    	for (int i = 0; i < files.length; i++) {
            giveFeedbackToUser();
    		File file = files[i];
    		String name = file.getName();
    		if (file.isDirectory()) {
    			if (canRunThisFolder(name)) {
    				File subReportDiry = new File(theReportDiry,name);
    				if (!subReportDiry.exists())
    					subReportDiry.mkdir();
    				runDiry(title+"."+file.getName(),file,subReportDiry,report,
    						fullSetUpTables,fullTearDownTables,path+"../",topReportDiry);
    		    	FolderRunnerDifference.setCurrentTestDiryFile(theInDiry);
    			}
    		}
    		else if (!specialFileName(name))
    			runFile(file,theReportDiry,report,fullSetUpTables,fullTearDownTables);
    	}
    	report.setFinished();
    }
	private void writeReport(File reportFile, Report report) throws IOException {
		Report reportToWrite = report;
		if (report.hasSingleChild())
			reportToWrite = report.firstChild();
		PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(reportFile)));
		output.print(reportToWrite.getHtml());
		output.close();
	}
    private void runSuite(File theSuiteFile, File theReportDiry, Report parentReport,
    		Parse setUpTables, Parse tearDownTables) {
		if (!theSuiteFile.exists() || theSuiteFile.isDirectory())
			throw new RuntimeException("Suite file doesn't exist or is not a file: "+theSuiteFile);
     	Report report = new Report(theSuiteFile.getName(),theReportDiry,parentReport,"",theReportDiry);
		runFile(theSuiteFile,theReportDiry,report,setUpTables,tearDownTables);
		report.setFinished();
	}
    private boolean canRunThisFolder(String name) {
        return !name.equals(FILES) && !name.startsWith(".") && !name.equals("CVS");
    }
    private void runFile(File file, File theReportDiry, Report report, Parse fullSetUpTables, 
    		Parse fullTearDownTables) {
    	String name = file.getName();
    	PrintStream oldOut = System.out;
    	PrintStream oldErr = System.err;
    	ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
    	ByteArrayOutputStream tempErr = new ByteArrayOutputStream();
    	try {
    		if (isXlsFileName(name) || isHtmlFileName(name)) {
    			System.setOut(new PrintStream(tempOut));
    			System.setErr(new PrintStream(tempErr));
    			File reportFile = new File(theReportDiry,reportName(file));
    			if (fileIsLocked(reportFile))
    				throw new RuntimeException("File is locked");
    			Parse setUp = copyParse(fullSetUpTables.more);
    			Parse tearDown = copyParse(fullTearDownTables);
    			Traverse.setContext(theReportDiry);
    			Counts counts;
    			if (isXlsFileName(name))
    				counts = new SpreadsheetRunner(report).run(file,reportFile,setUp,tearDown,batchFitLibrary);
    			else
    				counts = new HtmlRunner(report).runInSuite(file,reportFile,encoding,setUp,tearDown,batchFitLibrary);
    			report.addAssertionCountsForPage(reportFile,counts);
    		} else
    			throw new RuntimeException("Not HTML nor XLS");
    	} catch (Exception e) {
    		ignoreFile(file,e);
    	} finally {
    		reportOutput(name,"out",tempOut.toString());
    		reportOutput(name,"err",tempErr.toString());
    		System.setOut(oldOut);                
    		System.setErr(oldErr);                
    	}
    }
    private static boolean fileIsLocked(File file) {
        return file.exists() && !file.canWrite();
    }
    public static boolean specialFileName(String mixedCaseName) {
        String name = mixedCaseName.toUpperCase();
        return SETUPS.contains(name) || TEARDOWNS.contains(name);
    }
    private Parse appendSetUp(Parse tables, File theInDiry) throws IOException {
        Parse newTables = copyParse(tables);
        gatherTables(theInDiry, SETUPS, newTables.last());
        return newTables;
    }
    private Parse copyParse(Parse parse) {
        return ParseUtility.copyParse(parse);
    }
    private Parse prependTearDown(Parse tables, File theInDiry) throws IOException {
        Parse newTables = new Parse("","",null,null);
        gatherTables(theInDiry, TEARDOWNS, newTables);
        ParseUtility.append(newTables,copyParse(tables));
        return newTables.more;
    }
    private static String reportName(File file) {
        return ReportHtml.reportName(file);
    }
    private Parse gatherTables(File theInDiry, Set matching, Parse endTable) throws FileNotFoundException, IOException {
        File xlsFile = null;
        File htmlFile = null;
        File[] files = theInDiry.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String name = file.getName().toUpperCase();
            if (matching.contains(name)) {
                if (isXlsFileName(name))
                    xlsFile = file;
                else {
                    if (htmlFile != null)
                        throw new RuntimeException("Can't have .html and .htm files in "+
                                theInDiry.getAbsolutePath());
                    htmlFile = file;
                }
                endTable = endTable.last();
            }
        }
        try {
            if (xlsFile != null)
                ParseUtility.append(endTable,new SpreadsheetRunner().collectTable(xlsFile));
        } catch (CustomRunnerException e) {
            ignoreFile(htmlFile,e);
        }
        try {
            if (htmlFile != null)
                ParseUtility.append(endTable,new HtmlRunner().collectTable(htmlFile,encoding));
        } catch (ParseException e) {
            ignoreFile(htmlFile,e);
        }
        return endTable;
    }
    private void ignoreFile(File file, Exception e) {
        System.out.println("Ignored file: "+ file.getAbsolutePath()+" due to: "+e);
    }
    public static boolean isHtmlFileName(String name) {
        String upperName = name.toUpperCase();
        return upperName.endsWith(".HTML") || upperName.endsWith(".HTM");
    }
    public static boolean isXlsFileName(String name) {
        return name.toUpperCase().endsWith(".XLS");
    }
    private void copyCssAndImageFilesFromJar(File theInDiry) throws IOException {
        File filesDiry = new File(theInDiry,FILES);
        if (!filesDiry.exists())
            filesDiry.mkdir();
        File cssDiry = new File(filesDiry,"css");
        if (!cssDiry.exists())
            cssDiry.mkdir();
        createFileIfNeeded(new File(cssDiry,"fitnesse.css"), "css/fitnesse.css");
        
        File imagesDiry = new File(filesDiry,"images");
        if (!imagesDiry.exists())
            imagesDiry.mkdir();
        createFileIfNeeded(new File(imagesDiry,"collapsableClosed.gif"), "images/collapsableClosed.gif");
        createFileIfNeeded(new File(imagesDiry,"collapsableOpen.gif"), "images/collapsableClosed.gif");
    }
    private void createFileIfNeeded(File target, String resource) throws FileNotFoundException, IOException {
        if (target.exists())
            return;
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream reader = classLoader.getResourceAsStream(resource);
        if (reader == null)
        	throw new FitLibraryException("Unable to access resource from jar: "+resource);
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(target));
        while (true) {
            int ch = reader.read();
            if (ch < 0)
                break;
            writer.write(ch);
        }
        reader.close();
        writer.close();
    }
    private void setTestFile(String testDiryName) {
        inDiry = new File(testDiryName);
        reportDiry = new File(inDiry.getParentFile(),"reports");
    }
    private void setFiles(String testDiryName, String reportDiryName) {
        inDiry = new File(testDiryName);
        reportDiry = new File(reportDiryName);
    }
    public void addTestListener(TestListener listener) {
        testListeners.add(listener);
    }
    private void giveFeedbackToUser() {
        for (Iterator it = testListeners.iterator(); it.hasNext(); )
            ((TestListener)it.next()).testComplete(topReport.failing(),topReport.getCounts(),topReport.getAssertionCounts());
    }
    private void reportOutput(String name, String out, String message) {
        for (Iterator it = testListeners.iterator(); it.hasNext(); )
            ((TestListener)it.next()).reportOutput(name,out,message);
    }
    private void suiteFinished() {
        for (Iterator it = testListeners.iterator(); it.hasNext(); )
            ((TestListener)it.next()).suiteComplete();
    }
    public void exit() {
        topReport.exit();
    }
}
