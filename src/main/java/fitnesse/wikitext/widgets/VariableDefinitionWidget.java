// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import fitnesse.html.HtmlUtil;

public class VariableDefinitionWidget extends ParentWidget
{
	public static final String REGEXP = "^!define [\\w\\.]+ +(?:(?:\\{[^}]*\\})|(?:\\([^)]*\\)))";
	private static final Pattern pattern =
	  Pattern.compile("^!define ([\\w\\.]+) +([\\{\\(])(.*)[\\}\\)]",
		                Pattern.DOTALL + Pattern.MULTILINE);
	public static Pattern getPattern()
	{
		return pattern;
	}
	public String name;
	public String value;

	
	public boolean isNumber(String value) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher match = pattern.matcher(value);
		return match.find();
	}
	
	public VariableDefinitionWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			name = match.group(1);
			value = match.group(3);
		}
		
		//etc definition pattern
		//FIT_ROOT
		this.parent.addVariable("FIT_ROOT", System.getProperty("FIT_ROOT"));
		
		//Debug option
		if("debug".equals(name) && true == isNumber(value))
		{
			name = "COMMAND_PATTERN";
			value = "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + value + " -cp %p %m";
		}
	}

	public String render() throws Exception
	{
		this.parent.addVariable(name, value);
		return HtmlUtil.metaText("variable defined: " + name + "=" + value);
	}

	public String asWikiText() throws Exception
	{
		String text = "!define " + name + " ";
		if(value.indexOf("{") == -1)
			text += "{" + value + "}";
		else
			text += "(" + value + ")";
		return text;
	}
}
