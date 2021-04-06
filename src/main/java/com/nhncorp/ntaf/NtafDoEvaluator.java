/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package com.nhncorp.ntaf;

import fit.RunningState;
import fitlibrary.suite.FlowControl;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Evaluator;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public interface NtafDoEvaluator extends Evaluator {
	Object interpretInFlow(Table table, TestResults testResults);

	Object interpretWholeTable(Table table, TableListener tableListener);

	Object interpretInnerTables(Tables tables, int fromRow, int toRow, TableListener tableListener,
		RunningState runningState);

	void setUp(Table firstTable, TestResults testResults);

	void tearDown(Table firstTable, TestResults testResults);

	void doNotTearDownAutomatically();

	void registerFlowControl(FlowControl control);
}
