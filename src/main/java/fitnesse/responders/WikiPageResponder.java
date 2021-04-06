// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fit.FitServer;
import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.editing.EditResponder;
import fitnesse.threadlocal.ThreadLocalUtil;
import fitnesse.wiki.*;
import fitnesse.wikitext.widgets.Constants;
import fitnesse.wikitext.widgets.TableRowWidget;

import org.apache.velocity.VelocityContext;

public class WikiPageResponder implements SecureResponder {
	private static final String REPLACEMENT = "§";
	private static final String HTTP_PROTOCOL = "http://";
	private static final String EMPTY = "";
	private static final String INCLUDE_TEARDOWN = "!include -teardown .*";
	private static final String INCLUDE_SETUP = "!include -setup .*";
	private static final String NEWLINE = "\\n";
	private static final String RUN_VISIBLEDIVLAYER = "<script>visibleDivLayer();checkMenuPosition();</script>";
	private static final String ADD_CHILD = "<a style=\"font-size:small;\" onclick=\"popup('addChildPopup')\"> [add child]</a>";
	private static final String DIV_STYLE = "style='position:absolute;left:45px;top:170px;width:108px;z-index:10001;height:137px;visibility:hidden;'";
	protected WikiPage page;
	protected PageData pageData;
	protected String pageTitle;
	protected Request request;
	protected PageCrawler crawler;

	public WikiPageResponder() {
		TableRowWidget.currRow = 0;
		TableRowWidget.currUrl = "";
	}

	public WikiPageResponder(WikiPage page) throws Exception {
		this.page = page;
		pageData = page.getData();
	}

	public Response makeResponse(FitNesseContext context, Request request) throws Exception {
		loadPage(request.getResource(), context);
		if (page == null)
			return notFoundResponse(context, request);
		else
			return makePageResponse(context);
	}

	protected void loadPage(String pageName, FitNesseContext context) throws Exception {
		WikiPagePath path = PathParser.parse(pageName);
		crawler = context.root.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		page = crawler.getPage(context.root, path);
		if (page != null)
			pageData = page.getData();
	}

	private Response notFoundResponse(FitNesseContext context, Request request) throws Exception {
		if (dontCreateNonExistentPage(request))
			return new NotFoundResponder().makeResponse(context, request);
		return new EditResponder().makeResponseForNonExistentPage(context, request);
	}

	private boolean dontCreateNonExistentPage(Request request) {
		String dontCreate = (String)request.getInput("dontCreatePage");
		return dontCreate != null && (dontCreate.length() == 0 || Boolean.parseBoolean(dontCreate));
	}

	private SimpleResponse makePageResponse(FitNesseContext context) throws Exception {
		try {
			pageTitle = PathParser.render(crawler.getFullPath(page));
			ThreadLocalUtil.setValue(Constants.ENTRY_PAGE, pageTitle);
			String html = makeHtml(context);

			SimpleResponse response = new SimpleResponse();
			response.setMaxAge(0);
			response.setContent(html);
			return response;
		} finally {
			ThreadLocalUtil.clear();
		}
	}

	public String makeHtml(FitNesseContext context) throws Exception {
		WikiPage page = pageData.getWikiPage();
		HtmlPage html = context.htmlPageFactory.newPage();
		WikiPagePath fullPath = page.getPageCrawler().getFullPath(page);
		String fullPathName = PathParser.render(fullPath);

		html.title.use(fullPathName);
		html.header.use(HtmlUtil.makeBreadCrumbsWithCurrentPageNotLinked(fullPathName));
		html.header.add(ADD_CHILD);
		html.actions.use(HtmlUtil.makeActions(page.getActions()));
		SetupTeardownIncluder.includeInto(pageData);
		html.main.use(generateHtml(pageData));

		String contents = pageData.getContent();

		if (false == contents.isEmpty()) {
			contents = removeSetUpAndTearDown(contents);
		}

		addHtmlContentsForWebKit(html, contents);
		VelocityContext velocityContext = new VelocityContext();

		velocityContext.put("page_name", page.getName());
		velocityContext.put("full_path", fullPathName);
		html.main.add(VelocityFactory.translateTemplate(velocityContext, "addChildPagePopup.vm"));
		handleSpecialProperties(html, page);

		return html.html();
	}

	private void addHtmlContentsForWebKit(HtmlPage html, String contents) throws Exception {
		html.main.add("<input type='hidden' id='contents' name='contents' value='" + contents + "'/>");
		html.main.add(new LayerMenuGenerator().toString());
		html.main.add("<input type='hidden' id='currRow' name='currRow' value=''/>");
		html.main.add("<input type='hidden' id='popMenu' name='popMenu' value='false'/>");
		html.main.add("<div id='SMARTBOX' name='SMARTBOX' " + DIV_STYLE + " />");
		html.main.add("<div id='TARGET_HTML' name='TARGET_HTML' " + DIV_STYLE + " />");
		
		if (true == fit.FitServer.enableSmartBox) {
			html.main.add(RUN_VISIBLEDIVLAYER);
		}
	}

	private String removeSetUpAndTearDown(String contents) {
		contents = contents.replaceFirst(NEWLINE, EMPTY);
		contents = contents.replaceAll(INCLUDE_SETUP + NEWLINE, EMPTY);
		contents = contents.replaceAll(INCLUDE_TEARDOWN + NEWLINE, EMPTY);

		if (contents.length() >= 1) {
			contents = contents.substring(0, contents.length() - 1);
		}

		contents = contents.replaceAll("'", REPLACEMENT);

		return contents;
	}

	/* hook for subclasses */
	protected String generateHtml(PageData pageData) throws Exception {
		return HtmlUtil.makePageHtmlWithHeaderAndFooter(pageData);
	}

	private void handleSpecialProperties(HtmlPage html, WikiPage page) throws Exception {
		WikiImportProperty.handleImportProperties(html, page, pageData);
	}

	public SecureOperation getSecureOperation() {
		return new SecureReadOperation();
	}
}