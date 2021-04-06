package fitlibrary.suite;

import fit.FixtureBridge;
import fitlibrary.table.Tables;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class BatchFitLibrary {
    private boolean first = true;
	private SuiteRunner suiteRunner;
	private TableListener tableListener = new TableListener();
	private Reportage reportage;

	public BatchFitLibrary() {
		this(new DefaultReportage());
	}
	public BatchFitLibrary(Reportage reportage) {
		this.reportage = reportage;
	}
	public BatchFitLibrary(TableListener tableListener) {
		this.tableListener = tableListener;
	}
	public TestResults doTables(Tables theTables) {
		tableListener.clearTestResults();
		if (first) {
			first = false;
			FixtureBridge fixtureBridge = new FixtureBridge();
			fixtureBridge.counts = tableListener.getTestResults().getCounts();
			Object firstObjectOfSuite = fixtureBridge.firstObject(theTables.parse(),tableListener.getTestResults());
			if (firstObjectOfSuite == null) {
				theTables.ignoreAndFinished(tableListener);
				return tableListener.getTestResults();
			}
			if (firstObjectOfSuite instanceof SuiteEvaluator) {
				suiteRunner = new IntegratedSuiteRunner((SuiteEvaluator)firstObjectOfSuite);
				reportage.showAllReports();
			} else
				suiteRunner = new IndependentSuiteRunner(firstObjectOfSuite);
			suiteRunner.runFirstStorytest(theTables,tableListener);
		} else
			suiteRunner.runStorytest(theTables,tableListener);
		return tableListener.getTestResults();
	}
	public void doTables(Tables theTables, TableListener listener) {
		this.tableListener = listener;
		doTables(theTables);
	}
	public void exit() {
		suiteRunner.exit();
	}
	public static class DefaultReportage implements Reportage {
		public void showAllReports() {
		}
	}
}
