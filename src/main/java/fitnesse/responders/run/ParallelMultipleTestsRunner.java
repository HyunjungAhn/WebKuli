// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.components.ClassPathBuilder;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.responders.run.TestSystem.Descriptor;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.util.*;

public class ParallelMultipleTestsRunner implements TestSystemListener, Stoppable {
	private final ResultsListener resultsListener;
	private final FitNesseContext fitNesseContext;
	private final WikiPage page;
	private final List<WikiPage> testPagesToRun;
	private boolean isFastTest = false;
	private boolean isRemoteDebug = false;

	private Map<String, WikiPage> processingMap = new TreeMap<String, WikiPage>();
	private WikiPage currentTest = null;
	public static boolean isAllTestComplete = false;
	private TestSystemGroup testSystemGroup = null;
	private TestSystem currentTestSystem = null;
	private boolean isStopped = false;
	private String stopId = null;
	private PageListSetUpTearDownSurrounder surrounder;
	private String pageName = "";

	private class PagesByTestSystem extends HashMap<TestSystem.Descriptor, LinkedList<WikiPage>> {
		private static final long serialVersionUID = 1L;
	}

	public ParallelMultipleTestsRunner(final List<WikiPage> testPagesToRun, final FitNesseContext fitNesseContext, final WikiPage page, final ResultsListener resultsListener) {
		this.testPagesToRun = testPagesToRun;
		this.resultsListener = resultsListener;
		this.page = page;
		this.fitNesseContext = fitNesseContext;

		surrounder = new PageListSetUpTearDownSurrounder(fitNesseContext.root);
	}

	public void setDebug(boolean isDebug) {
		isRemoteDebug = isDebug;
	}

	public void setFastTest(boolean isFastTest) {
		this.isFastTest = isFastTest;
	}

	public void executeTestPages() {
		try {
			internalExecuteTestPages();
			resultsListener.allTestingComplete();
		} catch (Exception exception) {
			//hoped to write exceptions to log file but will take some work.
			exception.printStackTrace(System.out);
			exceptionOccurred(exception);
		}
	}

	private void internalExecuteTestPages() throws Exception {
		synchronized (this) {
			testSystemGroup = new TestSystemGroup(fitNesseContext, page, this);
			stopId = fitNesseContext.runningTestingTracker.addStartedProcess(this);
		}
		testSystemGroup.setFastTest(isFastTest);

		resultsListener.setExecutionLogAndTrackingId(stopId, testSystemGroup.getExecutionLog());
		PagesByTestSystem pagesByTestSystem = makeMapOfPagesByTestSystem();
		announceTotalTestsToRun(pagesByTestSystem);

		for (TestSystem.Descriptor descriptor : pagesByTestSystem.keySet()) {
			executePagesInTestSystem(descriptor, pagesByTestSystem);
		}

		fitNesseContext.runningTestingTracker.removeEndedProcess(stopId);
	}

	private void executePagesInTestSystem(TestSystem.Descriptor descriptor, PagesByTestSystem pagesByTestSystem) throws Exception {
		List<WikiPage> pagesInTestSystem = pagesByTestSystem.get(descriptor);

		startTestSystemAndExecutePages(descriptor, pagesInTestSystem);
	}

	private void startTestSystemAndExecutePages(TestSystem.Descriptor descriptor, List<WikiPage> testSystemPages) throws Exception {
		executeTestSystemPages(testSystemPages, descriptor);
	}

	private void executeTestSystemPages(List<WikiPage> pagesInTestSystem, Descriptor descriptor) throws Exception {
		LinkedList<ParallelTest> parallelTests = new LinkedList<ParallelMultipleTestsRunner.ParallelTest>();
		String buildClassPath = buildClassPath();

		ParallelTest rootParallelTest = startRootPage(pagesInTestSystem, descriptor, buildClassPath);
		startSubPage(pagesInTestSystem, descriptor, parallelTests, buildClassPath);
		waitForTestSystemToSendResults();
		
		rootParallelTest.getTestSystem().bye();
		for (ParallelTest parallelTest : parallelTests) {
			parallelTest.getTestSystem().bye();
		}
	}

