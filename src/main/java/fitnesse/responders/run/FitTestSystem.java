// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fit.FitServer;
import fitnesse.FitNesseContext;
import fitnesse.components.CommandRunningFitClient;
import fitnesse.html.HtmlTag;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.widgets.ParentWidget;

import java.net.UnknownHostException;
import java.util.Map;

public class FitTestSystem extends TestSystem {
	private CommandRunningFitClient client;
	private FitNesseContext context;

	public FitTestSystem(FitNesseContext context, WikiPage page, TestSystemListener listener) {
		super(page, listener);
		this.context = context;
	}

	protected ExecutionLog createExecutionLog(String classPath, Descriptor descriptor) throws Exception {
		String command = buildCommand(descriptor, classPath);
		Map<String, String> environmentVariables = createClasspathEnvironment(classPath);
		client = new CommandRunningFitClient(this, command, context.port, environmentVariables, context.socketDealer, fastTest);
		return new ExecutionLog(page, client.commandRunner);
	}

	public void bye() throws Exception {
		client.done();
		client.join();
	}

	public String runTestsAndGenerateHtml(PageData pageData) throws Exception {
		String html = pageData.getHtml();
		String pageName = pageData.getWikiPage().getName();

		if (html.length() == 0) {
			client.send(emptyPageContent);
		}
		else {
			if (FitServer.parallelTestSuite) {
				if (!FitServer.iFrameDisp) {
					client.send(makeFullPageNameTag(pageData).html() + addIframeTag(pageName) + html);
					FitServer.iFrameDisp = true;
				} else {
					client.send(makeFullPageNameTag(pageData).html() + html);
				}
			} else {
				client.send(html);
			}
		}

		return html;
	}

	private String addIframeTag(String pageName) {
		StringBuffer gridIframe = new StringBuffer("<iframe ");
		StringBuffer hostName;
		try {
			hostName = new StringBuffer(java.net.InetAddress.getLocalHost().getHostName());

			gridIframe.append("id").append("=\"").append(pageName).append("\" ");
			gridIframe.append("name").append("=\"").append(pageName).append("\" ");
			gridIframe.append("src").append("=\"http://").append(hostName).append(":4444/console\" ");
			gridIframe.append("style").append("=\"border:none;width:100%;height:400px\">");
			gridIframe.append("</iframe>");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return gridIframe.toString();
	}

	private HtmlTag makeFullPageNameTag(PageData pageData) throws Exception {
		StringBuffer fullPageName = new StringBuffer();
		WikiPage wikiPage = pageData.getWikiPage();
		WikiPagePath fullPath = wikiPage.getPageCrawler().getFullPath(wikiPage);

		fullPageName.append("[").append(fullPath).append("]");

		HtmlTag h3Tag = new HtmlTag("h3");
		h3Tag.add(fullPageName.toString());

		return h3Tag;
	}

	public boolean isSuccessfullyStarted() {
		return client.isSuccessfullyStarted();
	}

	public void kill() throws Exception {
		client.kill();
	}

	public void start() throws Exception {
		client.start();
	}
}