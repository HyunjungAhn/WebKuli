/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import fit.FitServer;
import fit.Parse;
import fitlibrary.utility.TestResults;

public abstract class ParseNode {
	public final static String PASS = " class=\"pass\"";
	public final static String FAIL = " class=\"fail\"";
	public final static String IGNORE = " class=\"ignore\"";
	public final static String ERROR = " class=\"error\"";
	public Parse parse;
	public static final String ITERATION_START = "<!--ITERATION-->";

	public ParseNode(Parse parse) {
		this.parse = parse;
	}

	public void pass(TestResults testResults) {
		ensureBodyNotNull();
		String resultStr = decorateResultString(parse);

		if (false == parse.body.contains(ITERATION_START)) {
			parse.body = "";
			addTableToCell(parse, "pass", resultStr, false);
		} else {
			addTableToCell(parse, "pass", resultStr, true);
		}

		testResults.pass();
	}

	protected void addTableToCell(Parse cell, String className, String content, boolean upLine) {
		cell.addToBody(ITERATION_START);

		if (true == upLine) {
			cell.addToBody("<hr>");
		}

		cell.addToBody("<div class=" + className + ">" + content + "</div>");
	}

	public void fail(TestResults testResults) {
		ensureBodyNotNull();
		if (!hadError()) {
			String resultStr = decorateResultString(parse);

			if (false == parse.body.contains(ITERATION_START)) {
				parse.body = "";
				addTableToCell(parse, "fail", resultStr, false);
			} else {
				addTableToCell(parse, "fail", resultStr, true);
			}
			
    		testResults.fail();
		}
	}
	
	public void fail(TestResults testResults, String actualResult) {
		ensureBodyNotNull();

		if (!hadError()) {
			String resultStr = decorateResultString(parse);
			resultStr = resultStr + label("expected &nbsp;") + actualResult + label("actual");

			if (false == parse.body.contains(ITERATION_START)) {
				parse.body = "";
				addTableToCell(parse, "fail", resultStr, false);
			} else {
				addTableToCell(parse, "fail", resultStr, true);
			}

			testResults.fail();
		}
	}

	private String decorateResultString(Parse resultParse) {
		String resultStr = resultParse.orgbody;

		if (FitServer.hasSymbol(resultStr)) {
			return resultStr + "=" + FitServer.getSymbol(resultStr);
		}

		return resultStr;
	}

	public void passOrFail(TestResults testResults, boolean right) {
		Parse resultParse = parse.more;
		while (null != resultParse) {
			decorateResultString(resultParse);
			resultParse = resultParse.more;
		}
		if (right)
			pass(testResults);
		else
			fail(testResults);
	}

	public static String label(String string) {
		return " <span class=\"fit_label\">" + string + "</span>";
	}

	public boolean didPass() {
		return tagContains(PASS);
	}

	public boolean didFail() {
		return tagContains(FAIL);
	}

	public boolean wasIgnored() {
		return tagContains(IGNORE);
	}

	public boolean hadError() {
		return tagContains(ERROR);
	}

	protected void ensureBodyNotNull() {
		if (parse.body == null)
			parse.body = "";
	}

	private boolean tagContains(String label) {
		return parse.tag.indexOf(label) >= 0;
	}

	protected abstract void error(TestResults counts, Throwable e);
}