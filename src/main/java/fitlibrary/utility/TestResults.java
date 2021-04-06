/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.utility;

import fit.Counts;

public class TestResults {
	public static final String PASS_COLOUR = "#cfffcf";
	public static final String FAIL_COLOUR = "#ffcfcf";
	public static final String IGNORE_COLOR = "#efefef";
	public static final String ERROR_COLOUR = "#ffffcf";
	private Counts counts = new Counts();

	public TestResults(Counts counts) {
		this.counts = counts;
	}
	public TestResults() {
		//
	}
	public void pass() {
		counts.right++;
	}
	public void fail() {
		counts.wrong++;
	}
	public void exception() {
		counts.exceptions++;
	}
	public void ignore() {
		counts.ignores++;
	}
	public void clear() {
		counts.right = 0;
		counts.wrong = 0;
		counts.ignores = 0;
		counts.exceptions = 0;
	}
	public void add(TestResults otherResults) {
		counts.tally(otherResults.counts);
	}
	public boolean passed() {
		return counts.right > 0 && counts.wrong == 0 & counts.exceptions == 0;
	}
	public boolean failed() {
		return counts.wrong > 0;
	}
	public boolean errors() {
		return counts.exceptions > 0;
	}
	public boolean problems() {
		return counts.wrong + counts.exceptions > 0;
	}
	public String toString() {
		return counts.toString();
	}
	public boolean matches(String rights, String wrongs, String ignores, String exceptions) {
		return	cellValue(rights) == counts.right &&
				cellValue(wrongs) == counts.wrong &&
				cellValue(ignores) == counts.ignores &&
				cellValue(exceptions) == counts.exceptions;
		}
	private int cellValue(String s) {
		return Integer.parseInt(s);
	}
	public Counts getCounts() {
		return counts;
	}
	public String asHtmlTable() {
		String colour = IGNORE_COLOR;
		if (passed())
			colour = PASS_COLOUR;
		else if (problems())
			if (counts.exceptions > 0)
				colour = ERROR_COLOUR;
			else
				colour = FAIL_COLOUR;
		return "<html><table><tr><td "+backgroundColour(colour)+">"+counts.toString()+"</td></tr></table><html>";
	}
	public static String replaceCss(String html) {
	    String result = html;
		result = result.replaceAll("class=\"pass\"",  backgroundColour(PASS_COLOUR));
	    result = result.replaceAll("class=\"fail\"",  backgroundColour(FAIL_COLOUR));
	    result = result.replaceAll("class=\"ignore\"",backgroundColour(IGNORE_COLOR));
	    result = result.replaceAll("class=\"error\"", backgroundColour(ERROR_COLOUR));
		return result;
	}
	private static String backgroundColour(String colour) {
		return "bgcolor=\""+colour+"\"";
	}
}
