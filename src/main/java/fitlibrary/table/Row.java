/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import fit.Parse;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TestResults;

public class Row extends ParseNode {
    private boolean rowIsHidden = false;
    
	public Row(Parse parse) {
        super(parse);
    }
    public Row() {
        this(new Parse("tr","",null,null));
    }
    public int size() {
    	if (parse.parts == null)
    		return 0;
        return parse.parts.size();
    }
    public Cell cell(int i) {
        if (!cellExists(i))
            throw new MissingCellsException("");
        return new Cell(parse.parts.at(i));
    }
    public boolean cellExists(int i) {
        return i >= 0 && i < size();
    }
    public String toString() {
        return "Row["+ParseUtility.toString(parse.parts)+"]";
    }
    public void pass(TestResults testResults) {
    	if (rowIsHidden)
    		System.out.println("Bug: colouring a cell in a hidden table");
    	super.pass(testResults);
    }
    public void fail(TestResults testResults) {
    	if (rowIsHidden)
    		System.out.println("Bug: colouring a cell in a hidden table");
    	super.fail(testResults);
    }
    public void error(TestResults testResults, Throwable e) {
        cell(0).error(testResults,e);
    }
    public String text(int i) {
        return cell(i).text();
    }
    public void missing(TestResults testResults) {
        cell(0).expectedElementMissing(testResults);
    }
    public Cell addCell() {
    	Cell cell = new Cell("");
		addCell(cell);
		return cell;
    }
    public void addCell(Cell cell) {
    	if (rowIsHidden)
    		System.out.println("Bug: Adding a cell to a hidden row in a table");
        if (parse.parts == null)
            parse.parts = cell.parse;
        else
            parse.parts.last().more = cell.parse;
    }
	public Cell addCell(String text) {
        Cell cell = new Cell(text);
        addCell(cell);
        return cell;
	}
    public Cell addCell(String text, int cols) {
        Cell cell = new Cell(text,cols);
        addCell(cell);
        return cell;
    }
    public boolean equals(Object object) {
        if (!(object instanceof Row))
            return false;
        Row other = (Row)object;
        if (other.size() != size())
            return false;
        for (int i = 0; i < size(); i++)
            if (!cell(i).equals(other.cell(i)))
                return false;
        return true;
    }
	public Row rowFrom(int i) {
		// Can be an empty row
		return new Row(new Parse("tr","",parse.parts.at(i),null));
	}
	public Cell last() {
		return cell(size()-1);
	}
	public void ignore(TestResults testResults) {
		for (int i = 0; i < size(); i++)
			cell(i).ignore(testResults);
	}
	public boolean isEmpty() {
		return size() == 0;
	}
	public void setIsHidden() {
		this.rowIsHidden  = true;
		for (int i = 0; i < size(); i++)
			cell(i).setIsHidden();
	}
	public Cell lastCell() {
		return cell(size()-1);
	}
}
