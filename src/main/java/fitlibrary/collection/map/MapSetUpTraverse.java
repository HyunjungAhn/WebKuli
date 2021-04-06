/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.collection.map;

import java.util.HashMap;
import java.util.Map;

import fitlibrary.exception.table.RowWrongWidthException;
import fitlibrary.parser.Parser;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.traverse.Traverse;
import fitlibrary.typed.Typed;
import fitlibrary.utility.TestResults;

public class MapSetUpTraverse extends Traverse {
    private Map theMap = new HashMap();
    protected Parser keyParser;
    protected Parser valueParser;

    protected MapSetUpTraverse() {
    	//
    }
    public MapSetUpTraverse(Typed keyTyped, Typed valueTyped) {
        this.keyParser = keyTyped.parser(this);
        this.valueParser = valueTyped.parser(this);
    }
    public Object interpret(Table table, TestResults testResults) {
    	try {
    		for (int rowNo = 1; rowNo < table.size(); rowNo++)
    			processRow(table.row(rowNo), testResults);
    	} catch (Exception e) {
    		table.error(testResults,e);
    	}
    	return theMap;
    }
    protected void processRow(Row row, TestResults testResults) throws Exception {
        try {
            if (row.size() != 2)
                throw new RowWrongWidthException(2);
            theMap.put(keyParser.parseTyped(row.cell(0),testResults).getSubject(),
            		valueParser.parseTyped(row.cell(1),testResults).getSubject());
        } catch (Exception e) {
            row.error(testResults,e);
        }
    }
    public Map getResults() {
        return theMap;
    }
}