	private void startSubPage(List<WikiPage> pagesInTestSystem, Descriptor descriptor, LinkedList<ParallelTest> parallelTests, String buildClassPath) throws Exception {
		TestSystem testSystem;

		for (WikiPage testPage : pagesInTestSystem) {
			currentTestSystem = testSystemGroup.startTestSystem(descriptor, buildClassPath);
			testSystem = currentTestSystem;
			resultsListener.testSystemStarted(testSystem, descriptor.testSystemName, descriptor.testRunner);

			parallelTests.add(new ParallelTest(testPage, testSystem));
		}

		for (ParallelTest parallelTest : parallelTests) {
			if (parallelTest.getTestSystem() != null) {
				if (parallelTest.getTestSystem().isSuccessfullyStarted()) {
					StringBuffer fullPageName = getFullPageName(parallelTest);
					processingMap.put(fullPageName.toString(), parallelTest.getWikiPage());
					parallelTest.start();
				} else {
					throw new Exception("Test system not started");
				}
			}
		}
	}

	private ParallelTest startRootPage(List<WikiPage> pagesInTestSystem, Descriptor descriptor, String buildClassPath) throws Exception {
		TestSystem testSystem;
		WikiPage rootPage = pagesInTestSystem.remove(0);

		currentTestSystem = testSystemGroup.startTestSystem(descriptor, buildClassPath);
		testSystem = currentTestSystem;
		resultsListener.testSystemStarted(testSystem, descriptor.testSystemName, descriptor.testRunner);
		ParallelTest rootParallelTest = new ParallelTest(rootPage, testSystem);
		StringBuffer rootPageName = getFullPageName(rootParallelTest);
		processingMap.put(rootPageName.toString(), rootParallelTest.getWikiPage());
		rootParallelTest.start();

		return rootParallelTest;
	}

	private StringBuffer getFullPageName(ParallelTest parallelTest) throws Exception {
		StringBuffer fullPageName = new StringBuffer();
		WikiPage wikiPage = parallelTest.getWikiPage();
		WikiPagePath fullPath = wikiPage.getPageCrawler().getFullPath(wikiPage);
		fullPageName.append(fullPath);

		return fullPageName;
	}

	private void waitForTestSystemToSendResults() throws InterruptedException {
//		while ((processingMap.size() > 0) && isNotStopped()) {
		while (processingMap.size() > 0) {
			Thread.sleep(50);
		}
	}

	PagesByTestSystem makeMapOfPagesByTestSystem() throws Exception {
		return addSuiteSetUpAndTearDownToAllTestSystems(mapWithAllPagesButSuiteSetUpAndTearDown());
	}

	private PagesByTestSystem mapWithAllPagesButSuiteSetUpAndTearDown() throws Exception {
		PagesByTestSystem pagesByTestSystem = new PagesByTestSystem();

		for (WikiPage testPage : testPagesToRun) {
			if (!SuiteContentsFinder.isSuiteSetupOrTearDown(testPage)) {
				addPageToListWithinMap(pagesByTestSystem, testPage);
			}
		}
		return pagesByTestSystem;
	}

	private void addPageToListWithinMap(PagesByTestSystem pagesByTestSystem, WikiPage testPage) throws Exception {
		Descriptor descriptor = TestSystem.getDescriptor(testPage.getData(), isRemoteDebug);
		getOrMakeListWithinMap(pagesByTestSystem, descriptor).add(testPage);
	}

