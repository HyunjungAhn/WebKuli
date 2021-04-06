// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.velocity.VelocityContext;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.SaveRecorder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.MockingPageCrawler;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.Utils;

public class EditResponder implements SecureResponder {
	public static final String CONTENT_INPUT_NAME = "pageContent";
	public static final String TIME_STAMP = "saveTime";
	public static final String TICKET_ID = "ticketId";
	public static final String IS_TEST = "isTest";
	public static final String DEVICE_TYPE = "deviceType";

	protected String content;
	protected WikiPage page;
	protected WikiPage root;
	protected PageData pageData;
	protected Request request;

	public EditResponder() {
	}

	public Response makeResponse(FitNesseContext context, Request request) throws Exception {
		boolean nonExistent = request.hasInput("nonExistent");
		return doMakeResponse(context, request, nonExistent);
	}

	public Response makeResponseForNonExistentPage(FitNesseContext context, Request request) throws Exception {
		return doMakeResponse(context, request, true);
	}

	protected Response doMakeResponse(FitNesseContext context, Request request, boolean firstTimeForNewPage) throws Exception {
		initializeResponder(context.root, request);

		SimpleResponse response = new SimpleResponse();
		String resource = request.getResource();
		WikiPagePath path = PathParser.parse(resource);
		PageCrawler crawler = context.root.getPageCrawler();
		if (!crawler.pageExists(root, path)) {
			crawler.setDeadEndStrategy(new MockingPageCrawler());
			page = crawler.getPage(root, path);
		} else
			page = crawler.getPage(root, path);

		pageData = page.getData();
		content = createPageContent();

		String html = doMakeHtml(resource, context, firstTimeForNewPage);

		response.setContent(html);
		response.setMaxAge(0);

		return response;
	}

	protected void initializeResponder(WikiPage root, Request request) {
		this.root = root;
		this.request = request;
	}

	protected String createPageContent() throws Exception {
		return pageData.getContent();
	}

	public String makeHtml(String resource, FitNesseContext context) throws Exception {
		return doMakeHtml(resource, context, false);
	}

	public String doMakeHtml(String resource, FitNesseContext context, boolean firstTimeForNewPage) throws Exception {
		HtmlPage html = context.htmlPageFactory.newPage();
		WikiPagePath fullPath = page.getPageCrawler().getFullPath(page);
		String fullPathName = PathParser.render(fullPath);
		String title = firstTimeForNewPage ? "Page doesn't exist. Edit " : "Edit ";
		html.title.use(title + resource + ":");
		html.head.add("<script src=\"/files/javascript/cursor.js\" type=\"text/javascript\"></script>");
		html.body.addAttribute("onload", "document.f." + CONTENT_INPUT_NAME + ".focus()");
		HtmlTag header = makeHeader(resource, title);
		html.header.use(header);
		html.header.add("<a style=\"font-size:small;\" onclick=\"cursorPosition();popup('applyPatternPopup')\"> [apply pattern]</a>");
		html.main.use(makeEditForm(resource, firstTimeForNewPage, context.defaultNewPageContent));

		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("page_name", page.getName());
		velocityContext.put("full_path", fullPathName);
		
		String rootPath = System.getProperty("FIT_ROOT");
		if (rootPath != null) {
			velocityContext.put("select_context", createSelectTag(rootPath + "/PatternList"));
		} else {
			velocityContext.put("select_context", createSelectTag("./FitNesseRoot/PatternList"));
		}
		
		html.main.add(VelocityFactory.translateTemplate(velocityContext, "applyPatternPagePopup.vm"));

		return html.html();
	}

	private String[] getPatternFileName(String dir) {
		File file = new File(dir);
		return file.list();
	}

