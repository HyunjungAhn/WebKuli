//Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
//Copyright (c) 2002 Cunningham & Cunningham, Inc.
//Released under the terms of the GNU General Public License version 2 or later.
//Copyright (C) 2003,2004 by Robert C. Martin and Micah D. Martin. All rights reserved.
//Released under the terms of the GNU General Public License version 2 or later.
//This is the same as fit.FitServer except that newFixture() has been made protected.
//Altered by Rick Mugridge, December 2005, to allow changes in a subclass.
package fit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import fit.exception.FitParseException;
import fitlibrary.table.ParseNode;
import fitlibrary.utility.ParseUtility;
import util.CommandLine;
import util.StreamReader;

/* This is a variation of FitServer that changes the doTables() call in 
 * the middle of the process() method.
 */
public abstract class FitServerBridge  {
    public String input;
    public Parse tables;
    public Fixture fixture = new Fixture();
    public FixtureListener fixtureListener = new TablePrintingFixtureListener();
    private Counts counts = new Counts();
    protected OutputStream socketOutput;
    private StreamReader socketReader;
    private boolean verbose = true;
    private String host;
    private int port;
    private int socketToken;
    private Socket socket;
    protected int numberOfPages = 0;
	protected boolean showAllReports = false;
    
    public FitServerBridge(String host, int port, boolean verbose) {
        this.host = host;
        this.port = port;
        this.verbose = verbose;
    }
    public FitServerBridge() {
    	//
    }
    public void run(String argv[]) throws Exception {
        args(argv);
        establishConnection();
        validateConnection();
        process();
        closeConnection();
        exit();
    }
    public void closeConnection() throws IOException {
        socket.close();
    }
    public void process() {
        fixture.listener = fixtureListener;
        try {
            int size = 1;
            while ((size = FitProtocol.readSize(socketReader)) != 0) {
                try {
                    print("processing document of size: " + size + "\n");
                    String document = FitProtocol.readDocument(socketReader,
                            size);
                    // TODO MDM if the page name was always the first line of
                    // the body, it could be printed here.
                    tables = new Parse(document);
                    doTables(tables);
                    print("\tresults: " + fixture.counts() + "\n");
                    counts.tally(fixture.counts);
                    numberOfPages++;
                } catch (FitParseException e) {
                    exception(e);
                }
            }
            print("completion signal recieved" + "\n");
        } catch (Exception e) {
            exception(e);
        }
    }
    public abstract void doTables(Parse theTables);
    
	public String readDocument() throws Exception {
        int size = FitProtocol.readSize(socketReader);
        return FitProtocol.readDocument(socketReader, size);
    }
    public void args(String[] argv) {
        CommandLine commandLine = new CommandLine("[-v] host port socketToken");
        if (commandLine.parse(argv)) {
            host = commandLine.getArgument("host");
            port = Integer.parseInt(commandLine.getArgument("port"));
            socketToken = Integer.parseInt(commandLine.getArgument("socketToken"));
            verbose = commandLine.hasOption("v");
        } else
            usage();
    }
    private void usage() {
        System.out
                .println("usage: java fit.FitServer [-v] host port socketTicket");
        System.out.println("\t-v\tverbose");
        System.exit(-1);
    }
    protected void exception(Exception e) {
        print("Exception occurred!" + "\n");
        print("\t" + e.getMessage() + "\n");
        tables = new Parse("span", "Exception occurred: ", null, null);
        fixture.exception(tables, e);
        counts.exceptions += 1;
        fixture.listener.tableFinished(tables);
        fixture.listener.tablesFinished(counts); // TODO shouldn't this be fixture.counts
    }
    public void exit() throws Exception {
        print("exiting" + "\n");
        print("\tend results: " + counts.toString() + "\n");
    }
    public int exitCode() {
        return counts.wrong + counts.exceptions;
    }
    public void establishConnection() throws Exception {
        establishConnection(makeHttpRequest());
    }
    public void establishConnection(String httpRequest) throws Exception {
        socket = new Socket(host, port);
        socketOutput = socket.getOutputStream();
        socketReader = new StreamReader(socket.getInputStream());
        byte[] bytes = httpRequest.getBytes("UTF-8");
        socketOutput.write(bytes);
        socketOutput.flush();
        print("http request sent" + "\n");
    }
    private String makeHttpRequest() {
        return "GET /?responder=socketCatcher&ticket=" + socketToken
                + " HTTP/1.1\r\n\r\n";
    }
    public void validateConnection() throws Exception {
        print("validating connection...");
        int statusSize = FitProtocol.readSize(socketReader);
        if (statusSize == 0)
            print("...ok" + "\n");
        else {
            String errorMessage = FitProtocol.readDocument(socketReader,
                    statusSize);
            print("...failed bacuase: " + errorMessage + "\n");
            System.out.println("An error occured while connecting to client.");
            System.out.println(errorMessage);
            System.exit(-1);
        }
    }
    public Counts getCounts() {
        return counts;
    }
    private void print(String message) {
        if (verbose)
            System.out.print(message);
    }
    public static byte[] readTable(Parse table) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = new OutputStreamWriter(byteBuffer,
                "UTF-8");
        PrintWriter writer = new PrintWriter(streamWriter);
        Parse more = table.more;
        table.more = null;
        if (table.trailer == null)
            table.trailer = "";
        table.print(writer);
        table.more = more;
        writer.close();
        return byteBuffer.toByteArray();
    }
    public void writeCounts(Counts count) throws IOException {
        // TODO This can't be right.... which counts should be used?
        FitProtocol.writeCounts(counts, socketOutput);
    }

    public void showAllReports() {
		showAllReports  = true;
	}

	class TablePrintingFixtureListener implements FixtureListener {
        public void tableFinished(Parse table) {
            try {
                byte[] bytes;
            	if (!showAllReports && numberOfPages > 1 && passed(table))
            		bytes = ".".getBytes();
            	else
            		bytes = readTable(table);
                if (bytes.length > 0)
                    FitProtocol.writeData(bytes, socketOutput);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
		public void tablesFinished(Counts count) {
            try {
                FitProtocol.writeCounts(count, socketOutput);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		private boolean passed(Parse table) {
			String s = ParseUtility.toString(table);
			return s.indexOf(ParseNode.FAIL) < 0 && s.indexOf(ParseNode.ERROR) < 0;
		}
    }
}
