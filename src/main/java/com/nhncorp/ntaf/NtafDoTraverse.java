/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package com.nhncorp.ntaf;

import fit.FitServer;
import fit.Fixture;
import fit.FixtureLoader;
import fit.FlowState;
import fit.Parse;
import fit.RunningState;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nhncorp.ntaf.annotation.CellIgnoreFirst;
import com.nhncorp.ntaf.annotation.CellTestResult;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.closure.Closure;
import fitlibrary.closure.LookupMethodTarget;
import fitlibrary.closure.MethodTarget;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.NotRejectedException;
import fitlibrary.exception.method.AmbiguousActionException;
import fitlibrary.exception.method.MissingMethodException;
import fitlibrary.exception.parse.ParseException;
import fitlibrary.exception.table.ExtraCellsException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.parser.Parser;
import fitlibrary.parser.graphic.GraphicParser;
import fitlibrary.parser.graphic.ObjectDotGraphic;
import fitlibrary.suite.FlowControl;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.CommentTraverse;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.function.CalculateTraverse;
import fitlibrary.traverse.function.ConstraintTraverse;
import fitlibrary.typed.NonGenericTyped;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class NtafDoTraverse extends Traverse implements NtafDoEvaluator {
	private static final String COMMAND_EXCEPTION = "command_exception";
	private static final String COMMAND_WRONG = "command_wrong";
	private static final String COMMAND_RIGHT = "command_right";
	private static final String COMMAND_CONTINUE = "command_continue";
	private static final String COMMAND_BREAK = "command_break";
	private static final String COMMAND_VAR = "command_var";
	private static final String COMMAND_LOG = "command_log";
	private static final String COMMAND_STRING = "command_string";
	private static final String ASSERTEQUALS = "assertEquals";
	private static final Object ASSERTNOTEQUALS = "assertNotEquals";
	private static final Object ASSERTTRUE = "assertTrue";
	private static final Object ASSERTFALSE = "assertFalse";
	private static final Object ASSERTNULL = "assertNull";
	private static final Object ASSERTNOTNULL = "assertNotNull";
	private static final Object ASSERTEXIST = "assertExist";
	private static final Object ASSERTNOTEXIST = "assertNotExist";
	private boolean expected;
	private Object expectedResult = Boolean.TRUE; // Used for UI code generation
	private NtafCollectionSetUpTraverse setUpTraverse = null; // delegate for
																// setup phase
	private boolean settingUp = true;
	private FlowControl flowControl;
	public static Row GLOBAL_ROW = null;
	protected boolean initIfCondition;

	public void setInitIfCondition(boolean initIfCondition) {
		this.initIfCondition = initIfCondition;
	}

	protected int cnt;
	protected int tableIndex;
	protected boolean bContinue;

	protected Fixture flowFixture;
	protected NtafExecuteData data;
	protected NtafDoTraverse parent;

	static NtafInfo ntafInfo = new NtafInfo();

	protected NtafDoTraverse() {
		initIfCondition = true;
	}

	public NtafDoTraverse(Object sut) {
		super(sut);
		initIfCondition = true;

		if (sut instanceof Fixture) {
			flowFixture = (Fixture)sut;
		}
	}

	public NtafDoTraverse(TypedObject typedObject) {
		super(typedObject);
		initIfCondition = true;
	}

	protected boolean isTimedout() {
		boolean result;

		if (null == data) {
			result = false;
		} else if (null == data.getChildState()) {
			result = false;
		} else {
			result = (FlowState.TIMEOUT == data.getChildState().getRunState());
		}

		return result;
	}

	protected boolean isRunnableKeyword(String keywordname) {
		return ntafInfo.isStartKeyword(keywordname) && null != flowFixture;
	}

	protected boolean runTablesWithBreakCondition() {
		boolean breakMet;

		RunningState childState = new RunningState();
		tableIndex = ((NtafDoFixture)flowFixture).runNestedTables(data.getTables(), tableIndex, data.getTableListener(), childState);

		if (childState.getRunState() == FlowState.BREAK) {
			cnt = data.getBreakCount();
			breakMet = true;
		} else if (childState.getRunState() == FlowState.CONTINUE) {
			bContinue = true;
			breakMet = true;
		} else {
			breakMet = false;
		}

		return breakMet;
	}

	protected Object runInnerTables() {
		Object result = flowFixture;
		String orgKeywordname = "";
		boolean isSymbolKeyword = false;

		for (tableIndex = data.getFrom(); tableIndex < data.getTo(); tableIndex++) {
			String keywordname = data.getKeywordText(tableIndex);

			if (true == FitServer.hasSymbol(keywordname)) {
				isSymbolKeyword = true;
				orgKeywordname = keywordname;
				keywordname = FitServer.getSymbol(keywordname).toString();
			}

			if (NtafInfo.isKeyword(keywordname)) {
				if (ntafInfo.isStartKeyword(keywordname) && null != flowFixture && flowFixture instanceof NtafDoFixture) {
					NtafDoFixture doFixture = (NtafDoFixture)flowFixture;

					tableIndex = doFixture.runNestedTables(data.getTables(), tableIndex, data.getTableListener(), data.getChildState());
				} else if (true != handleCommand(data.getTables().table(tableIndex).row(0), keywordname, ntafInfo, data.getChildState(), data.getTestResults())) {
					break;
				}
			} else {
				if (true == isSymbolKeyword) {
					changeTableBody(data.getTables().table(tableIndex).parse.parts.parts, keywordname);
				}

				result = executeFixture(data.getTables().table(tableIndex), data.getTestResults());

				if (true == isSymbolKeyword) {
					changeTableBody(data.getTables().table(tableIndex).parse.parts.parts, orgKeywordname);
				}

				isSymbolKeyword = false;
			}
		}

		return result;
	}

	private void changeTableBody(Parse parse, String keywordname) {
		parse.body = keywordname;
		parse.orgbody = keywordname;
	}

	protected Object runInnerLoopTables() {
		Object result = null;
		String orgKeywordname = "";
		boolean isSymbolKeyword = false;

		for (tableIndex = data.getFrom(); tableIndex < data.getTo(); tableIndex++) {
			String keywordname = data.getKeywordText(tableIndex);

			if (true == FitServer.hasSymbol(keywordname)) {
				isSymbolKeyword = true;
				orgKeywordname = keywordname;
				keywordname = FitServer.getSymbol(keywordname).toString();
			}

			if (NtafInfo.isKeyword(keywordname)) {
				if (isRunnableKeyword(keywordname)) {
					if (runTablesWithBreakCondition()) {
						break;
					}
				} else if (runSpecialKeyword(keywordname)) {
					break;
				}
			} else {
				if (true == isSymbolKeyword) {
					changeTableBody(data.getTables().table(tableIndex).parse.parts.parts, keywordname);
				}

				result = executeFixture(data.getTables().table(tableIndex), data.getTestResults());

				if (true == isSymbolKeyword) {
					changeTableBody(data.getTables().table(tableIndex).parse.parts.parts, orgKeywordname);
				}

				isSymbolKeyword = false;
			}

			if (bContinue) {
				bContinue = false;
				continue;
			}
		}

		return result;
	}

	protected void ignore(int index) {
		if (false == data.getTables().table(index).row(0).cell(0).isIgnored()) {
			data.getTables().table(index).row(0).cell(0).ignore(data.getTestResults());
		}
	}

	public boolean handleCommand(Row rowZero, String keywordname, NtafInfo ntafInfo, RunningState childState, TestResults testResults) {

		if (false == rowZero.cell(0).isIgnored()) {
			rowZero.cell(0).ignore(testResults);
		}

		if (keywordname.equals(COMMAND_LOG)) {
			ntafInfo.printLog(rowZero.text(1));
			return true;
		} else if (keywordname.equals(COMMAND_RIGHT)) {
			testResults.pass();
			return true;
		} else if (keywordname.equals(COMMAND_WRONG)) {
			testResults.fail();
			return true;
		} else if (keywordname.equals(COMMAND_EXCEPTION)) {
			testResults.exception();
			return true;
		} else if (keywordname.equals(COMMAND_VAR)) {
			rowZero.addCell().setText(FlowFixture.replaceSymbol(rowZero.text(1)));
			return true;
		} else if (keywordname.equals(COMMAND_BREAK) && null != childState) {
			childState.setRunState(FlowState.BREAK);
			return false;
		} else if (keywordname.equals(COMMAND_CONTINUE) && null != childState) {
			childState.setRunState(FlowState.CONTINUE);
			return false;
		} else if (true == keywordname.equals(COMMAND_STRING) && true == rowZero.cellExists(3)) {
			runStringKeyword(rowZero);
			return true;
		} else if (true == (keywordname.equals(ASSERTEQUALS) | keywordname.equals(ASSERTNOTEQUALS) | keywordname.equals(ASSERTTRUE) | keywordname.equals(ASSERTFALSE) | keywordname.equals(ASSERTNULL) | keywordname.equals(ASSERTNOTNULL) | keywordname.equals(ASSERTEXIST) | keywordname.equals(ASSERTNOTEXIST))) {
			NtafInFlowPageRunner.checkAssertStatement(testResults, rowZero, keywordname);
		}

		return true;
	}

	protected boolean runSpecialKeyword(String keywordname) {
		boolean breakMet = false;
		TestResults testResults = data.getTestResults();
		ignore(tableIndex);
		Row rowZero = data.getTables().table(tableIndex).row(0);

		if (COMMAND_BREAK.equals(keywordname)) {
			breakMet = runBreakKeyword(rowZero);
		} else if (COMMAND_CONTINUE.equals(keywordname)) {
			breakMet = runContinueKeyword(rowZero);
		} else if (COMMAND_LOG.equals(keywordname)) {
			ntafInfo.printLog(data.getCellParameterText(tableIndex));
		} else if (COMMAND_VAR.equals(keywordname)) {
			runVarKeyword(rowZero);
		} else if (COMMAND_RIGHT.equals(keywordname)) {
			testResults.pass();
		} else if (COMMAND_WRONG.equals(keywordname)) {
			testResults.fail();
		} else if (COMMAND_EXCEPTION.equals(keywordname)) {
			testResults.exception();
		} else if (true == keywordname.equals(COMMAND_STRING) && true == rowZero.cellExists(3)) {
			runStringKeyword(rowZero);
		} else if (true == (keywordname.equals(ASSERTEQUALS) | keywordname.equals(ASSERTNOTEQUALS) | keywordname.equals(ASSERTTRUE) | keywordname.equals(ASSERTFALSE) | keywordname.equals(ASSERTNULL) | keywordname.equals(ASSERTNOTNULL) | keywordname.equals(ASSERTEXIST) | keywordname.equals(ASSERTNOTEXIST))) {
			NtafInFlowPageRunner.checkAssertStatement(data.getTestResults(), rowZero, keywordname);
		}

		return breakMet;
	}

	private void runStringKeyword(Row rowZero) {
		NtafInFlowPageRunner.executeCommandString(rowZero, data.getTableListener());
	}

	private void runVarKeyword(Row rowZero) {
		if (true == rowZero.cellExists(2)) {
			rowZero.cell(2).setText("Value : ");

			if (false == rowZero.cell(2).isIgnored()) {
				rowZero.cell(2).ignore(data.getTableListener().getTestResults());
			}

			rowZero.addCell().setText(FlowFixture.replaceSymbol(data.getCellParameterText(tableIndex)));
		} else {
			FlowFixture.replaceSymbol(data.getCellParameterText(tableIndex));
		}
	}

	private boolean runContinueKeyword(Row rowZero) {
		boolean breakMet;
		String expr;

		if (true == rowZero.cellExists(1)) {
			expr = rowZero.cell(1).parse.body;
			bContinue = compareCondition(expr);

			if (true == bContinue) {
				breakMet = true;
			} else {
				breakMet = false;
			}
		} else {
			bContinue = true;
			breakMet = true;
		}

		return breakMet;
	}

	private boolean runBreakKeyword(Row rowZero) {
		boolean breakMet;
		String expr;

		if (true == rowZero.cellExists(1)) {
			expr = rowZero.cell(1).parse.body;
			breakMet = compareCondition(expr);
		} else {
			breakMet = true;
		}

		if (true == breakMet) {
			cnt = data.getBreakCount();
		}

		return breakMet;
	}

	public boolean compareCondition(String expr) {
		String LT = "&lt;";
		String GT = "&gt;";
		String LTEQ = "&lt;=";
		String GTEQ = "&gt;=";
		String EQ = "==";
		String NOTEQ = "!=";
		final String compareOp = "(.*)(&lt;\\=|&gt;\\=|&lt;|&gt;|==|!=)(.*)";
		Pattern pattern = Pattern.compile(compareOp);
		Matcher matcher = pattern.matcher(expr);

		if (true == matcher.find()) {
			String leftValue = FlowFixture.replaceSymbol(matcher.group(1)).trim();
			String operator = matcher.group(2);
			String rightValue = FlowFixture.replaceSymbol(matcher.group(3)).trim();

			if (true == LT.equals(operator)) {
				return Double.parseDouble(leftValue) < Double.parseDouble(rightValue) ? true : false;
			} else if (true == LTEQ.equals(operator)) {
				return Double.parseDouble(leftValue) <= Double.parseDouble(rightValue) ? true : false;
			} else if (true == GT.equals(operator)) {
				return Double.parseDouble(leftValue) > Double.parseDouble(rightValue) ? true : false;
			} else if (true == GTEQ.equals(operator)) {
				return Double.parseDouble(leftValue) >= Double.parseDouble(rightValue) ? true : false;
			} else if (true == EQ.equals(operator)) {
				return leftValue.equals(rightValue) ? true : false;
			} else if (true == NOTEQ.equals(operator)) {
				return leftValue.equals(rightValue) ? false : true;
			}
		}

		return false;
	}

	public Object interpret(Table table, TestResults testResults) {
		Object result = null;
		setUp(table, testResults);
		int size = table.size();

		for (int rowNo = 1; rowNo < size; rowNo++) {
			Row row = table.row(rowNo);
			try {
				result = interpretRow(row, testResults);

				if (result instanceof NtafDoEvaluator) {
					NtafDoEvaluator evaluator = (NtafDoEvaluator)result;
					evaluator.interpretInFlow(new Table(row), testResults);
					break;
				} else if (result instanceof Evaluator) {
					Evaluator evaluator = (Evaluator)result;
					evaluator.interpret(new Table(row), testResults);
					break;
				} else if (getAlienTraverseHandler().isAlienTraverse(result)) {
					getAlienTraverseHandler().doTable(result, new Table(row), testResults);
					break;
				}
			} catch (Exception ex) {
				row.error(testResults, ex);
			}
		}

		tearDown(table, testResults);

		return result;
	}

	public Object interpretWholeTable(Table table, TableListener tableListener) {
		TestResults testResults = tableListener.getTestResults();

		try {
			Object result = interpretRow(table.row(0), testResults);

			if (result instanceof Fixture) {
				((Fixture)result).getArgsForTable(table.parse);
			}

			if (result instanceof Evaluator) {
				Evaluator evaluator = (Evaluator)result;
				evaluator.interpret(table, testResults);
				return result;
			} else if (getAlienTraverseHandler().isAlienTraverse(result)) {
				getAlienTraverseHandler().doTable(result, table, testResults);
				return result;
			} else {
				// do the rest of the table with this traverse
				return interpretInFlow(table, testResults);
			}
		} catch (Throwable e) {
			table.error(testResults, e);
		}
		return null;
	}

	public Object interpretInnerTables(Tables tables, int fromRow, int toRow, TableListener tableListener, RunningState state) {
		Object result = interpretFlowState(tables, fromRow, toRow, tableListener, state);
		return result;
	}

	// @Overridden
	public Object interpretInFlow(Table table, TestResults testResults) {
		return interpret(table, testResults);
	}

	public Object interpretRow(Row row, TestResults testResults) {
		Object result;
		final Cell cell = row.cell(0);

		if (cell.hasEmbeddedTable()) {
			setExpectedResult(null);

			// Used? by jongchae77
			// interpretInnerTables(cell.innerTables(), testResults);
			return null;
		}

		setExpectedResult(Boolean.TRUE);
		String methodName = row.text(0);

		// For special keyword of NtafFlowFixture
		if (isHelp(row)) {
			try {
				displayHelp(row, methodName);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		// Check for Fixture
		Fixture fixture = findFixture(methodName);

		if (null != fixture) {
			return fixture;
		}

		try {
			result = runMethod(row, testResults, methodName);
		} catch (IgnoredException ex) {
			ex.printStackTrace();
			result = null;
		} catch (Exception ex) {
			cell.error(testResults, ex);
			result = null;
		}

		return result;
	}

	private Object runMethod(Row row, TestResults testResults, String methodName) throws Exception {
		Object result = null;

		String method = methodName;

		if (!method.equals("")) {
			method = ExtendedCamelCase.camel(method);
		}

		NtafDoTraverse switchedSetUp = switchSetUp();
		CalledMethodTarget specialMethod = switchedSetUp.findSpecialMethod(row.text(0));
		checkForAmbiguity(method, specialMethod, null);

		try {
			CalledMethodTarget target = switchedSetUp.findMethodByActionName(row, row.size() - 1);

			Closure closure = target.getClosure();
			Method reflectedMethod = (Method)closure.getObject();

			annotationCellTestResult(row, testResults, reflectedMethod);
			annotationCellIgnoreFirst(row, testResults, reflectedMethod);

			checkForAmbiguity(method, specialMethod, target);

			NtafDoTraverse.GLOBAL_ROW = row;
			result = target.invokeAndWrap(row.rowFrom(1), testResults);

			if (result instanceof Boolean) {
				target.color(row, ((Boolean)result).booleanValue(), testResults);
			}

			return result;
		} catch (MissingMethodException e) {
			if (specialMethod == null) {
				throw e;
			}

			result = specialMethod.invoke(new Object[] {row, testResults});
		}

		return result;
	}

	private void annotationCellIgnoreFirst(Row row, TestResults testResults, Method reflectedMethod) {
		CellIgnoreFirst annotation = reflectedMethod.getAnnotation(CellIgnoreFirst.class);

		if (annotation != null) {
			row.cell(0).ignore(testResults);
		}
	}

	private void annotationCellTestResult(Row row, TestResults testResults, Method reflectedMethod) {
		CellTestResult annotation = reflectedMethod.getAnnotation(CellTestResult.class);

		if (annotation != null) {
			for (int i = 0; i < annotation.position().length; i++) {
				int position = annotation.position()[i];
				String testResult = annotation.testResult()[i];

				if (testResult.equals("right")) {
					row.cell(position).pass(testResults);
				} else if (testResult.equals("wrong")) {
					row.cell(position).fail(testResults);
				} else if (testResult.equals("exception")) {
					row.cell(position).error(testResults, new Exception());
				} else {
					row.cell(position).ignore(testResults);
				}
			}
		}
	}

	protected boolean isHelp(Row row) {
		return row.cellExists(1) && row.cell(1).text().equals("help");
	}

	protected Fixture findFixture(String methodName) {
		try {
			Fixture fixture = FixtureLoader.instance().disgraceThenLoad(methodName);

			if (null != fixture) {
				return fixture;
			}
		} catch (Throwable e1) {
		}

		return null;
	}

	private void displayHelp(Row row, String methodName) throws Exception {
		String resultStr = NtafFixture.parsingXML(methodName, "");

		if (resultStr.equals("")) {
			resultStr = "Can't find help comment in XML";
		}

		if (methodName.equals("FlowFixture")) {
			row.cell(1).parse.addToBody("<hr>" + "Fixture List" + Fixture.label(NtafFixture.parsingXML("", "")) + "<br><br> Keyword List" + Fixture.label(resultStr));
		} else {
			row.cell(1).parse.addToBody("<hr>" + "Keyword List" + Fixture.label(resultStr));
		}
	}

	// Used? by jongchae77
	// private void interpretInnerTables(Tables tables, TestResults testResults)
	// {
	// new NtafInFlowPageRunner(this, false).run(tables, 0, new
	// TableListener(testResults));
	// }

	protected Object interpretFlowState(Tables tables, int fromRow, int toRow, TableListener tableListener, RunningState childState) {

		TestResults testResults = tableListener.getTestResults();
		Object result = null;

		FlowFixture parentFixture = null;

		if (null != flowFixture && flowFixture instanceof FlowFixture) {
			parentFixture = (FlowFixture)flowFixture;
		}

		if (0 == ntafInfo.getStateListSize()) {
			result = interpretRow(tables.table(fromRow).row(0), testResults);

			if (null != result && result instanceof Fixture) {
				getAlienTraverseHandler().doTable(result, tables.table(0), testResults);
			}
		} else {
			result = runNtafKeywords(tables, fromRow, toRow, tableListener, childState, testResults, parentFixture);
		}

		return result;
	}

	protected NtafDoTraverse createLoop(NtafFlowState state, NtafExecuteData data) {
		NtafDoTraverse travInstance = null;

		if (state.equals(NtafFlowState.ITERATE)) {
			travInstance = new NtafIterate(data);
		} else if (state.equals(NtafFlowState.LOOP)) {
			travInstance = new NtafLoop(data);
		}

		return travInstance;
	}

	protected NtafDoTraverse createOthers(NtafFlowState state, NtafExecuteData data, int fromRow, int toRow) {
		NtafDoTraverse travInstance = null;

		if (state.equals(NtafFlowState.IF) || state.equals(NtafFlowState.ELSEIF) || state.equals(NtafFlowState.ELSE)) {
			travInstance = new NtafIf(data, state, fromRow, toRow);
		} else if (state.equals(NtafFlowState.SEQUENCE)) {
			travInstance = new NtafSequence(data);
		} else if (state.equals(NtafFlowState.PARALLEL)) {
			travInstance = new NtafParallel(data);
		} else if (state.equals(NtafFlowState.PARALLELITERATE)) {
			travInstance = new NtafParallelIterate(data);
		} else if (state.equals(NtafFlowState.TIMER)) {
			travInstance = new NtafTimer(data);
		}

		return travInstance;
	}

	protected Object runNtafKeywords(Tables tables, int fromRow, int toRow, TableListener tableListener, RunningState childState, TestResults testResults, FlowFixture parentFixture) {
		NtafDoTraverse travInstance = null;
		boolean runKeyword = true;
		Object result = null;

		NtafFlowState state = ntafInfo.getStateList().getLast();
		NtafExecuteData data = new NtafExecuteData(parentFixture, tables, fromRow, toRow, tableListener, state, childState);

		if (state.equals(NtafFlowState.IF) && initIfCondition) {
			ntafInfo.removeIfConditionAll();
		}

		travInstance = createKeywordTraverse(state, data, fromRow, toRow);

		if (null == travInstance) {
			runKeyword = false;
			result = interpretRow(tables.table(fromRow).row(0), testResults);

			if (null != result) {
				getAlienTraverseHandler().doTable(result, tables.table(fromRow), testResults);
			}
		}

		if (runKeyword) {
			travInstance.setSetUpTraverse(this);
			travInstance.setSettingUp(true);
			travInstance.setParent(this);
			result = travInstance.execute();
		}

		return result;
	}

	private void setParent(NtafDoTraverse parent) {
		this.parent = parent;

	}

	protected NtafDoTraverse createKeywordTraverse(NtafFlowState state, NtafExecuteData data, int fromRow, int toRow) {
		NtafDoTraverse travInstance;
		travInstance = createLoop(state, data);

		if (null == travInstance) {
			travInstance = createOthers(state, data, fromRow, toRow);
		}

		return travInstance;
	}

	protected Object execute() {
		return null;
	}

	/**
	 * Check that the result of the action in the rest of the row matches the
	 * expected value in the last cell of the row.
	 * 
	 * @param testResults
	 * @param evaluator
	 */
	public void check(final Row row, TestResults testResults) throws Exception {
		int less = 3;

		if (row.size() < less) {
			throw new MissingCellsException("DoTraverseCheck");
		}

		CalledMethodTarget target = findMethodFromRow(row, less);
		Cell expectedCell = row.last();

		if (expected) {
			expectedResult = target.getResult(expectedCell, testResults);
		}

		target.invokeAndCheck(row.rowFrom(2), expectedCell, testResults, false);
	}

	public void reject(Row row, TestResults testResults) throws Exception {
		not(row, testResults);
	}

	/**
	 * Same as reject()
	 * 
	 * @param testResults
	 */
	public void not(Row row, TestResults testResults) throws Exception {
		Cell notCell = row.cell(0);
		expectedResult = Boolean.FALSE;

		try {
			Object result = callMethodInRow(row, testResults, false);

			if (!(result instanceof Boolean)) {
				notCell.error(testResults, new NotRejectedException());
			} else if (((Boolean)result).booleanValue()) {
				notCell.fail(testResults);
			} else {
				notCell.pass(testResults);
			}
		} catch (IgnoredException e) {
			e.printStackTrace();
		} catch (FitLibraryException e) {
			if (e instanceof ParseException) {
				notCell.pass(testResults);
			} else {
				row.error(testResults, e);
			}
		} catch (Exception e) {
			notCell.pass(testResults);
		}
	}

	/**
	 * Add a cell containing the result of the rest of the row. HTML is not
	 * altered, so it can be viewed directly.
	 */
	public void show(Row row, TestResults testResults) throws Exception {
		try {
			CalledMethodTarget target = findMethodFromRow(row, 2);
			Object result = target.invoke(row.rowFrom(2), testResults, true);
			row.addCell(target.getResultString(result));
		} catch (IgnoredException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add a cell containing the result of the rest of the row, shown as a Dot
	 * graphic.
	 * 
	 * @param testResults
	 */
	public void showDot(Row row, TestResults testResults) throws Exception {
		Parser adapter = new GraphicParser(new NonGenericTyped(ObjectDotGraphic.class));
		try {
			Object result = callMethodInRow(row, testResults, true);
			row.addCell(adapter.show(new ObjectDotGraphic(result)));
		} catch (IgnoredException e) { // No result, so ignore
			e.printStackTrace();
		}
	}

	/**
	 * Checks that the action in the rest of the row succeeds. o If a boolean is
	 * returned, it must be true. o For other result types, no exception should
	 * be thrown.
	 * 
	 * @param testResults
	 */
	public void ensure(Row row, TestResults testResults) throws Exception {
		expectedResult = Boolean.TRUE;
		try {
			Object result = callMethodInRow(row, testResults, true);
			row.cell(0).passOrFail(testResults, ((Boolean)result).booleanValue());
		} catch (IgnoredException e) {
			e.printStackTrace();
		} catch (Exception e) {
			row.cell(0).fail(testResults);
		}
	}

	/**
	 * The rest of the row is ignored.
	 */
	public void note(Row row, TestResults testResults) throws Exception {
		// Nothing to do
	}

	/**
	 * The rest of the table is ignored (and not coloured)
	 */
	public CommentTraverse comment(Row row, TestResults testResults) throws Exception {
		return new CommentTraverse();
	}

	/**
	 * The rest of the table is ignored (and is coloured as ignored)
	 */
	public CommentTraverse ignored(Row row, TestResults testResults) throws Exception {
		return new CommentTraverse(true);
	}

	/**
	 * To allow for DoTraverse to be used without writing any fixturing code.
	 */
	public void start(Row row, TestResults testResults) throws Exception {
		String className = row.text(1);

		if (row.size() != 2) {
			throw new ExtraCellsException("DoTraverseStart");
		}

		setSystemUnderTest(ClassUtility.newInstance(className));
	}

	/**
	 * To allow for a CalculateTraverse to be used for the rest of the table.
	 */
	public CalculateTraverse calculate(Row row, TestResults testResults) throws Exception {
		if (row.size() != 1) {
			throw new ExtraCellsException("DoTraverseCalculate");
		}

		CalculateTraverse traverse;

		if (this.getClass() == NtafDoTraverse.class) {
			traverse = new CalculateTraverse(getTypedSystemUnderTest());
		} else {
			traverse = new CalculateTraverse(this);
		}

		traverse.theSetUpTearDownAlreadyHandled();

		return traverse;
	}

	/**
	 * To allow for a ConstraintTraverse to be used for the rest of the table.
	 */
	public ConstraintTraverse constraint(Row row, TestResults testResults) throws Exception {
		if (row.size() != 1) {
			throw new ExtraCellsException("DoTraverseConstraint");
		}

		ConstraintTraverse traverse = new ConstraintTraverse(this);
		traverse.theSetUpTearDownAlreadyHandled();

		return traverse;
	}

	/**
	 * To allow for a failing ConstraintTraverse to be used for the rest of the
	 * table.
	 */
	public ConstraintTraverse failingConstraint(Row row, TestResults testResults) throws Exception {
		if (row.size() != 1) {
			throw new ExtraCellsException("DoTraverseConstraint");
		}

		ConstraintTraverse traverse = new ConstraintTraverse(this, false);
		traverse.theSetUpTearDownAlreadyHandled();

		return traverse;
	}

	/**
	 * The rest of the storytest is ignored (but is not coloured as ignored)
	 */
	public void abandonStorytest(Row row, TestResults testResults) {
		flowControl.abandon();
	}

	/**
	 * if (stopOnError) then we don't continue intepreting a table if there's
	 * been a problem
	 */
	public void setStopOnError(boolean stopOnError) {
		flowControl.setStopOnError(stopOnError);
	}

	public void expectedTestResults(Row row, TestResults testResults) throws Exception {
		if (testResults.matches(row.text(1), row.text(3), row.text(5), row.text(7))) {
			testResults.clear();
			row.cell(0).pass(testResults);
		} else {
			String results = testResults.toString();
			testResults.clear();
			row.cell(0).fail(testResults, results);
		}
	}

	public CalledMethodTarget findMethodFromRow(final Row row, int less) throws Exception {
		return findMethodByActionName(row.rowFrom(1), row.size() - less);
	}

	/**
	 * Is overridden in subclass SequenceTraverse to process arguments
	 * differently
	 */
	public CalledMethodTarget findMethodByActionName(Row row, int allArgs) throws Exception {
		return LookupMethodTarget.findTheMethodMapped(row.text(0), allArgs, this);
	}

	private Object callMethodInRow(Row row, TestResults testResults, boolean catchError) throws Exception {
		return findMethodFromRow(row, 2).invoke(row.rowFrom(2), testResults, catchError);
	}

	private CalledMethodTarget findSpecialMethod(String name) {
		return LookupMethodTarget.findSpecialMethod(this, name);
	}

	public void setGatherExpectedForGeneration(boolean expected) {
		this.expected = expected;
	}

	public void setExpectedResult(Object expectedResult) {
		this.expectedResult = expectedResult;
	}

	public Object getExpectedResult() {
		return expectedResult;
	}

	public static void checkForAmbiguity(String methodName, CalledMethodTarget specialMethod, MethodTarget target) {
		String methodDetails = "method " + methodName + "()";
		String parseMethods = "method " + methodName + "(Row)";

		if (target != null && specialMethod != null) {
			throw new AmbiguousActionException(methodDetails, parseMethods);
		}
	}

	public void setSetUpTraverse(NtafCollectionSetUpTraverse setUpTraverse) {
		this.setUpTraverse = setUpTraverse;
		setUpTraverse.setOuterContext(this);
	}

	public void setSetUpTraverse(Object object) {
		setSetUpTraverse(new NtafCollectionSetUpTraverse(object));
	}

	public void setSettingUp(boolean settingUp) {
		this.settingUp = settingUp;
	}

	public NtafDoTraverse switchSetUp() {
		if (settingUp && setUpTraverse != null) {
			return setUpTraverse;
		}

		return this;
	}

	public void finishSettingUp() {
		setSettingUp(false);
	}

	public void doNotTearDownAutomatically() {
		this.canTearDown = false;
	}

	public void registerFlowControl(FlowControl flowControl) {
		this.flowControl = flowControl;
	}

	public Object executeFixture(Table table, TestResults testResults) {
		Object result;
		result = interpretRow(table.row(0), testResults);

		if (null != result && result instanceof Fixture) {
			((Fixture)result).getArgsForTable(table.parse);
			getAlienTraverseHandler().doTable(result, table, testResults);
		}

		return result;
	}

	public NtafThread executeParallelFixture(Table table, TestResults testResults) {
		NtafThread thread = null;
		Object result = interpretRow(table.row(0), testResults);

		if (null != result && result instanceof Fixture) {
			((Fixture)result).getArgsForTable(table.parse);
			thread = new NtafThread(getAlienTraverseHandler(), result, table, testResults);
		}

		return thread;
	}

	protected boolean checkPreConditions(String keyWord) {
		boolean result = true;

		// attribute "var" is required.
		if (data.getStrVar().equals("")) {
			ntafInfo.printError(keyWord + " VAR is required!");
			result = false;
		}

		// attribute "in" is required.
		if (0 == data.getIterateSize()) {
			ntafInfo.printError(keyWord + " IN is required!");
			result = false;
		}

		return result;
	}

	protected void prepareIterateValue() {
		FitServer.setSymbol(data.getStrVar(), Fixture.replaceSymbol(data.getIterateList().get(cnt)));
		FitServer.setSymbol(data.getStrIndexvar(), cnt);
	}

	protected void addThread(NtafThread thread) {
		if (null != thread) {
			data.getThreadArray().add(thread);
		}
	}

	protected void runThreads() {
		int threadSize = data.getThreadArray().size();

		if (0 < threadSize) {
			// Create
			Thread workingThreads[] = new Thread[threadSize];
			data.setDoneLatch(new CountDownLatch(threadSize));

			for (int j = 0; j < threadSize; ++j) {
				NtafThread thread = data.getThreadArray().get(j);
				thread.setLatch(data.getDoneLatch());
				workingThreads[j] = new Thread(thread);
			}

			// Start
			for (int j = 0; j < threadSize; ++j) {
				workingThreads[j].start();
			}

			// Wait until all threads done
			try {
				data.getDoneLatch().await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			data.getThreadArray().clear();
		}
	}
}