	private String readFileContent(String dir) {
		StringBuffer content = new StringBuffer();

		try {
			FileInputStream fileInputStream = new FileInputStream(dir + "/content.txt");
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			BufferedReader br = new BufferedReader(inputStreamReader);
			String value = "";

			while ((value = br.readLine()) != null) {
				content.append(value);
				content.append("\n");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content.toString();
	}

	public String createSelectTag(String dir) {
		StringBuffer content = new StringBuffer();
		content.append("<select name=\"patternlist\" onchange=\"showContent(this);\">");
		content.append("<option value=\"\">-------</option>");

		for (String eachValue : getPatternFileName(dir)) {
			if (true == eachValue.matches(".*\\..*")) {
				continue;
			}
			content.append("<option value=");
			content.append("\"" + readFileContent(dir + "/" + eachValue) + "\">");
			content.append(eachValue);
			content.append("</option>");
		}

		content.append("</select>");

		return content.toString();
	}

	public HtmlTag makeHeader(String resource, String title) throws Exception {
		return HtmlUtil.makeBreadCrumbsWithPageType(resource, title + "Page:");
	}

	private void printFile(String data) {
		FileWriter writer;
		try {
			writer = new FileWriter("c:\\dev\\backup\\test2010040802.txt", true);
			writer.write(data);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public HtmlTag makeEditForm(String resource, boolean firstTimeForNewPage, String defaultNewPageContent) throws Exception {
		HtmlTag form = new HtmlTag("form");
		form.addAttribute("name", "f");
		form.addAttribute("action", resource);
		form.addAttribute("method", "post");
		form.add(HtmlUtil.makeInputTag("hidden", "position", "0"));
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "saveData"));
		form.add(HtmlUtil.makeInputTag("hidden", TIME_STAMP, String.valueOf(SaveRecorder.timeStamp())));
		form.add(HtmlUtil.makeInputTag("hidden", TICKET_ID, String.valueOf((SaveRecorder.newTicket()))));
		if (request.hasInput("redirectToReferer") && request.hasHeader("Referer")) {
			String redirectUrl = request.getHeader("Referer").toString();
			int questionMarkIndex = redirectUrl.indexOf("?");
			if (questionMarkIndex > 0)	
				redirectUrl = redirectUrl.substring(0, questionMarkIndex);
			redirectUrl += "?" + request.getInput("redirectAction").toString();
			form.add(HtmlUtil.makeInputTag("hidden", "redirect", redirectUrl));
		}

		form.add(createTextarea(firstTimeForNewPage, defaultNewPageContent));
		form.add(createButtons());
		form.add(createOptions());
		form.add("<div class=\"hints\"><br />Hints:\n<ul>"
			+ "<li>Use alt+s (Windows) or control+s (Mac OS X) to save your changes. Or, tab from the text area to the \"Save\" button!</li>\n"
			+ "<li>Grab the lower-right corner of the text area to increase its size (works with some browsers).</li>\n"
			+ "</ul></div>");

		TagGroup group = new TagGroup();
		group.add(form);

		return group;
	}

	public HtmlTag createOptions() throws Exception {
		HtmlTag options = HtmlUtil.makeDivTag("edit_options");
		options.add(makeScriptOptions());
		return options;
	}

	private HtmlTag makeScriptOptions() {
		TagGroup scripts = new TagGroup();

		includeJavaScriptFile("/files/javascript/textareaWrapSupport.js", scripts);

		return scripts;
	}

	private HtmlTag createButtons() throws Exception {
		HtmlTag buttons = HtmlUtil.makeDivTag("edit_buttons");
		buttons.add(makeSaveButton());
		buttons.add(makeScriptButtons());
		return buttons;
	}

	private HtmlTag makeScriptButtons() {
		TagGroup scripts = new TagGroup();

		includeJavaScriptFile("/files/javascript/SpreadsheetTranslator.js", scripts);
		includeJavaScriptFile("/files/javascript/spreadsheetSupport.js", scripts);
		includeJavaScriptFile("/files/javascript/WikiFormatter.js", scripts);
		includeJavaScriptFile("/files/javascript/wikiFormatterSupport.js", scripts);
		includeJavaScriptFile("/files/javascript/fitnesse.js", scripts);

		return scripts;
	}

	protected void includeJavaScriptFile(String jsFile, TagGroup scripts) {
		HtmlTag scriptTag = HtmlUtil.makeJavascriptLink(jsFile);
		scripts.add(scriptTag);
	}

	protected HtmlTag makeSaveButton() {
		HtmlTag saveButton = HtmlUtil.makeInputTag("submit", "save", "Save");
		saveButton.addAttribute("tabindex", "2");
		saveButton.addAttribute("accesskey", "s");
		return saveButton;
	}

	private HtmlTag createTextarea(boolean firstTimeForNewPage, String defaultNewPageContent) {
		HtmlTag textarea = new HtmlTag("textarea");
		textarea.addAttribute("class", CONTENT_INPUT_NAME + " no_wrap");
		textarea.addAttribute("wrap", "off");
		textarea.addAttribute("id", CONTENT_INPUT_NAME);
		textarea.addAttribute("name", CONTENT_INPUT_NAME);
		textarea.addAttribute("rows", "30");
		textarea.addAttribute("cols", "70");
		textarea.addAttribute("tabindex", "1");
		textarea.add(Utils.escapeHTML(firstTimeForNewPage ? defaultNewPageContent : content));
		return textarea;
	}

	public SecureOperation getSecureOperation() {
		return new SecureReadOperation();
	}
}
