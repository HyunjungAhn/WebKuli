/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 16/12/2006
 */

package com.nhncorp.ntaf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import fit.FitServer;
import fit.Fixture;
import fit.FlowState;
import fit.Parse;
import fit.RunningState;
import fitlibrary.suite.FlowControl;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class NtafInFlowPageRunner {
	private static final String IGNORE = "@";
	private static final String COMMAND_EXCEPTION = "command_exception";
	private static final String COMMAND_WRONG = "command_wrong";
	private static final String COMMAND_RIGHT = "command_right";
	private static final String FLOW_FIXTURE = "FlowFixture";
	private static final String HELP = "help";
	private static final String COMMAND_LOG = "command_log";
	private static final String COMMAND_VAR = "command_var";
	private static final String COMMAND_GOTO = "command_goto";
	private static final String COMMAND_LABEL = "command_label";
	private static final String COMMAND_EXIT = "command_exit";
	private static final String COMMAND_STRING = "command_string";
	private static final String NUMBER_TYPE = "[0-9]*";
	private static final String NTAF_SYMBOL = "\\$.*\\$";
	private static final String ASSERTEQUALS = "assertEquals";
	private static final Object ASSERTNOTEQUALS = "assertNotEquals";
	private static final Object ASSERTTRUE = "assertTrue";
	private static final Object ASSERTFALSE = "assertFalse";
	private static final Object ASSERTNULL = "assertNull";
	private static final Object ASSERTNOTNULL = "assertNotNull";
	private static final Object ASSERTEXIST = "assertExist";
	private static final Object ASSERTNOTEXIST = "assertNotExist";
	private static HashMap<String, Integer> labelIndex = new HashMap<String, Integer>();
	private NtafDoEvaluator doEvaluator;
	private boolean abandoned = false;
	private boolean stopOnError = false;
	private int stateCount = 0;
	private static int runtableIndex;
	public static int tableSize;
	private int endTablePosition;
	static NtafInfo ntafInfo = new NtafInfo();

	public NtafInFlowPageRunner(NtafDoEvaluator doEvaluator, boolean abandoned) {
		this.doEvaluator = doEvaluator;
		this.abandoned = abandoned;
		doEvaluator.registerFlowControl(new FlowControl() {

			public void abandon() {
				NtafInFlowPageRunner.this.abandoned = true;
			}

			public void setStopOnError(boolean stopOnError) {

			}

		});
	}

	public void printHelp(String keyword, Parse parse) {
		try {
			String resultStr = NtafFixture.parsingXML(FLOW_FIXTURE, keyword);

			if (resultStr.equals("")) {
				resultStr = "Can't find help comment in XML";
			}

			parse.addToBody("<hr>" + "Attribute List" + "<br>" + Fixture.label(resultStr));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getEndTableIndex(Tables tables, int from) {
		int size = tables.size();
		int tableIdx, talbeSize;

		for (tableIdx = from; tableIdx < size; tableIdx++) {
			Table table = tables.table(tableIdx);
			String keywordname = table.row(0).text(0);

			if (NtafInfo.getKeywords().contains(keywordname)) {
				int staeAccumulate = 0;

				if (ntafInfo.isStartKeyword(keywordname)) {

					++staeAccumulate;

					for (talbeSize = tableIdx + 1; talbeSize < size; talbeSize++) {
						Table nextTable = tables.table(talbeSize);
						String nextKeywordname = nextTable.row(0).text(0);

						if (keywordname.equals(nextKeywordname)) {
							++staeAccumulate;
						} else {
							if (ntafInfo.findEndKeyword(keywordname, nextKeywordname)) {
								--staeAccumulate;

								if (0 == staeAccumulate) {
									return talbeSize;
								}
							}
						}
					}
				}
			}
		}

		return size;
	}

	protected boolean isTimeout(RunningState state) {
		return null != state && FlowState.TIMEOUT == state.getRunState();
	}

	public String subStringRestKeywordname(String keywordname) {
		return keywordname.substring(keywordname.indexOf("?") + 1, keywordname.length());
	}

	public String subStringCurrentKeywordname(String keywordname) {
		return keywordname.substring(0, keywordname.indexOf("?"));
	}

	private static void setTableSize(int tableSize) {
		NtafInFlowPageRunner.tableSize = tableSize;
	}

	public int runTables(Tables tables, int from, TableListener tableListener, boolean runByBlock, RunningState state) {
		TestResults testResults = tableListener.getTestResults();
		int tableSize = tables.size();
		int innerTableIndex;
		int tableIndex = 0;
		String keywordname = "";

		setTableSize(tableSize);
		endTablePosition = tableSize;
		saveLabelIndex(tables, from, tableSize);

		int lastRowTest = readTestType();

		for (setRuntableIndex(from); getRuntableIndex() < tableSize; runtableIndex++) {
			if (true == isTimeout(state)) {
				break;
			}

			Table table = tables.table(getRuntableIndex());
			Row rowZero = table.row(0);
			keywordname = rowZero.text(0);

			if (keywordname.startsWith(IGNORE)) {
				tableIndex++;
				finishTable(table, from, tableListener);
				continue;
			}

			if (lastRowTest != 0) {
				if (runtableIndex != 0 && runtableIndex != (tableSize - lastRowTest)) {
					tableIndex++;
					finishTable(table, from, tableListener);
					continue;
				}
			}

			if (true == FitServer.hasSymbol(keywordname)) {
				keywordname = FitServer.getSymbol(keywordname).toString();
				changeTableBody(rowZero.parse.parts, keywordname);
			}

			if (true == NtafInfo.getKeywords().contains(keywordname)) {
				boolean help = isHelp(table);
				ignoreCellIfRequired(testResults, table);
				stateCount = 0;

				if (true == ntafInfo.isStartKeyword(keywordname)) {
					++stateCount;
					ntafInfo.addState(keywordname);
					innerTableIndex = calculateState(tables, testResults, getRuntableIndex(), keywordname);
					parseCommandIfExist(table, keywordname, innerTableIndex);
					runNestedTables(tables, tableListener, state, innerTableIndex, table, keywordname, help);

					if (true == runByBlock) {
						return endTablePosition;
					}

					finishTables(tables, tableIndex, innerTableIndex + 1, tableListener);
					tableIndex = innerTableIndex + 1;
				}

				else {
					runNonPairKeywordAndFixture(tableListener, table, keywordname);
					finishTable(table, from, tableListener);

					if (true == keywordname.equals(COMMAND_EXIT)) {
						return endTablePosition;
					}

					if (true == keywordname.equals(COMMAND_GOTO)) {
						setRuntableIndex(labelIndex.get(rowZero.text(1)));
					}

					tableIndex++;
				}

			} else {
				tableIndex++;
				runOtherTables(tableListener, testResults, table);
				finishTable(table, from, tableListener);
			}
		}

		return endTablePosition;
	}

	private int readTestType() {
		int lastRowTest = 0;
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("test_type.conf")));
			lastRowTest = Integer.parseInt(bufferedReader.readLine().split("=")[1]);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return lastRowTest;
	}

	public boolean matchesMultiTestForm(String keywordname) {
		return keywordname.matches("([a-zA-Z]*\\?)*");
	}

	private void changeTableBody(Parse parse, String keywordname) {
		parse.body = keywordname;
		parse.orgbody = keywordname;
	}

	private void saveLabelIndex(Tables tables, int from, int tableSize) {
		for (int index = from; index < tableSize; index++) {
			Table table = tables.table(index);
			Row rowZero = table.row(0);
			String keywordname = rowZero.text(0);

			if (true == keywordname.equals(COMMAND_LABEL)) {
				labelIndex.put(rowZero.text(1), index);
			}
		}
	}

	private void runNestedTables(Tables tables, TableListener tableListener, RunningState state, int innerTableIndex, Table table, String keywordname, boolean help) {
		TestResults testResults = tableListener.getTestResults();

		if (isIgnore(testResults, stateCount)) {
			ignoreCellIfRequired(testResults, table);
		} else {
			if (help) {
				printHelp(keywordname, table.row(0).cell(1).parse);
			} else {
				doEvaluator.interpretInnerTables(tables, getRuntableIndex() + 1, innerTableIndex, tableListener, state);
				endTablePosition = setRuntableIndex(innerTableIndex);
			}
		}
	}

	private int calculateState(Tables tables, TestResults testResults, int tableIndex, String keywordname) {
		int tableSize = tables.size();
		int innerTableIndex;

		for (innerTableIndex = tableIndex + 1; innerTableIndex < tableSize; innerTableIndex++) {
			Table nextTable = tables.table(innerTableIndex);
			String nextKeywordname = nextTable.row(0).text(0);

			if (keywordname.equals(nextKeywordname)) {
				++stateCount;
			} else {
				if (ntafInfo.findEndKeyword(keywordname, nextKeywordname)) {
					ignoreCellIfRequired(testResults, nextTable);
					--stateCount;

					if (0 == stateCount) {
						break;
					}
				}
			}
		}

		return innerTableIndex;
	}

	private boolean isError(TestResults testResults) {
		return abandoned || (stopOnError && testResults.problems());
	}

	private boolean isIgnore(TestResults testResults, int staeAccumulate) {
		return isError(testResults) || 0 != staeAccumulate;
	}

	private void runOtherTables(TableListener tableListener, TestResults testResults, Table table) {
		if (isError(testResults)) {
			ignoreCellIfRequired(testResults, table);
		} else {
			doEvaluator.interpretWholeTable(table, tableListener);
		}
	}

	private void runNonPairKeywordAndFixture(TableListener tableListener, Table table, String keywordname) {
		Row rowZero = table.row(0);
		TestResults testResults = tableListener.getTestResults();

		if (keywordname.equals(COMMAND_LOG) && rowZero.cellExists(1)) {
			ntafInfo.printLog(rowZero.text(1));
		} else if (true == keywordname.equals(COMMAND_RIGHT)) {
			testResults.pass();
		} else if (true == keywordname.equals(COMMAND_WRONG)) {
			testResults.fail();
		} else if (true == keywordname.equals(COMMAND_EXCEPTION)) {
			testResults.exception();
		} else if (true == keywordname.equals(COMMAND_VAR) && false == rowZero.cellExists(2)) {
			rowZero.addCell().setText(FlowFixture.replaceSymbol(rowZero.text(1)));
			rowZero.cell(0).ignore(testResults);
		} else if (true == keywordname.equals(COMMAND_GOTO)) {
		} else if (true == keywordname.equals(COMMAND_LABEL)) {
		} else if (true == keywordname.equals(COMMAND_EXIT)) {
		} else if (true == keywordname.equals(COMMAND_STRING) && true == rowZero.cellExists(1) && true == rowZero.cellExists(2) && true == rowZero.cellExists(3)) {
			executeCommandString(rowZero, tableListener);
		} else if (true == (keywordname.equals(ASSERTEQUALS) | keywordname.equals(ASSERTNOTEQUALS) | keywordname.equals(ASSERTTRUE) | keywordname.equals(ASSERTFALSE) | keywordname.equals(ASSERTNULL) | keywordname.equals(ASSERTNOTNULL) | keywordname.equals(ASSERTEXIST) | keywordname.equals(ASSERTNOTEXIST))) {
			checkAssertStatement(testResults, rowZero, keywordname);
		} else {
			doEvaluator.interpretWholeTable(table, tableListener);
		}
	}

	public static void checkAssertStatement(TestResults testResults, Row row, String keywordname) {
		Parse expected = row.cell(1).parse;

		if (true == keywordname.equals(ASSERTEQUALS)) {
			Parse actual = row.cell(2).parse;

			setResultAssertCell(testResults, row, NtafInFlowPageRunner.assertEquals(row.text(1), row.text(2)));
			actual.body = NtafInFlowPageRunner.decorateResultString(actual);
		} else if (true == keywordname.equals(ASSERTNOTEQUALS)) {
			Parse actual = row.cell(2).parse;

			setResultAssertCell(testResults, row, NtafInFlowPageRunner.assertNotEquals(row.text(1), row.text(2)));
			actual.body = NtafInFlowPageRunner.decorateResultString(actual);
		} else if (true == keywordname.equals(ASSERTTRUE)) {
			setResultAssertCell(testResults, row, NtafInFlowPageRunner.assertTrue(row.text(1)));
		} else if (true == keywordname.equals(ASSERTFALSE)) {
			setResultAssertCell(testResults, row, NtafInFlowPageRunner.assertFalse(row.text(1)));
		} else if (true == keywordname.equals(ASSERTNULL)) {
			setResultAssertCell(testResults, row, NtafInFlowPageRunner.assertNull(row.text(1)));
		} else if (true == keywordname.equals(ASSERTNOTNULL)) {
			setResultAssertCell(testResults, row, NtafInFlowPageRunner.assertNotNull(row.text(1)));
		} else if (true == keywordname.equals(ASSERTEXIST)) {
			setResultAssertCell(testResults, row, NtafInFlowPageRunner.assertExist(row.text(1)));
		} else if (true == keywordname.equals(ASSERTNOTEXIST)) {
			setResultAssertCell(testResults, row, NtafInFlowPageRunner.assertNotExist(row.text(1)));
		}

		expected.body = NtafInFlowPageRunner.decorateResultString(expected);
	}

	public static void setResultAssertCell(TestResults testResults, Row row, Boolean assertResult) {
		if (true == assertResult) {
			row.cell(1).pass(testResults);
		} else {
			row.cell(1).fail(testResults);
		}
	}

	public static String decorateResultString(Parse cell) {
		String resultStr;

		if (true == FitServer.hasSymbol(cell.orgbody)) {
			resultStr = cell.orgbody + "=" + FitServer.getSymbol(cell.orgbody);
		} else {
			resultStr = cell.orgbody;
		}

		return resultStr;
	}

	public static Boolean assertExist(String text) {
		return FitServer.hasSymbol(text);
	}

	public static Boolean assertNotExist(String text) {
		return !assertExist(text);
	}

	public static boolean assertTrue(String condition) {
		if (true == FitServer.hasSymbol(condition)) {
			condition = FitServer.getSymbol(condition).toString();
		}

		NtafInfo ntafInfo = new NtafInfo();

		return ntafInfo.checkCondition(condition);
	}

	public static boolean assertFalse(String condition) {
		return !assertTrue(condition);
	}

	public static boolean assertNull(String symbol) {
		Object obj = null;

		if (FitServer.hasSymbol(symbol)) {
			obj = FitServer.getSymbol(symbol);
		} else {
			obj = symbol;
		}

		return obj == null ? true : false;
	}

	public static boolean assertNotNull(String symbol) {
		return !assertNull(symbol);
	}

	public static boolean assertNotEquals(final String expected, final String actual) {
		return !assertEquals(expected, actual);
	}

	public static boolean assertEquals(String expected, String actual) {
		boolean result = false;

		if (FitServer.hasSymbol(expected)) {
			expected = FitServer.getSymbol(expected).toString();
		}

		if (FitServer.hasSymbol(actual)) {
			actual = FitServer.getSymbol(actual).toString();
		}

		if (NtafInfo.isNumberCompare(expected, actual)) {
			final double actualNumber = NtafInfo.castingNumber(actual);
			final double expectedNumber = NtafInfo.castingNumber(expected);

			if (NtafInfo.isSameDouble(expectedNumber, actualNumber)) {
				result = true;
			} else {
				result = false;
			}
		} else if (expected.equals(actual)) {
			result = true;
		}

		return result;
	}

	public static void executeCommandString(Row rowZero, TableListener tableListener) {
		String symbol = rowZero.text(1);
		String method = rowZero.text(2).toLowerCase(Locale.US);
		String parameter = rowZero.text(3);
		TestResults testResults = tableListener.getTestResults();

		if (true == rowZero.cell(2).isIgnored()) {
			rowZero.cell(2).ignore(testResults);
		}

		if (true == FitServer.hasSymbol(symbol)) {
			String symbolValue = FitServer.getSymbol(symbol).toString();

			if (true == method.equals("regx")) {
				executeCommandRegxString(rowZero, symbol, parameter, symbolValue);
			} else if (true == method.equals("substring")) {
				execueteCommandSubString(rowZero, symbol, parameter, symbolValue);
			} else if (true == method.equals("contain")) {
				executeCommandContainString(rowZero, parameter, testResults, symbolValue);
			} else if (true == method.equals("length")) {
				executeCommandLengthString(rowZero, symbol, parameter, symbolValue);
			}
		}
	}

	private static void executeCommandLengthString(Row rowZero, String symbol, String parameter, String symbolValue) {
		String stringSize = Integer.toString(symbolValue.length());
		symbol = rowZero.text(3);
		FitServer.setSymbol(symbol, stringSize);
		rowZero.addCell().setText(stringSize);
	}

	private static void executeCommandContainString(Row rowZero, String parameter, TestResults testResults, String symbolValue) {
		String flagString;

		if (true == parameter.matches(NTAF_SYMBOL) && true == FitServer.hasSymbol(parameter)) {
			parameter = FitServer.getSymbol(parameter).toString();
		}

		if (true == symbolValue.contains(parameter)) {
			flagString = "true";
			if (false == rowZero.cellExists(4)) {
				rowZero.cell(3).pass(testResults);
			}
		} else {
			flagString = "false";
			if (false == rowZero.cellExists(4)) {
				rowZero.cell(3).fail(testResults);
			}
		}

		if (true == rowZero.cellExists(4) && true == rowZero.text(4).matches(NTAF_SYMBOL)) {
			String symbol = rowZero.text(4);
			FitServer.setSymbol(symbol, flagString);
			rowZero.addCell().setText(flagString);
		}
	}

	private static void execueteCommandSubString(Row rowZero, String symbol, String parameter, String symbolValue) {
		String subedString;

		if (true == rowZero.cellExists(5) && true == rowZero.text(5).matches(NTAF_SYMBOL)) {
			subedString = symbolValue.substring(Integer.parseInt(parameter), Integer.parseInt(rowZero.text(4)));
			symbol = rowZero.text(5);
		} else if (true == rowZero.cellExists(4) && false == rowZero.text(4).equals("") && true == rowZero.text(4).matches(NUMBER_TYPE)) {
			subedString = symbolValue.substring(Integer.parseInt(parameter), Integer.parseInt(rowZero.text(4)));
		} else {
			subedString = symbolValue.substring(Integer.parseInt(parameter));

			if (true == rowZero.cellExists(4) && true == rowZero.text(4).matches(NTAF_SYMBOL)) {
				symbol = rowZero.text(4);
			}
		}

		FitServer.setSymbol(symbol, subedString);
		rowZero.addCell().setText(subedString);
	}

	private static void executeCommandRegxString(Row rowZero, String symbol, String parameter, String symbolValue) {
		Pattern pattern = Pattern.compile(parameter);
		Matcher matcher = pattern.matcher(symbolValue);

		if (true == matcher.find()) {
			int groupNo = 0;
			symbol = rowZero.text(4);

			if (true == rowZero.cellExists(4)) {
				groupNo = Integer.parseInt(rowZero.text(4));
				symbol = rowZero.text(5);
			}

			FitServer.setSymbol(symbol, matcher.group(groupNo));
			rowZero.addCell().setText(matcher.group(groupNo));
		}
	}

	private void parseCommandIfExist(Table table, String keywordname, int innerTableIndex) {
		if (table.row(0).cellExists(1)) {
			ntafInfo.parseCommand(keywordname, table.row(0).text(1), innerTableIndex);
		}
	}

	private void ignoreCellIfRequired(TestResults testResults, Table table) {
		if (true == table.row(0).text(0).equals(COMMAND_VAR)) {
			return;
		}

		if (false == table.row(0).cell(0).isIgnored()) {
			table.row(0).cell(0).ignore(testResults);
		}
	}

	protected boolean isHelp(Table table) {
		return table.row(0).cellExists(1) && table.row(0).cell(1).text().equals(HELP);
	}

	private void finishTables(Tables tables, int from, int to, TableListener tableListener) {
		for (int i = from; i < to; i++) {
			tableListener.tableFinished(tables.table(i));
		}

	}

	private void finishTable(Table table, int from, TableListener tableListener) {
		tableListener.tableFinished(table);
	}

	public void run(Tables tables, int from, TableListener tableListener) {
		doEvaluator.doNotTearDownAutomatically();
		RunningState childState = new RunningState();
		runTables(tables, from, tableListener, false, childState);
		doEvaluator.tearDown(tables.table(0), tableListener.getTestResults());
	}

	public void setStopOnError(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}

	public void setAbandon(boolean abandoned) {
		this.abandoned = abandoned;
	}

	public static int setRuntableIndex(int runtableIndex) {
		NtafInFlowPageRunner.runtableIndex = runtableIndex;
		return runtableIndex;
	}

	public static int getRuntableIndex() {
		return runtableIndex;
	}
}
