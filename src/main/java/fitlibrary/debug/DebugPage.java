/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 8/11/2006
*/

package fitlibrary.debug;

import java.io.IOException;

import fit.Counts;
import fit.FixtureListener;
import fit.Parse;
import fit.exception.FitParseException;
import fitlibrary.suite.BatchFitLibrary;
import fitlibrary.table.Tables;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class DebugPage {
	protected int tablesFinished = 0;
	protected int storytestsFinished = 0;
	protected int expectedTablesFinished = 0;
	
	protected FixtureListener fixtureListener = new FixtureListener() {
		public void tableFinished(Parse table) {
			tablesFinished++;
		}
		public void tablesFinished(Counts count) {
			storytestsFinished++;
		}
	};
	BatchFitLibrary batchFitLibrary = new BatchFitLibrary(new TableListener(fixtureListener));
	private GrabPage grabPage;

	public static void main(String[] args) throws Exception {
		String url = "http://localhost:8980/";
		String[] pageNames = new String[] {
				"BatchFitLibraryInFitNesse"
		};
		run(url, pageNames);
	}
	public static void run(String url, String[] pageNames) throws Exception {
		DebugPage runPage = new DebugPage(url);
		runPage.runs(pageNames);
	}
	public DebugPage(String url) {
		grabPage = new GrabPage(url);
	}
	public void runs(String[] pageNames) throws FitParseException, IOException {
		tablesFinished = 0;
		storytestsFinished = 0;
		for (int i = 0; i < pageNames.length; i++) {
			run(pageNames[i]);
			if (storytestsFinished != i+1)
				throw new RuntimeException("Wrong # of FixtureListener events fired for "+pageNames[i]+
						": "+storytestsFinished+" instead of "+(i+1));
		}
		if (tablesFinished != expectedTablesFinished)
			throw new RuntimeException("Expected FixtureListener events for "+expectedTablesFinished+
					" tables but instead got "+tablesFinished);
	}
	public void run(String pageName) throws IOException, FitParseException {
		String html = grabPage.grabPage(pageName);
		Parse parse = new Parse(html);
		System.out.println("\n----------\nHTML for "+pageName+"\n----------\n"+html);
		Tables tables = new Tables(parse);
		expectedTablesFinished += tables.size();
		TestResults testResults = batchFitLibrary.doTables(tables);
		System.out.println("\n----------\nHTML Report for "+pageName+"\n----------\n"+ParseUtility.toString(parse));
		System.out.println(testResults);
	}
}
