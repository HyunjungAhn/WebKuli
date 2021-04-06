/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.suite;

import fit.FitServerBridge;
import fit.Parse;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Traverse;
import fitlibrary.typed.TypedFactory;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class FitLibraryServer extends FitServerBridge implements Reportage {
	private BatchFitLibrary batching = new BatchFitLibrary(this);

    public FitLibraryServer(String host, int port, boolean verbose) {
        super(host,port,verbose);
        installGenericProcessing();
    }
    public FitLibraryServer() {
    	installGenericProcessing();
    }
	public void doTables(Parse parseTables) {
		fixture.counts = doTables(new Tables(parseTables)).getCounts();
    }
	public TestResults doTables(Tables theTables) {
		TableListener tableListener = new TableListener(fixtureListener);
		batching.doTables(theTables, tableListener);
		return tableListener.getTestResults();
	}
	public void exit() throws Exception {
		batching.exit();
		super.exit();
	}
	public static void installGenericProcessing() {
		try {
			Class typedFactory = Class.forName("fitlibrary2.typed.GenericTypedFactory");
			Traverse.installTypedFactory((TypedFactory) typedFactory.newInstance());
		} catch (Exception ex) {
			// FitLibrary2 is not on the classpath, so don't install generics handling after all.
		}
	}
    public static void main(String[] args) throws Exception {
        FitServerBridge fitServer = new FitLibraryServer();
        fitServer.run(args);
        System.exit(fitServer.exitCode());
    }
}