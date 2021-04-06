package com.nhncorp.ntaf;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import fit.Fixture;
import fit.RunningState;
import fitlibrary.table.Tables;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class NtafExecuteData {
	private static final String DURATION = "duration";
	private static final String EXPR = "expr";
	private static final String INDEXVAR = "indexvar";
	private static final String UNTIL = "until";
	private static final String WHILE = "while";
	private static final String BY = "by";
	private static final String TO = "to";
	private static final String FROM = "from";
	private static final String VAR = "var";
	private static final int UNKNOWN_KEYWORD = -1;
	private static final int SYMBOL_ITERATE = 0;
	private static final int SYMBOL_LOOP = 1;
	private static final int SYMBOL_IF = 2;
	private static final int SYMBOL_SEQUENCE = 3;
	private static final int SYMBOL_PARALLEL = 4;
	private static final int SYMBOL_P_ITERATE = 5;
	private static final int SYMBOL_TIMER = 6;

	private Fixture flowFixture;
	private Tables tables;
	private int fromRow;
	private int toRow;
	private TestResults testResults;
	private TableListener tableListener;
	private RunningState childState;
	private NtafFlowState state;

	private String strVar = "";
	private String strFrom = "";
	private String strTo = "";
	private String strBy = "";
	private String strWhile = "";
	private String strUntil = "";
	private int loopFrom;
	private int loopTo;
	private int incrementBy;

	private int keywordType;

	private String strIndexvar;
	private ArrayList<String> iterateList = null;

	private String strCondition;

	private CountDownLatch doneLatch;
	private ArrayList<NtafThread> threadArray = null;

	private String strDuration;

	public NtafExecuteData(FlowFixture parentFixture, Tables tables, int fromRow, int toRow,
		TableListener tableListener, NtafFlowState state, RunningState childState) {
		this.flowFixture = parentFixture;
		this.tables = tables;
		this.fromRow = fromRow;
		this.toRow = toRow;
		this.tableListener = tableListener;
		this.testResults = tableListener.getTestResults();
		this.childState = childState;
		this.state = state;
		keywordType = UNKNOWN_KEYWORD;

	}

	public void teardownAttributesAndSymbols() {
		switch (keywordType) {
			case SYMBOL_LOOP:
				NtafDoTraverse.ntafInfo.removeSymbol(VAR);
				NtafDoTraverse.ntafInfo.removeAttribute(VAR);
				NtafDoTraverse.ntafInfo.removeAttribute(FROM);
				NtafDoTraverse.ntafInfo.removeAttribute(TO);
				NtafDoTraverse.ntafInfo.removeAttribute(BY);
				NtafDoTraverse.ntafInfo.removeAttribute(WHILE);
				NtafDoTraverse.ntafInfo.removeAttribute(UNTIL);
				break;
			case SYMBOL_P_ITERATE:
			case SYMBOL_ITERATE:
				NtafDoTraverse.ntafInfo.removeSymbol(VAR);
				NtafDoTraverse.ntafInfo.removeSymbol(INDEXVAR);
				NtafDoTraverse.ntafInfo.removeAttribute(VAR);
				NtafDoTraverse.ntafInfo.removeAttribute(INDEXVAR);
				break;
			case SYMBOL_IF:
				NtafDoTraverse.ntafInfo.removeSymbol(EXPR);
				NtafDoTraverse.ntafInfo.removeAttribute(EXPR);
				break;
			case SYMBOL_TIMER:
				NtafDoTraverse.ntafInfo.removeAttribute(DURATION);
				break;
		}

	}

	public void initializeTimerAttributes() {
		strDuration = NtafDoTraverse.ntafInfo.getReplaceSymbolAttribute(DURATION);

		keywordType = SYMBOL_TIMER;
	}

	public void initializeParallelIterateAttributes() {
		strVar = NtafDoTraverse.ntafInfo.getAttribute(VAR).trim();
		strIndexvar = NtafDoTraverse.ntafInfo.getAttribute(INDEXVAR).trim();
		iterateList = new ArrayList<String>(NtafDoTraverse.ntafInfo.getInList());

		initializeParallelAttributes();
		keywordType = SYMBOL_P_ITERATE;
	}

	public void initializeIterateAttributes() {
		strVar = NtafDoTraverse.ntafInfo.getAttribute(VAR).trim();
		strIndexvar = NtafDoTraverse.ntafInfo.getAttribute(INDEXVAR).trim();
		iterateList = new ArrayList<String>(NtafDoTraverse.ntafInfo.getInList());

		keywordType = SYMBOL_ITERATE;

	}

	public void initializeLoopAttributes() {
		//get the value of attributes
		strVar = NtafDoTraverse.ntafInfo.getAttribute(VAR).trim();
		strFrom = NtafDoTraverse.ntafInfo.getReplaceSymbolAttribute(FROM);
		strTo = NtafDoTraverse.ntafInfo.getReplaceSymbolAttribute(TO);
		strBy = NtafDoTraverse.ntafInfo.getReplaceSymbolAttribute(BY);
		strWhile = NtafDoTraverse.ntafInfo.getReplaceSymbolAttribute(WHILE);
		strUntil = NtafDoTraverse.ntafInfo.getReplaceSymbolAttribute(UNTIL);

		keywordType = SYMBOL_LOOP;

		if (strFrom.equals("")) {
			loopFrom = 0;
		} else {
			loopFrom = Integer.parseInt(strFrom);
		}

		if (strTo.equals("")) {
			loopTo = loopFrom + 1;
		} else {
			loopTo = Integer.parseInt(strTo);
		}

		if (strBy.equals("")) {
			incrementBy = 1;
		} else {
			incrementBy = Integer.parseInt(strBy);
		}
	}

	public void initializeIfAttributes(NtafFlowState state, int fromRow, int toRow) {
		int index = 0;

		if (state.equals(NtafFlowState.ELSE)) {
			index = fromRow - 2;
		} else {
			index = toRow;
		}

		strCondition = NtafDoTraverse.ntafInfo.getAttribute(index + EXPR).trim();

		keywordType = SYMBOL_IF;
	}

	public void initializeSequenceAttributes() {
		keywordType = SYMBOL_SEQUENCE;
	}

	public void initializeParallelAttributes() {
		doneLatch = null;
		threadArray = new ArrayList<NtafThread>();
		keywordType = SYMBOL_PARALLEL;
	}

	public String getStrIndexvar() {
		return this.strIndexvar;
	}

	public ArrayList<String> getIterateList() {
		return this.iterateList;
	}

	public int getIterateSize() {
		return this.iterateList.size();
	}

	public Fixture getFlowFixture() {
		return this.flowFixture;
	}

	public Tables getTables() {
		return this.tables;
	}

	public int getFrom() {
		return this.fromRow;
	}

	public int getTo() {
		return this.toRow;
	}

	public TestResults getTestResults() {
		return this.testResults;
	}

	public TableListener getTableListener() {
		return this.tableListener;
	}

	public RunningState getChildState() {
		return this.childState;
	}

	public String getStrVar() {
		return this.strVar;
	}

	public String getStrFrom() {
		return this.strFrom;
	}

	public String getStrTo() {
		return this.strTo;
	}

	public String getStrBy() {
		return this.strBy;
	}

	public String getStrWhile() {
		return this.strWhile;
	}

	public String getStrUntil() {
		return this.strUntil;
	}

	public int getLoopFrom() {
		return this.loopFrom;
	}

	public int getLoopTo() {
		return this.loopTo;
	}

	public void setLoopTo(int loopTo) {
		this.loopTo = loopTo;
	}

	public int getIncrementBy() {
		return this.incrementBy;
	}

	public void adjustLoopToValue() {
		if (strTo.equals("")) {
			this.loopTo += 2;
		}
	}

	public String getCellParameterText(int index) {
		return tables.table(index).row(0).text(1);
	}

	public String getKeywordText(int index) {
		return tables.table(index).row(0).text(0);
	}

	public int getBreakCount() {
		int result = 0;

		switch (keywordType) {
			case SYMBOL_LOOP:
				result = loopTo;
				break;
			case SYMBOL_P_ITERATE:
			case SYMBOL_ITERATE:
				result = iterateList.size();
				break;
		}

		return result;
	}

	public NtafFlowState getState() {
		return this.state;
	}

	public String getStrCondition() {
		return this.strCondition;
	}

	public CountDownLatch getDoneLatch() {
		return this.doneLatch;
	}

	public ArrayList<NtafThread> getThreadArray() {
		return this.threadArray;
	}

	public void setDoneLatch(CountDownLatch doneLatch) {
		this.doneLatch = doneLatch;
	}

	public String getStrDuration() {
		return this.strDuration;
	}

	public void setStrDuration(String strDuration) {
		this.strDuration = strDuration;
	}
}