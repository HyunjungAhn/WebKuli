/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import fit.Fixture;
import fit.Parse;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.table.NestedTableExpectedException;
import fitlibrary.exception.table.SingleNestedTableExpected;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TestResults;

public class Cell extends ParseNode {
    private boolean cellIsInHiddenRow = false;
    
	public Cell(Parse parse) {
        super(parse);
    }
    public Cell() {
        this("");
    }
    public Cell(String cellText) {
        this(new Parse("td",cellText,null,null));
    }
    public Cell(String cellText, int cols) {
        this(new Parse("td colspan="+cols,cellText,null,null));
	}
	public Cell(Tables innerTables) {
		this(new Parse("td","",innerTables.parse,null));
	}
	public String text() {
        if (parse.body == null)
            return "";
        return parse.text();
    }
    public String textLower() {
        return text().toLowerCase();
    }
    public boolean matchesText(String text) {
        return text().toLowerCase().equals(text.toLowerCase());
    }
    public boolean isBlank() {
        return text().equals("");
    }
    public boolean hasEmbeddedTable() {
        return parse.parts != null;
    }
    public Tables innerTables() {
        if (!hasEmbeddedTable())
            throw new NestedTableExpectedException();
        return new Tables(parse.parts);
    }
    public Cell copy() {
        return new Cell(ParseUtility.copyParse(parse));
    }
    public boolean equals(Object object) {
        if (!(object instanceof Cell))
            return false;
        Cell other = (Cell)object;
        return parse.body.equals(other.parse.body);
    }
    public void fail(TestResults testResults, String msg, String actualResult) {
    	if (cellIsInHiddenRow)
    		System.out.println("Bug: colouring a cell in a hidden table");
        fail(testResults, actualResult);
    }
    public void expectedElementMissing(TestResults testResults) {
        fail(testResults);
        addToBody(label("missing"));
    }
    public void actualElementMissing(TestResults testResults) {
        fail(testResults);
        addToBody(label("surplus"));
    }
    public void actualElementMissing(TestResults testResults, String value) {
        fail(testResults);
        parse.body = Fixture.gray(Fixture.escape(value.toString()));
        addToBody(label("surplus"));
    }
    public void pass(TestResults testResults) {
    	if (cellIsInHiddenRow)
    		System.out.println("Bug: colouring a cell in a hidden table");
    	super.pass(testResults);
    }
    public void fail(TestResults testResults) {
    	if (cellIsInHiddenRow)
    		System.out.println("Bug: colouring a cell in a hidden table");
    	super.fail(testResults);
    }
    public void error(TestResults testResults, Throwable e) {
    	if (cellIsInHiddenRow)
    		System.out.println("Bug: colouring a cell in a hidden table");
        ensureBodyNotNull();
        addToBody(exceptionMessage(e));
        parse.addToTag(ERROR);
        testResults.exception();
    }
	private static String exceptionMessage(Throwable exception) {
        while (exception.getClass().equals(InvocationTargetException.class))
            exception = ((InvocationTargetException) exception).getTargetException();
        if (exception instanceof IgnoredException)
            return "";
        if (exception instanceof FitLibraryException)
            return "<hr/>" + Fixture.label(Traverse.escapeHtml(exception.getMessage()));
        final StringWriter buf = new StringWriter();
        exception.printStackTrace(new PrintWriter(buf));
        return "<hr><pre><div class=\"fit_stacktrace\">"
            + (buf.toString()) + "</div></pre>";
    }
    public void ignore(TestResults testResults) {
    	if (cellIsInHiddenRow)
    		System.out.println("Bug: colouring a cell in a hidden table");
        ensureBodyNotNull();
//        if (parse.tag.indexOf("class") >= 0)
//        	throw new RuntimeException("Duplicate cell class in tag. Tag is already"+
//        			parse.tag.substring(1,parse.tag.length()-2));
        parse.addToTag(IGNORE);
        testResults.ignore();
    }
    public boolean isIgnored() {
    	boolean result = false;
    	
    	if (parse.tag.indexOf("class") >= 0)
    		result = true;
    	
    	return result;
    }
    public Table getEmbeddedTable() {
        Tables tables = getEmbeddedTables();
        if (tables.size() != 1)
        	throw new SingleNestedTableExpected();
		return tables.table(0);
    }
    public Tables getEmbeddedTables() {
        if (!hasEmbeddedTable())
            throw new NestedTableExpectedException();
		return new Tables(parse.parts);
    }
    public String toString() {
        if (hasEmbeddedTable())
            return "Cell["+ParseUtility.toString(parse.parts)+"]";
        return text();
    }
    public void wrongHtml(TestResults counts, String actual) {
        fail(counts);
        addToBody(label("expected") + "<hr>" + actual
                + label("actual"));
    }
    private void addToBody(String msg) {
        if (hasEmbeddedTable()) {
            if (parse.parts.more == null)
                parse.parts.trailer = msg;
            else
                parse.parts.more.leader += msg;
        }
        else
            parse.addToBody(msg);
    }
	public void setText(String text) {
		parse.body = text;
	}
	public String fullText() {
		return parse.body;
	}
	public void setUnvisitedEscapedText(String s) {
		setUnvisitedText(Fixture.escape(s));
	}
	public void setUnvisitedText(String s) {
		setText(Fixture.gray(s));
	}
	public void passIfBlank(TestResults counts) {
		if (isBlank())
			pass(counts);
		else
			fail(counts,"");
	}
	public void passIfNotEmbedded(TestResults counts) {
		if (!hasEmbeddedTable()) // already coloured
			pass(counts);
	}
	public void setIsHidden() {
		this.cellIsInHiddenRow = true;
	}
	public void setInnerTables(Tables tables) {
		parse.parts = tables.parse();
	}
	public void extraColumns(int i) {
		parse.addToTag(" colspan=\""+(i+1)+"\"");
	}
}
