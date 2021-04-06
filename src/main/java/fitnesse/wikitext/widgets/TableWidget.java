// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wikitext.WikiWidget;

public class TableWidget extends ParentWidget {
	public static final String LF = LINE_BREAK_PATTERN;
	public static final String REGEXP = "^!?(?:\\|[^\r\n]*?\\|" + LF + ")+";
	private static final Pattern pattern = Pattern.compile("(!?)(\\|[^\r\n]*?)\\|" + LF);

	public boolean isLiteralTable;
	private int columns = 0;

	public int getColumns() {
		return columns;
	}

	public String asWikiText() throws Exception {
		StringBuffer wikiText = new StringBuffer();
		if (isLiteralTable)
			wikiText.append("!");
		appendTableWikiText(wikiText);
		return wikiText.toString();
	}

	private void appendTableWikiText(StringBuffer wikiText) throws Exception {
		for (WikiWidget rowWidget : getChildren()) {
			TableRowWidget row = (TableRowWidget)rowWidget;
			wikiText.append("|");
			appendRowWikiText(wikiText, row);
			wikiText.append("\n");
		}
	}

	private void appendRowWikiText(StringBuffer wikiText, TableRowWidget row) throws Exception {
		for (WikiWidget cellWidget : row.getChildren()) {
			TableCellWidget cell = (TableCellWidget)cellWidget;
			appendCellWikiText(wikiText, cell);
			wikiText.append("|");
		}
	}

	private void appendCellWikiText(StringBuffer wikiText, TableCellWidget cell) throws Exception {
		for (WikiWidget contentWidget : cell.getChildren())
			wikiText.append(contentWidget.asWikiText());
	}

	public TableWidget(ParentWidget parent, String text) throws Exception {
		super(parent);
		Matcher match = pattern.matcher(text);
		if (match.find()) {
			isLiteralTable = "!".equals(match.group(1));
			addRows(text);
			getMaxNumberOfColumns();
		} else
			; // throw Exception?
	}

	private void getMaxNumberOfColumns() {
		for (WikiWidget widget : children) {
			TableRowWidget rowWidget = (TableRowWidget)widget;
			columns = Math.max(columns, rowWidget.getColumns());
		}
	}

	public String render() throws Exception {
		Random random = new Random();
		final String id = random.nextLong() + "";
		final String divCollapseRimTag = "<div class=\"collapse_rim\">";
		final String divMetaTag = "<div style=\"float: right;\" class=\"meta\">" + "<a href=\"javascript:expandAll();\">Expand All</a> | " + "<a href=\"javascript:collapseAll();\">Collapse All</a></div>\n";
		final String aHrefTag = "<a href=\"javascript:toggleCollapsable('" + id + "');\">\n" + "<img src=\"/files/images/collapsableOpen.gif\" class=\"left\" " + "id=\"img" + id + "\"/>\n</a>\n";

		final Pattern startPattern = Pattern.compile("(start_[a-z]*)");
		final Pattern endPattern = Pattern.compile("(end_[a-z]*)");
		final Pattern commentPattern = Pattern.compile("//(.*)</td>");
		String startComment = "", endComment = "";
		StringBuffer html = new StringBuffer("<table border=\"1\" cellspacing=\"0\">\n");

		html.append(childHtml()).append("</table>\n");
		String htmlString = html.toString();
		String className = "";
		if (htmlString.contains("start_sequence") || htmlString.contains("end_sequence")) {
			className = "hidden";
//			startComment = "<!--∆START_NOTES-->";
//			endComment = "<!--∆END_NOTES-->";
		} else {
			className = "collapsable";
		}
		
		String divCollapsableTag = "<div class=\"" + className + "\" id=\"" + id + "\">\n";

		Matcher startMatcher = startPattern.matcher(htmlString);

		if (true == startMatcher.find()) {
			String title = startMatcher.group(0);

			Matcher commentMatcher = commentPattern.matcher(htmlString);
			if (true == commentMatcher.find()) {
				title = commentMatcher.group(1);
			}

			String spanMetaTag = "<span class=\"meta\">" + title + "</span>\n";
			htmlString = startComment + divCollapseRimTag + divMetaTag + aHrefTag + spanMetaTag + divCollapsableTag + htmlString;
		}

		Matcher endMatcher = endPattern.matcher(htmlString);
		if (true == endMatcher.find()) {
			htmlString += "</div>\n</div>\n" + endComment;
		}

		return htmlString;
	}

	public void addRows(String text) throws Exception {
		Matcher match = pattern.matcher(text);
		if (match.find()) {
			new TableRowWidget(this, match.group(2), isLiteralTable);
			addRows(text.substring(match.end()));
		}
	}

	public void setLiteralTable(boolean isLiteralTable) {
		this.isLiteralTable = isLiteralTable;
	}
}
