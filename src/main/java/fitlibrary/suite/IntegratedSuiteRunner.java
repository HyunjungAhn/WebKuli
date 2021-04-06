/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 10/12/2006
*/

package fitlibrary.suite;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.typed.NonGenericTypedFactory;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class IntegratedSuiteRunner implements SuiteRunner {
	private SuiteEvaluator suiteEvaluator;
	protected boolean abandoned = false;

	public IntegratedSuiteRunner(SuiteEvaluator suiteEvaluator) {
		this.suiteEvaluator = suiteEvaluator;
		suiteEvaluator.registerFlowControl(new FlowControl() {
			public void abandon() {
				abandoned  = true;
			}
			public void setStopOnError(boolean stopOnError) {
			}
		});
	}
	public void runFirstStorytest(Tables tables, TableListener tableListener) {
		Table firstTable = tables.table(0);
//		suiteEvaluator.doNotTearDownAutomatically();
		suiteMethod("suiteSetUp", firstTable, tableListener.getTestResults());
		suiteEvaluator.interpret(firstTable,tableListener.getTestResults());
		tableListener.tableFinished(firstTable);
		runEachStorytest(tables,1,tableListener);
	}
	private void suiteMethod(String methodName, Table firstTable, TestResults results) {
		try {
			CalledMethodTarget methodTarget = new NonGenericTypedFactory().asTypedObject(suiteEvaluator).optionallyFindMethodOnTypedObject(methodName,0,suiteEvaluator,false);
			if (methodTarget != null)
				methodTarget.invoke();
		} catch (Exception e) {
			firstTable.error(results, e);
		}
	}
	public void runStorytest(Tables tables, TableListener tableListener) {
		runEachStorytest(tables,0,tableListener);
	}
	private void runEachStorytest(Tables tables, int fromTable, TableListener tableListener) {
		abandoned = false;
		for (int i = fromTable; i < tables.size(); i++) {
			Table table = tables.table(i);
			if (abandoned)
				tableListener.tableFinished(table);
			else {
				TestResults testResults = tableListener.getTestResults();
				Object result = suiteEvaluator.interpretWholeTable(table,tableListener);
				tableListener.tableFinished(table);
				if (abandoned)
					table.ignore(testResults);
				if (result instanceof DoEvaluator) {
					DoEvaluator doEvaluator = (DoEvaluator)result;
					new InFlowPageRunner(doEvaluator,abandoned).run(tables,i+1,tableListener);
					break;
				}
			}
		}
		tableListener.storytestFinished();
	}
	public void exit() {
		// We're unable to see any problems on tearDown!
		Row row = new Row();
		row.addCell();
		suiteMethod("suiteTearDown",new Table(row),new TestResults());
	}
}
