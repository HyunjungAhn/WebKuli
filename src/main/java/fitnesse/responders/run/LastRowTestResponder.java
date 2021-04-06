// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.http.Response;
import fitnesse.responders.LayerMenuGenerator;
import fitnesse.responders.run.formatters.*;
import fitnesse.responders.testHistory.PageHistory;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LastRowTestResponder extends TestResponder implements SecureResponder {
	private static final String REPLACEMENT = "§";
	private static final String IMPORT_TOOLTIP = "<script src=\"/files/javascript/tooltip.js\" type=\"text/javascript\"></script>";
	private static final String RUN_VISIBLEDIVLAYER = "<script>visibleDivLayer();checkMenuPosition();</script>";
	private static LinkedList<TestEventListener> eventListeners = new LinkedList<TestEventListener>();
	protected PageData data;
	protected CompositeFormatter formatters;
	private boolean isClosed = false;
	
	public Logger logger = Logger.getRootLogger();

	private boolean fastTest = false;
	private boolean remoteDebug = false;
	protected TestSystem testSystem;
	public static String wikiContents = "";
	public static String pageName = "";

	public LastRowTestResponder() {
		super();
		BasicConfigurator.configure();
		formatters = new CompositeFormatter();
	}

	protected void doSending() throws Exception {
		writeIntervalData("lastRowTest=1");
		fastTest |= request.hasInput("debug");
		remoteDebug |= request.hasInput("remote_debug");
		data = page.getData();
		createFormatterAndWriteHead();

		if (false == response.isXmlFormat()) {
			WikiPagePath fullPath = page.getPageCrawler().getFullPath(page);
			String fullPathName = PathParser.render(fullPath);
			String contents = data.getContent();
			
			if (contents.trim().endsWith("!|end_iterate|")) {
				writeIntervalData("lastRowTest=2");
			}
			contents = contents.replaceAll("'", REPLACEMENT);

			addToResponse("<input type='hidden' id='contents' name='contents' value='" + contents + "'/>");
			addToResponse("<input type='hidden' id='pageName' name='pageName' value='" + fullPathName + "'/>");
			addToResponse(IMPORT_TOOLTIP);
			addToResponse(new LayerMenuGenerator().toString());

			if (true == fit.FitServer.enableSmartBox) {
				addToResponse(RUN_VISIBLEDIVLAYER);
			}
		}

		sendPreTestNotification();
		performExecution();
		writeIntervalData("lastRowTest=0");
		int exitCode = formatters.getErrorCount();
		closeHtmlResponse(exitCode);
	}

	private void writeIntervalData(String data) throws IOException {
		FileWriter writer = new FileWriter("test_type.conf", false);
		writer.write(data);
		writer.close();
	}

	protected void createFormatterAndWriteHead() throws Exception {
		if (response.isXmlFormat())
			addXmlFormatter();
		else
			addHtmlFormatter();

		addTestHistoryFormatter();
		formatters.writeHead(getTitle());
	}

	String getTitle() {
		return "Test Results";
	}

	void addXmlFormatter() throws Exception {
		XmlFormatter.WriterFactory writerSource = new XmlFormatter.WriterFactory() {
			public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) {
				return makeResponseWriter();
			}
		};
		formatters.add(new XmlFormatter(context, page, writerSource));
	}

	protected Writer makeResponseWriter() {
		return new Writer() {
			public void write(char[] cbuf, int off, int len) {
				String fragment = new String(cbuf, off, len);
				try {
					response.add(fragment.getBytes("UTF-8"));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void flush() throws IOException {
			}

			public void close() throws IOException {
			}
		};
	}

	void addHtmlFormatter() throws Exception {
		BaseFormatter formatter = new TestHtmlFormatter(context, page, context.htmlPageFactory) {
			@Override
			protected void writeData(String output) throws Exception {
				addToResponse(output);
			}
		};
		formatters.add(formatter);
	}

	protected void addTestHistoryFormatter() throws Exception {
		HistoryWriterFactory writerFactory = new HistoryWriterFactory();
		//    formatters.add(new XmlFormatter(context, page, writerFactory));
		formatters.add(new PageHistoryFormatter(context, page, writerFactory));
	}

	protected void sendPreTestNotification() throws Exception {
		for (TestEventListener eventListener : eventListeners) {
			eventListener.notifyPreTest(this, data);
		}
	}

	protected void performExecution() throws Exception {
		List<WikiPage> test2run = new SuiteContentsFinder(page, null, root).makePageListForSingleTest();

		MultipleTestsRunner runner = new MultipleTestsRunner(test2run, context, page, formatters);
		runner.setFastTest(fastTest);
		runner.setDebug(isRemoteDebug());

		if (isEmpty(page))
			formatters.addMessageForBlankHtml();

		runner.executeTestPages();
		//FitServer.lastRowTest = false;
	}

	private boolean isEmpty(WikiPage page) throws Exception {
		return page.getData().getContent().length() == 0;
	}

	public SecureOperation getSecureOperation() {
		return new SecureTestOperation();
	}

	public static void registerListener(TestEventListener listener) {
		eventListeners.add(listener);
	}

	public void setFastTest(boolean fastTest) {
		this.fastTest = fastTest;
	}

	public boolean isFastTest() {
		return fastTest;
	}

	public void addToResponse(byte[] output) throws Exception {
		if (!isClosed()) {
			response.add(output);
		}
	}

	public void addToResponse(String output) throws Exception {
		if (!isClosed()) {
			response.add(output);
		}
	}

	synchronized boolean isClosed() {
		return isClosed;
	}

	synchronized void setClosed() {
		isClosed = true;
	}

	void closeHtmlResponse(int exitCode) throws Exception {
		if (!isClosed()) {
			setClosed();
			response.closeChunks();
			response.addTrailingHeader("Exit-Code", String.valueOf(exitCode));
			response.closeTrailer();
			response.close();
		}
	}

	void closeHtmlResponse() throws Exception {
		if (!isClosed()) {
			setClosed();
			response.closeChunks();
			response.close();
		}
	}

	boolean isRemoteDebug() {
		return remoteDebug;
	}

	public Response getResponse() {
		return response;
	}

	public static class HistoryWriterFactory implements XmlFormatter.WriterFactory {
		public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws Exception {
			File resultPath = new File(PageHistory.makePageHistoryFileName(context, page, counts, time));
			File resultDirectory = new File(resultPath.getParent());
			resultDirectory.mkdirs();
			File resultFile = new File(resultDirectory, resultPath.getName());
			return new FileWriter(resultFile);
		}
	}
}
