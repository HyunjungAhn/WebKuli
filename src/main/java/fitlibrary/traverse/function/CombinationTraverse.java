/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.function;

import java.util.ArrayList;
import java.util.List;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.closure.LookupMethodTarget;
import fitlibrary.exception.table.ExtraCellsException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.parser.Parser;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.utility.TestResults;

public class CombinationTraverse extends FunctionTraverse {
	private List topValues = new ArrayList();
    private boolean methodOK = false;
    private CalledMethodTarget methodTarget = null;
    private Parser firstParser;

    public CombinationTraverse() {
    	// No SUT
    }
    public CombinationTraverse(Object sut) {
    	super(sut);
    }
	public Object interpret(Table table, TestResults testResults) {
		bindFirstRowToTarget(table.row(1),testResults);
		for (int i = 2; i < table.size(); i++)
			processRow(table.row(i),testResults);
		return null;
	}
	public void bindFirstRowToTarget(Row row, TestResults testResults) {
		Parser secondParser = null;
		try {
			methodTarget = LookupMethodTarget.findTheMethodMapped("combine",2,this);
			Parser[] parameterParsers = methodTarget.getParameterParsers();
			firstParser = parameterParsers[0];
			secondParser = parameterParsers[1];
		} catch (Exception e) {
			row.error(testResults, e);
			return;
		}
        int rowLength = row.size();
        for (int i = 1; i < rowLength; i++) {
            Cell cell = row.cell(i);
            try {
                topValues.add(secondParser.parseTyped(cell,testResults).getSubject());
            } catch (Exception e) {
                cell.error(testResults,e);
                return;
            }
        }
		methodOK = true;
	}
    public void processRow(Row row, TestResults testResults) {
		if (!methodOK) {
			row.ignore(testResults);
			return;
		}
		try {
			Object arg1 = firstParser.parseTyped(row.cell(0),testResults).getSubject();
			if (row.size()-1 < topValues.size())
				throw new MissingCellsException("CombinationTraverse");
			if (row.size()-1 > topValues.size())
				throw new ExtraCellsException("CombinationTraverse");
            for (int i = 1; i < row.size(); i++) {
                Object result = methodTarget.invoke(new Object[]{arg1,topValues.get(i-1)});
                methodTarget.checkResult(row.cell(i),result,true,false, testResults);
            }
		} catch (Exception e) {
			row.error(testResults,e);
			return;
		}
	}
}
