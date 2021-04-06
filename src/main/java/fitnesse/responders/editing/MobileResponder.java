// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.components.RecentChanges;
import fitnesse.components.SaveRecorder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class MobileResponder implements SecureResponder {
	protected static final String EMPTY = "";
	protected static final String WHITESPACE = " ";
	public static ContentFilter contentFilter;
	protected String user;
	protected long ticketId;
	protected String deviceType;
	protected String savedContent;
	protected PageData data;
	protected long editTimeStamp;
	protected String isTest;
	protected Document doc = null;
	protected List<WikiContents> wikiContentsList = new ArrayList<WikiContents>();
	protected String keyValue = EMPTY;
	protected String arguments = EMPTY;
	protected static List<String> commander = new ArrayList<String>();

	static {
		commander.add("Touch");
		commander.add("TouchLeft");
		commander.add("TouchRight");
		commander.add("Switch");
		commander.add("Slide");
		commander.add("Scroll");
		commander.add("ScrollRight");
		commander.add("ScrollLeft");
		commander.add("ScrollUp");
		commander.add("ScrollDown");
		commander.add("Verify");
		commander.add("InputText");
		commander.add("Shake");
		commander.add("Move");
		commander.add("VScroll");
		commander.add("Pause");
		commander.add("WaitFor");
	}

	public class WikiContents {
		private String command = WHITESPACE;
		private String className = WHITESPACE;
		private String monkeyId = WHITESPACE;
		private String arguments = WHITESPACE;

		public WikiContents() {
		}

		public boolean isEmpty() {
			if (toString().equals("!| | | | |\n\n")) {
				return true;
			} else {
				return false;
			}
		}

		public String toString() {
			return "!|" + this.getCommand() + "|" + this.getClassName() + "|" + this.getMonkeyId() + "|" + this.getArguments() + "|" + "\n\n";
		}

		protected void setCommand(String command) {
			this.command = command;
		}

		protected String getCommand() {
			return command;
		}

		protected void setClassName(String className) {
			this.className = className;
		}

		protected String getClassName() {
			return className;
		}

		protected void setMonkeyId(String monkeyId) {
			this.monkeyId = monkeyId;
		}

		protected String getMonkeyId() {
			return monkeyId;
		}

		protected void setArguments(String arguments) {
			this.arguments = arguments;
		}

		protected String getArguments() {
			return arguments;
		}
	}

	private Document readDocument(String xml) {
		try {
			SAXBuilder builder = new SAXBuilder();
			org.jdom.Document result = builder.build(new StringReader(xml));
			return result;
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void convertIphoneToWiki() {
		doc = readDocument(savedContent);
		Element root = doc.getRootElement();
		WikiContents wikiContents = new WikiContents();
		wikiContents = getiPhoneXmlContents(wikiContents, root);
		makeXmlDictCase(wikiContents);
		savedContent = EMPTY;

		for (WikiContents contents : wikiContentsList) {
			savedContent += contents;
		}

		savedContent = savedContent.substring(0, savedContent.length() - 2);
	}

	public WikiContents getiPhoneXmlContents(WikiContents wikiContents, Element element) {
		List<?> childElements = element.getChildren();
		Iterator<?> iterator = childElements.iterator();

		while (iterator.hasNext()) {
			Element iteratorElements = (Element)iterator.next();

			if (iteratorElements.getName().equals("key")) {
				makeXmlKeyCase(iteratorElements);
			}

			if (iteratorElements.getName().equals("dict")) {
				wikiContents = makeXmlDictCase(wikiContents);
			}

			if (iteratorElements.getName().equals("string")) {
				wikiContents = makeXmlStringCase(wikiContents, iteratorElements);
			}

			wikiContents = getiPhoneXmlContents(wikiContents, iteratorElements);
		}

		return wikiContents;
	}

	private void makeXmlKeyCase(Element element) {
		keyValue = element.getValue();
	}

	private WikiContents makeXmlDictCase(WikiContents wikiContents) {
		if (!arguments.equals(EMPTY)) {
			wikiContents.setArguments(arguments.substring(0, arguments.length() - 1));
			arguments = EMPTY;
		}

		if (!wikiContents.isEmpty()) {
			wikiContentsList.add(wikiContents);
		}

		return new WikiContents();
	}

	private WikiContents makeXmlStringCase(WikiContents wikiContents, Element element) {
		String stringValue = element.getValue();

		if (stringValue.equals(EMPTY)) {
			stringValue = WHITESPACE;
		}

		if (keyValue.equals("command")) {
			wikiContents.setCommand(stringValue);
		} else if (keyValue.equals("className")) {
			wikiContents.setClassName(stringValue);
		} else if (keyValue.equals("monkeyID")) {
			wikiContents.setMonkeyId(stringValue);
		} else {
			arguments += stringValue + ",";
		}

		return wikiContents;
	}

	public Response makeResponse(FitNesseContext context, Request request) throws Exception {
		editTimeStamp = getEditTime(request);
		deviceType = getDeviceType(request);
		ticketId = getTicketId(request);
		isTest = (String)request.getInput(EditResponder.IS_TEST);
		String resource = request.getResource();
		WikiPage page = getPage(resource, context);
		data = page.getData();
		user = request.getAuthorizationUsername();

		if (editsNeedMerge())
			return new MergeResponder(request).makeResponse(context, request);
		else {
			savedContent = (String)request.getInput(EditResponder.CONTENT_INPUT_NAME);

			if (deviceType.equals("iphone")) {
				convertIphoneToWiki();
			} else if (deviceType.equals("android")) {
				convertAndroidToWiki();
			} else if (deviceType.equals("winmobile")) {
				convertWinMobileToWiki();
			}

			if (contentFilter != null && !contentFilter.isContentAcceptable(savedContent, resource))
				return makeBannedContentResponse(context, resource);
			else
				return saveEdits(request, page);
		}
	}

	private void convertWinMobileToWiki() {
		// 구현 예정
	}

	private void convertAndroidToWiki() {
		// 구현 예정
	}

	private Response makeBannedContentResponse(FitNesseContext context, String resource) throws Exception {
		SimpleResponse response = new SimpleResponse();
		HtmlPage html = context.htmlPageFactory.newPage();
		html.title.use("Edit " + resource);
		html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "Banned Content"));
		html.main.use(new HtmlTag("h3", "The content you're trying to save has been " + "banned from this site.  Your changes will not be saved!"));
		response.setContent(html.html());
		return response;
	}

	private Response saveEdits(Request request, WikiPage page) throws Exception {
		Response response = new SimpleResponse();
		setData();
		VersionInfo commitRecord = page.commit(data);
		response.addHeader("Previous-Version", commitRecord.getName());
		RecentChanges.updateRecentChanges(data);

		if (request.hasInput("redirect"))
			response.redirect(request.getInput("redirect").toString());
		else {
			if ("true".equals(isTest)) {
				response.redirect(request.getResource() + "?test");
			} else {
				response.redirect(request.getResource());
			}
		}

		return response;
	}

	private boolean editsNeedMerge() throws Exception {
		return SaveRecorder.changesShouldBeMerged(editTimeStamp, ticketId, data);
	}

	private long getTicketId(Request request) {
		if (!request.hasInput(EditResponder.TICKET_ID))
			return 0;
		String ticketIdString = (String)request.getInput(EditResponder.TICKET_ID);
		return Long.parseLong(ticketIdString);
	}

	private String getDeviceType(Request request) {
		if (!request.hasInput(EditResponder.DEVICE_TYPE))
			return EMPTY;
		String deviceTypeString = (String)request.getInput(EditResponder.DEVICE_TYPE);
		return deviceTypeString;
	}

	private long getEditTime(Request request) {
		if (!request.hasInput(EditResponder.TIME_STAMP))
			return 0;
		String editTimeStampString = (String)request.getInput(EditResponder.TIME_STAMP);
		long editTimeStamp = Long.parseLong(editTimeStampString);
		return editTimeStamp;
	}

	private WikiPage getPage(String resource, FitNesseContext context) throws Exception {
		WikiPagePath path = PathParser.parse(resource);
		PageCrawler pageCrawler = context.root.getPageCrawler();
		WikiPage page = pageCrawler.getPage(context.root, path);
		if (page == null)
			page = pageCrawler.addPage(context.root, PathParser.parse(resource));
		return page;
	}

	private void setData() throws Exception {
		data.setContent(savedContent);
		SaveRecorder.pageSaved(data, ticketId);
		if (user != null)
			data.setAttribute(PageData.LAST_MODIFYING_USER, user);
		else
			data.removeAttribute(PageData.LAST_MODIFYING_USER);
	}

	public SecureOperation getSecureOperation() {
		return new SecureWriteOperation();
	}
}
