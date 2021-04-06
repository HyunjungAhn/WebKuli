/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import fit.Parse;
import fitlibrary.exception.table.MissingRowException;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class Table extends ParseNode {
    private int firstErrorRow = 0;
    
	public Table() {
        super(new Parse("table","",null,null));
    }
	public Table(Parse parse) {
        super(parse);
    }
    public Table(Row row) {
    	super(new Parse("table","",row.parse,null));
	}
	public int size() {
        return parse.parts.size();
    }
    public Row row(int i) {
        if (!rowExists(i))
            throw new MissingRowException("");
        return new Row(parse.parts.at(i));
    }
    public boolean rowExists(int i) {
        return i >= 0 && i < size();
    }
    public String toString() {
        return "Table["+ParseUtility.toString(parse.parts)+"]";
    }
    public void wrong(TestResults testResults, String msg) {
        row(firstErrorRow).cell(0).fail(testResults,msg);
    }
    public void missing(TestResults testResults) {
        row(firstErrorRow).missing(testResults);
    }
    public void ignore(TestResults testResults) {
        row(firstErrorRow).ignore(testResults);
    }
    public void error(TestResults testResults, Throwable e) {
        row(firstErrorRow).error(testResults,e);
    }
	public void error(TableListener tableListener, Exception e) {
		error(tableListener.getTestResults(),e);
	}
    public Row lastRow() {
        return row(size()-1);
    }
    public void addRow(Row row) {
        if (parse.parts == null)
            parse.parts = row.parse;
        else
            parse.parts.last().more = row.parse;
    }
    public Row newRow() {
        Row row = new Row();
        addRow(row);
        return row;
    }
    public void finished(TableListener listener) {
        listener.tableFinished(this);
    }
	public Table withDummyFirstRow() {
		Parse firstRow = new Parse("tr", "", new Parse("td","empty",null,null), parse.parts);
		Parse pseudoTable = new Parse("table", "", firstRow, null);
		Table table = new Table(pseudoTable);
		table.setFirstRowIsHidden();
		return table;
	}
	private void setFirstRowIsHidden() {
		this.firstErrorRow  = 1;
		row(0).setIsHidden();
	}
	public Parse parse() {
		return parse;
	}
	public int phaseBreaks() {
		int count = 0;
		int hr = 0;
		while (true) {
			hr = parse.leader.indexOf("<hr>",hr);
			if (hr < 0)
				break;
			hr++;
			count++;
		}
		return count;
	}
	public int phaseBoundaryCount() {
		return (parse.leader+"|").split("<hr>").length-1;
	}
	public void addToLeader(String s) {
		parse.leader += s;
	}
	public void removeNext() {
		parse.more = parse.more.more;
	}
	public String getLeading() {
		return parse.leader;
	}
	public String getTrailing() {
		return parse.trailer;
	}
	public Tables getTables() {
		return new Tables(parse);
	}
	public void insertTable(int offset, Table table) {
		table.evenUpRows();
		Parse insertPoint = parse.at(offset);
		table.parse().more = insertPoint.more;
		insertPoint.more = table.parse();
	}
	public void evenUpRows() {
		int max = 0;
		for (int r = 0; r < size(); r++)
			max = Math.max(max, row(r).size());
		for (int r = 0; r < size(); r++) {
			Row row = row(r);
			if (row.size() < max) {
				row.lastCell().extraColumns(max-row.size());
			}
		}
	}
}
