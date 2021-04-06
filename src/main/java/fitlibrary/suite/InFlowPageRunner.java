/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 16/12/2006
*/

package fitlibrary.suite;

import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class InFlowPageRunner {
	private DoEvaluator doEvaluator;
	boolean abandoned = false;
	private boolean stopOnError = false;

	public InFlowPageRunner(DoEvaluator doEvaluator, boolean abandoned) {
		this.doEvaluator = doEvaluator;
		this.abandoned  = abandoned;
		doEvaluator.registerFlowControl(new FlowControl() {
			public void abandon() {
				InFlowPageRunner.this.abandoned  = true;
			}
			public void setStopOnError(boolean stopOnError) {
			}
		});
	}
	public void run(Tables tables, int from, TableListener tableListener) {
		doEvaluator.doNotTearDownAutomatically();
		TestResults testResults = tableListener.getTestResults();
		for (int i = from; i < tables.size(); i++) {
			Table table = tables.table(i);
			if (abandoned || (stopOnError && testResults.problems()))
				table.ignore(testResults);
			else
				doEvaluator.interpretWholeTable(table, tableListener);
			tableListener.tableFinished(table);
		}
		doEvaluator.tearDown(tables.table(0),testResults);
	}
	public void setStopOnError(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}
	public void setAbandon(boolean abandoned) {
		this.abandoned = abandoned;
	}
}