	private LinkedList<WikiPage> getOrMakeListWithinMap(PagesByTestSystem pagesByTestSystem, Descriptor descriptor) {
		LinkedList<WikiPage> pagesForTestSystem;
		if (!pagesByTestSystem.containsKey(descriptor)) {
			pagesForTestSystem = new LinkedList<WikiPage>();
			pagesByTestSystem.put(descriptor, pagesForTestSystem);
		} else {
			pagesForTestSystem = pagesByTestSystem.get(descriptor);
		}
		return pagesForTestSystem;
	}

	private PagesByTestSystem addSuiteSetUpAndTearDownToAllTestSystems(PagesByTestSystem pagesByTestSystem) throws Exception {
		if (testPagesToRun.size() == 0)
			return pagesByTestSystem;
		for (LinkedList<WikiPage> pagesForTestSystem : pagesByTestSystem.values())
			surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(pagesForTestSystem);

		return pagesByTestSystem;
	}

	private void announceTotalTestsToRun(PagesByTestSystem pagesByTestSystem) {
		int tests = 0;
		for (LinkedList<WikiPage> listOfPagesToRun : pagesByTestSystem.values()) {
			tests += listOfPagesToRun.size();
		}
		resultsListener.announceNumberTestsToRun(tests);
	}

	public String buildClassPath() throws Exception {

		final ClassPathBuilder classPathBuilder = new ClassPathBuilder();
		final String pathSeparator = classPathBuilder.getPathSeparator(page);
		List<String> classPathElements = new ArrayList<String>();
		Set<WikiPage> visitedPages = new HashSet<WikiPage>();

		for (WikiPage testPage : testPagesToRun) {
			addClassPathElements(testPage, classPathElements, visitedPages);
		}

		return classPathBuilder.createClassPathString(classPathElements, pathSeparator);
	}

	private void addClassPathElements(WikiPage page, List<String> classPathElements, Set<WikiPage> visitedPages) throws Exception {
		List<String> pathElements = new ClassPathBuilder().getInheritedPathElements(page, visitedPages);
		classPathElements.addAll(pathElements);
	}

	public void acceptOutputFirst(String output) throws Exception {
		pageName = output.substring(5, output.indexOf("]"));
		WikiPage dataFromMap = processingMap.isEmpty() ? null : processingMap.get(pageName);
		boolean isNewTest = dataFromMap != null && dataFromMap != currentTest;

		if (isNewTest) {
			currentTest = dataFromMap;
			resultsListener.newTestStarted(currentTest, System.currentTimeMillis());
		}

		resultsListener.testOutputChunk(output);

	}

	public void testComplete(TestSummary testSummary) throws Exception {
		WikiPage testPage = processingMap.remove(pageName);
		resultsListener.testComplete(testPage, testSummary);
	}

	public synchronized void exceptionOccurred(Throwable e) {
		try {
			resultsListener.errorOccured();
			stop();
		} catch (Exception e1) {
			if (isNotStopped()) {
				e1.printStackTrace();
			}
		}
	}

	public ResultsListener getResultsListener() {
		return resultsListener;
	}

	private synchronized boolean isNotStopped() {
		return !isStopped;
	}

	public void stop() throws Exception {
		boolean wasNotStopped = isNotStopped();
		synchronized (this) {
			isStopped = true;
			if (stopId != null) {
				fitNesseContext.runningTestingTracker.removeEndedProcess(stopId);
			}
		}

		if (wasNotStopped) {
			testSystemGroup.kill();
		}
	}

	public class ParallelTest extends Thread {
		private WikiPage testPage;
		private TestSystem testSystem;

		public ParallelTest(WikiPage testPage, TestSystem testSystem) {
			this.testPage = testPage;
			this.testSystem = testSystem;
		}

		public WikiPage getWikiPage() {
			return testPage;
		}

		public TestSystem getTestSystem() {
			return testSystem;
		}

		public void run() {
			try {
				PageData pageData;
				pageData = testPage.getData();
				SetupTeardownIncluder.includeInto(pageData);
				testSystem.runTestsAndGenerateHtml(pageData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
