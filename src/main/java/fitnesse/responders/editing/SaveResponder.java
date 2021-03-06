// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import java.io.FileWriter;
import java.io.IOException;

import fit.FitServer;
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

public class SaveResponder implements SecureResponder {
	public static ContentFilter contentFilter;

	private String user;
	private long ticketId;
	private String savedContent;
	private PageData data;
	private long editTimeStamp;
	private String isTest;

	private void printFile(String data) {
		FileWriter writer;
		try {
			writer = new FileWriter("ntaflog.log", true);
			writer.write(data);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Response makeResponse(FitNesseContext context, Request request) throws Exception {
		StringBuffer buffer = new StringBuffer();

		editTimeStamp = getEditTime(request);
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

			buffer.append("tickId : ").append(ticketId).append(" time : ").append(editTimeStamp).append(" isTest : ").append(isTest).append(" content : ").append(savedContent).append("\n");
			printFile(buffer.toString());

			if (contentFilter != null && !contentFilter.isContentAcceptable(savedContent, resource))
				return makeBannedContentResponse(context, resource);
			else
				return saveEdits(request, page);
		}

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
				response.redirect(request.getResource() + "?lastrowtest");
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
