/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.collection.set;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fitlibrary.closure.MethodTarget;
import fitlibrary.collection.CollectionTraverse;
import fitlibrary.exception.table.ExtraCellsException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.utility.TestResults;

/**
 * A more flexible alternative to RowFixture:
 * o The collection may be provided as an array, Collection or Iterator
 * o The column names may refer to properties of an element Object.
 * o The elements don't have to be of related types. Where a column doesn't apply
 *   to a particular element, the expected value must be blank.
 * 
 * For large sets, this is a more expensive algorithm than used in RowFixture.
 */
public class SetTraverse extends CollectionTraverse {
    public SetTraverse(Object sut) {
		super(sut);
	}
	public SetTraverse(Object sut, Object actuals) {
		super(sut,actuals);
	}
	public SetTraverse() {
		super(null);
	}
	public void interpret(final Row row, List theActuals, TestResults testResults) throws Exception {
    	if (theActuals.isEmpty()) {
			row.missing(testResults);
			return;   		
    	}
    	int rowLength = row.size();
		MethodTarget[] columnBindings = (MethodTarget[])theActuals.get(0);
		if (rowLength < columnBindings.length)
			throw new MissingCellsException("SetTraverse");
		if (rowLength > columnBindings.length)
			throw new ExtraCellsException("SetTraverse");
		List matchingActuals = theActuals;
		for (int column = 0; column < columnBindings.length; column++) {
			matchingActuals = matchOnColumn(row,matchingActuals,column,testResults);
			if (matchingActuals.isEmpty()) {
				row.missing(testResults);
				return;
			}
			if (matchingActuals.size() == 1) {
				MethodTarget[] theOne = (MethodTarget[])matchingActuals.get(0);
				matchRow(row,theOne,testResults);
				theActuals.remove(theOne);
				return;
			}
		}
		// There may be > 1 actuals that matched, so match on the first one.
		if (!matchingActuals.isEmpty()) {
			MethodTarget[] theOne = (MethodTarget[])matchingActuals.get(0);
			matchRow(row,theOne,testResults);
			theActuals.remove(theOne);
		}
	}
    /* Returns the subset of actuals that match on the given column of the row */
	private List matchOnColumn(final Row row, List theActuals, final int column, TestResults testResults) {
        final Cell cell = row.cell(column);
		List results = new ArrayList();
		for (Iterator it = theActuals.iterator(); it.hasNext(); ) {
			MethodTarget[] columnBindings = (MethodTarget[])it.next();
			MethodTarget getter = columnBindings[column];
			if (getter == null) { // Doesn't apply
				if (cell.isBlank())
					results.add(columnBindings);
			}
			else {
				try {
					if (getter.matches(cell,testResults))
						results.add(columnBindings);
				} catch (Exception e) {
					//
				}
			}
		}
		return results;
	}
}
