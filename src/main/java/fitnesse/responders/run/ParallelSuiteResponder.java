// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fit.FitServer;
import fitnesse.responders.run.formatters.*;

public class ParallelSuiteResponder extends TestResponder {
	String getTitle() {
		return "Suite Results";
	}

	void addHtmlFormatter() throws Exception {
		BaseFormatter formatter = new SuiteHtmlFormatter(context, page, context.htmlPageFactory) {
			protected void writeData(String output) throws Exception {
				addToResponse(output);
			}
		};
		formatters.add(formatter);
	}

	protected void addTestHistoryFormatter() throws Exception {
		HistoryWriterFactory source = new HistoryWriterFactory();
		formatters.add(new PageHistoryFormatter(context, page, source));
		formatters.add(new SuiteHistoryFormatter(context, page, source));
	}

	protected void performExecution() throws Exception {
		FitServer.parallelTestSuite = true;
		SuiteFilter filter = new SuiteFilter(getSuiteTagFilter(), getNotSuiteFilter(), getSuiteFirstTest());
		SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
		ParallelMultipleTestsRunner runner = new ParallelMultipleTestsRunner(suiteTestFinder.getAllPagesToRunForThisSuite(), context, page, formatters);
		runner.setDebug(isRemoteDebug());
		runner.executeTestPages();
		FitServer.parallelTestSuite = false;
		FitServer.iFrameDisp = false;
	}

	private String getSuiteTagFilter() {
		return request != null ? (String)request.getInput("suiteFilter") : null;
	}

	private String getNotSuiteFilter() {
		return request != null ? (String)request.getInput("excludeSuiteFilter") : null;
	}

	private String getSuiteFirstTest() throws Exception {
		String startTest = null;
		if (request != null) {
			startTest = (String)request.getInput("firstTest");
		}

		if (startTest != null) {
			String suiteName = page.getPageCrawler().getFullPath(page).toString();
			if (startTest.indexOf(suiteName) != 0) {
				startTest = suiteName + "." + startTest;
			}
		}

		return startTest;
	}
}
