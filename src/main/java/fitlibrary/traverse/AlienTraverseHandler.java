/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse;

import java.util.Map;

import fit.Fixture;
import fitlibrary.table.Table;
import fitlibrary.utility.TestResults;

public class AlienTraverseHandler {
	public boolean isAlienTraverse(Object result) {
		return result instanceof Fixture;
	}
	public void doTable(Object result, Table table, TestResults testResults) {
		Fixture fixture = (Fixture) result;
		fixture.counts = testResults.getCounts();
		fixture.doTable(table.parse);
	}
}
