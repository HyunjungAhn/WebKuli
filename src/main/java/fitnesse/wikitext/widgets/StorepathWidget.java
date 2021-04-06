// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.WidgetBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StorepathWidget extends ParentWidget implements WidgetWithTextArgument
{
	public static final String REGEXP = "^!store [^\r\n]*";
	private static final Pattern pattern = Pattern.compile("^!store (.*)");
	private String pathText;

	public StorepathWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			pathText = match.group(1);
			addChildWidgets(pathText);
		}
	}

	public WidgetBuilder getBuilder()
	{
		return WidgetBuilder.variableEvaluatorWidgetBuilder;
	}

	public String render() throws Exception
	{
		return HtmlUtil.metaText("!store " + childHtml());
	}

	public String asWikiText() throws Exception
	{
		return "!store " + pathText;
	}

	public String getText() throws Exception
	{
		return childHtml();
	}
}