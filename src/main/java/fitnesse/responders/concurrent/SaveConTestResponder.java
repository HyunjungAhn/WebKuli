//Copyright (C) 2009 by NHN Corporation. All rights reserved.
package fitnesse.responders.concurrent;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.components.SaveRecorder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.responders.editing.EditResponder;
import fitnesse.responders.editing.MergeResponder;
import fitnesse.responders.editing.SaveResponder;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.io.File;
import java.io.PrintWriter;

public class SaveConTestResponder extends SaveResponder {
	public static ContentFilter contentFilter;

	private String user;
	private long ticketId;
	private String savedContent;
	private PageData data;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception {
		String resource = request.getResource();
		WikiPage page = getPage(resource, context);
		data = page.getData();
		user = request.getAuthorizationUsername();
		
		data.getVariable(TestSystem.CONTEST_PROP);
				
		if (editsNeedMerge(request))
			return new MergeResponder(request).makeResponse(context, request);
		else 
			return saveEdits(request, page);
	}

	private Response saveEdits(Request request, WikiPage page) throws Exception {
		Response response = new SimpleResponse();
		String resource = request.getResource();
		setData();
		VersionInfo commitRecord = page.commit(data);
		response.addHeader("Previous-Version", commitRecord.getName());


		String contestProp = data.getVariable(TestSystem.CONTEST_PROP);
		String saveData = (String) request
				.getInput(ConTestResponder.CONTENT_INPUT_NAME);
		File file = new File(contestProp);
		PrintWriter pw = new PrintWriter(file);
		pw.print(saveData);
		pw.close();

		response.redirect(resource);
		return response;
	}

	private boolean editsNeedMerge(Request request) throws Exception {
		String editTimeStampString = (String) request.getInput(EditResponder.TIME_STAMP);
		long editTimeStamp = Long.parseLong(editTimeStampString);

		String ticketIdString = (String) request.getInput(EditResponder.TICKET_ID);
		ticketId = Long.parseLong(ticketIdString);

		return SaveRecorder.changesShouldBeMerged(editTimeStamp, ticketId, data);
	}

	private WikiPage getPage(String resource, FitNesseContext context)
			throws Exception {
		WikiPagePath path = PathParser.parse(resource);
		PageCrawler pageCrawler = context.root.getPageCrawler();
		WikiPage page = pageCrawler.getPage(context.root, path);
		if (page == null)
			page = pageCrawler
					.addPage(context.root, PathParser.parse(resource));
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
