// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.package fit;

package fit;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.*;
import java.text.*;
import fit.exception.*;

public class Fixture {
	private static final String DOUBLETYPE = "[\\-]*[0-9]+\\.[0-9]+";
	protected static final String ITERATION_START = "<!--ITERATION-->";
	private static final String WHOLE_WORD = "[\\w\\W]*";
	private static final String DOLLAR = "[$]";
	private static final String EXCLUDE_DOLLAR = "[\\w\\W&&[^$]]*";
	private static final String INCLUDE_DOLLAR = "[$][\\w\\W]*[$]";
	private static final String ASSIGN = "(\\s)*[=](\\s)*";
	private static final String PP = "([+][+])";
	private static final String MM = "([-][-])";
	private static final String PP_MM = PP + "|" + MM;
	private static final String PLUS = "(\\s)*[+][=](\\s)*";
	private static final String MINUS = "(\\s)*[-][=](\\s)*";
	private static final String MULTI = "(\\s)*[*][=](\\s)*";
	private static final String DIV = "(\\s)*[/][=](\\s)*";
	private static final String OPERATOR = "(" + PLUS + WHOLE_WORD + ")|" + "(" + MINUS + WHOLE_WORD + ")|" + "(" + MULTI + WHOLE_WORD + ")|" + "(" + DIV + WHOLE_WORD + ")";
	private static final Pattern SYMBOLPATTERN = Pattern.compile("(" + DOLLAR + "(" + EXCLUDE_DOLLAR + ")" + DOLLAR
		+ ASSIGN + WHOLE_WORD + "|" + PP_MM + "|" + OPERATOR + "|" + DOLLAR + "(" + EXCLUDE_DOLLAR + ")" + DOLLAR + "|"
		+ "(" + DOLLAR + EXCLUDE_DOLLAR + ")|" + "(" + EXCLUDE_DOLLAR + "))");
	private static final Pattern INCLUDEDOLLARPATTERN = Pattern.compile("(" + INCLUDE_DOLLAR + ")");
	private static final Pattern EXCLUDEDOLLARPATTERN = Pattern.compile("(" + EXCLUDE_DOLLAR + ")");
	private static final Pattern ASSIGNPATTERN = Pattern.compile("(" + INCLUDE_DOLLAR + ")(" + ASSIGN + WHOLE_WORD + ")");
	private static final Pattern OPERATORPATTERN = Pattern.compile("(" + ASSIGN + WHOLE_WORD + ")|" + PP_MM + "|" + OPERATOR);
	public Map<String, Object> summary = new HashMap<String, Object>();
	public Counts counts = new Counts();
	public FixtureListener listener = new NullFixtureListener();
	protected String[] args;
	protected String strResponse;
	protected String strErrorMsg;
	private static boolean forcedAbort = false; //Semaphores

	public static void setForcedAbort(boolean state) {
		forcedAbort = state;
	} //Semaphores

	public static boolean getForcedAbort() {
		return forcedAbort;
	} //Semaphores

	public boolean isDoRows = false;

	protected Class getTargetClass() {
		return getClass();
	}

	public class RunTime {
		long start = System.currentTimeMillis();

		long elapsed = 0;

		public String toString() {
			elapsed = System.currentTimeMillis() - start;
			if (elapsed > 600000) {
				return d(3600000) + ":" + d(600000) + d(60000) + ":" + d(10000) + d(1000);
			} else {
				return d(60000) + ":" + d(10000) + d(1000) + "." + d(100) + d(10);
			}
		}

		String d(long scale) {
			long report = elapsed / scale;
			elapsed -= report * scale;
			return Long.toString(report);
		}
	}

	/* Altered by Rick to dispatch on the first Fixture */
	public void doTables(Parse tables) {
		summary.put("run date", new Date());
		summary.put("run elapsed time", new RunTime());
		if (tables != null) {
			Parse heading = tables.at(0, 0, 0);

			if (heading != null) {
				try {
					Fixture fixture = getLinkedFixtureWithArgs(tables);
					fixture.listener = listener;
					fixture.interpretTables(tables);
				} catch (Throwable e) {
					exception(heading, e);
					interpretFollowingTables(tables);
				}
			}
		}

		writeTestResultForChart(tables);

		listener.tablesFinished(counts);
		SemaphoreFixture.ClearSemaphores(); //Semaphores:  clear all at end
	}

	private void writeTestResultForChart(Parse tables) {
		FileWriter writer;
		String resultPagePath;
		Pattern pattern = Pattern.compile("!store \\{([^\r\n]*)\\}");
		Matcher match = pattern.matcher(tables.leader);

		if (match.find()) {
			resultPagePath = match.group(1);

			try {
				File file = new File(resultPagePath);

				if (false == file.exists()) {
					writer = new FileWriter(resultPagePath, false);
					writer.write(String.valueOf(counts.wrong) + "\n");
					writer.write(String.valueOf(counts.right) + "\n");
					writer.write(String.valueOf(counts.exceptions) + "\n");
					writer.close();
				} else {
					try {
						String line = null;
						int num[] = new int[] {counts.wrong, counts.right, counts.exceptions};
						int i = 0;

						FileReader f = new FileReader(resultPagePath);
						BufferedReader buff = new BufferedReader(f);
						StringBuffer sb = new StringBuffer();

						while ((line = buff.readLine()) != null) {
							sb.append(line + "," + String.valueOf(num[i++]) + "\n");
						}

						buff.close();
						BufferedWriter bw = new BufferedWriter(new FileWriter(resultPagePath));
						bw.write(sb.toString());
						bw.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/* Added by Rick to allow a dispatch into DoFixture */
	protected void interpretTables(Parse tables) {
		try { // Don't create the first fixture again, because creation may do something important.
			getArgsForTable(tables); // get them again for the new fixture object
			doTable(tables);
		} catch (Exception ex) {
			exception(tables.at(0, 0, 0), ex);
			listener.tableFinished(tables);
			return;
		}
		interpretFollowingTables(tables);
	}

	/* Added by Rick */
	public void interpretFollowingTables(Parse tables) {
		listener.tableFinished(tables);
		tables = tables.more;
		while (tables != null) {
			Parse heading = tables.at(0, 0, 0);

			if (forcedAbort)
				ignore(heading); //Semaphores: ignore on failed lock
			else if (heading != null) {
				try {
					Fixture fixture = getLinkedFixtureWithArgs(tables);
					fixture.doTable(tables);
				} catch (Throwable e) {
					exception(heading, e);
				}

			}
			listener.tableFinished(tables);
			tables = tables.more;
		}
	}

	/* Added by Rick */
	protected Fixture getLinkedFixtureWithArgs(Parse tables) throws Throwable {
		Parse header = tables.at(0, 0, 0);
		Fixture fixture = loadFixture(header.text());
		fixture.counts = counts;
		fixture.summary = summary;
		fixture.getArgsForTable(tables);
		return fixture;
	}

	public static Fixture loadFixture(String fixtureName) throws Throwable {
		return FixtureLoader.instance().disgraceThenLoad(fixtureName);
	}

	public void getArgsForTable(Parse table) {
		List<String> argumentList = new ArrayList<String>();
		Parse parameters = table.parts.parts.more;
		for (; parameters != null; parameters = parameters.more) {
			argumentList.add(parameters.text());
		}

		args = (String[])argumentList.toArray(new String[0]);
	}

	public void doTable(Parse table) {
		doRows(table.parts.more);
	}

	public void doRows(Parse rows) {
		while (rows != null) {
			Parse more = rows.more;
			doRow(rows);
			rows = more;
		}
	}

	public void doRow(Parse row) {
		doCells(row.parts);
	}

	public void doCells(Parse cells) {
		for (int i = 0; cells != null; i++) {
			try {
				doCell(cells, i);
			} catch (Exception e) {
				exception(cells, e);
			}
			cells = cells.more;
		}
	}

	public void doCell(Parse cell, int columnNumber) {
		ignore(cell);
	}

	// Annotation ///////////////////////////////



	protected void addTableToCell(Parse cell, String className, String content, boolean upLine) {
		cell.addToBody(ITERATION_START);
		
		if (true == upLine) {
			cell.addToBody("<hr>");
		}
		
		cell.addToBody("<div class=" + className + ">" + content + "</div>");
	}
	
	private String decorateResultString(Parse cell) {
		String resultStr;

		if (true == FitServer.hasSymbol(cell.orgbody)) {
			resultStr = escape(cell.orgbody) + "=" + FitServer.getSymbol(cell.orgbody);
		} 
		else {
			resultStr = cell.orgbody;
		}

		return resultStr;
	}
	
	public void right(Parse cell) {
		String resultStr = decorateResultString(cell);

		if (false == cell.body.contains(ITERATION_START)) {
			cell.body = "";
			addTableToCell(cell, "pass", resultStr, false);
		}
		else {
			addTableToCell(cell, "pass", resultStr, true);
		}
		
		counts.right++;
	}
	

	public void wrong(Parse cell) {
		counts.wrong++;
	}

	public void wrong(Parse cell, String actual) {
		wrong(cell);
		String resultStr = decorateResultString(cell);
		resultStr = resultStr + label("expected &nbsp;") + actual + label("actual");
		
		if (false == cell.body.contains(ITERATION_START)) {
			cell.body = "";
			addTableToCell(cell, "fail", resultStr, false);
		}
		else {
			addTableToCell(cell, "fail", resultStr, true);
		}
	}


	public void ignore(Parse cell) {
		cell.addToTag(" class=\"ignore\"");
		counts.ignores++;
	}

	public void exception(Parse cell, Throwable exception) {
		while (exception.getClass().equals(InvocationTargetException.class)) {
			exception = ((InvocationTargetException)exception).getTargetException();
		}
		if (isFriendlyException(exception)) {
			cell.addToBody("<hr/>" + label(exception.getMessage()));
		} else {
			final StringWriter buf = new StringWriter();
			exception.printStackTrace(new PrintWriter(buf));
			cell.addToBody("<hr><pre><div class=\"fit_stacktrace\">" + (buf.toString()) + "</div></pre>");
		}
		cell.addToTag(" class=\"error\"");
		counts.exceptions++;
	}

	public boolean isFriendlyException(Throwable exception) {
		return exception instanceof FitFailureException;
	}

	// Utility //////////////////////////////////

	public String counts() {
		return counts.toString();
	}

	public static String label(String string) {
		return " <span class=\"fit_label\">" + string + "</span>";
	}

	public static String gray(String string) {
		return " <span class=\"fit_grey\">" + string + "</span>";
	}

	public static String escape(String string) {
		return escape(escape(string, '&', "&amp;"), '<', "&lt;");
	}

	public static String escape(String string, char from, String to) {
		int i = -1;
		while ((i = string.indexOf(from, i + 1)) >= 0) {
			if (i == 0) {
				string = to + string.substring(1);
			} else if (i == string.length()) {
				string = string.substring(0, i) + to;
			} else {
				string = string.substring(0, i) + to + string.substring(i + 1);
			}
		}
		return string;
	}

	public static String camel(String name) {
		StringBuffer b = new StringBuffer(name.length());
		StringTokenizer t = new StringTokenizer(name);
		b.append(t.nextToken());
		while (t.hasMoreTokens()) {
			String token = t.nextToken();
			b.append(token.substring(0, 1).toUpperCase()); // replace spaces with
			// camelCase
			b.append(token.substring(1));
		}
		return b.toString();
	}

	public Object parse(String s, Class type) throws Exception {
		if (type.equals(String.class)) {
			if (s.toLowerCase().equals("null"))
				return null;
			else if (s.toLowerCase().equals("blank"))
				return "";
			else
				return s;
		} else if (type.equals(Date.class)) {
			return DateFormat.getDateInstance(DateFormat.SHORT).parse(s);
		} else if (hasParseMethod(type)) {
			return callParseMethod(type, s);
		} else {
			throw new CouldNotParseFitFailureException(s, type.getName());
		}
	}

	public void check(Parse cell, TypeAdapter a) {
		String text = replaceSymbol(cell.text());

		if (text.equals(""))
			handleBlankCell(cell, a);
		else if (a == null)
			ignore(cell);
		else if (text.equals("error"))
			handleErrorInCell(a, cell);
		else
			compareCellToResult(a, cell);
	}

	private void compareCellToResult(TypeAdapter a, Parse cell) {
		new CellComparator().compareCellToResult(a, cell);
	}

	public void handleBlankCell(Parse cell, TypeAdapter a) {
		try {
			if (cell.isClean()) {
				cell.body = "";
				String strValue = a.toString(a.get());
				cell.addToBody(gray(strValue.trim()));
			} else
				cell.addToBody("<hr>" + gray(a.toString(a.get())));
		} catch (Exception e) {
			if (cell.isClean())
				cell.addToBody(gray("error"));
			else
				cell.addToBody("<hr>" + gray("error"));

		}
	}

	private void handleErrorInCell(TypeAdapter a, Parse cell) {
		try {
			Object result = a.invoke();
			wrong(cell, a.toString(result));
		} catch (IllegalAccessException e) {
			exception(cell, e);
		} catch (Exception e) {
			right(cell);
		}
	}

	public String[] getArgs() {
		return args;
	}

	public static boolean hasParseMethod(Class type) {
		try {
			type.getMethod("parse", new Class[] {String.class});
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	public static Object callParseMethod(Class type, String s) throws Exception {
		Method parseMethod = type.getMethod("parse", new Class[] {String.class});
		Object o = parseMethod.invoke(null, new Object[] {s});
		return o;
	}

	public static String executeOperator(String operator, String symbol) {
		String operatorValue = operator.trim();
		String symbolValue = symbol.trim();
		String result;

		if (isNumberOperation(operatorValue, symbolValue)) {
			result = getNumberResult(operatorValue, symbolValue);
		} else {
			result = getStringResult(operatorValue, symbolValue);
		}

		return result;
	}

	private static Object checkNumberType(String symbolValue) {
		if (symbolValue.matches(DOUBLETYPE)) {
			return Double.parseDouble(symbolValue);
		}

		else {
			return Integer.parseInt(symbolValue);
		}
	}

	private static String getNumberResult(String operatorValue, String symbolValue) {
		Object obj = checkNumberType(symbolValue);

		if (obj instanceof Double) {
			return calculateNumber(operatorValue, (Double)obj);
		}

		else if (obj instanceof Integer) {
			return calculateNumber(operatorValue, (Integer)obj);
		}

		else {
			return obj.toString();
		}
	}

	private static String getCalulationValue(String argumentValue) {
		if (argumentValue.length() < 3) {
			return "";
		}

		String result = argumentValue.substring(2).trim();

		if (FitServer.hasSymbol(result)) {
			result = replaceSymbol(result);
		}

		return result;
	}

	private static String calculateNumber(String operatorValue, double symbolValue) {
		double resultValue = symbolValue;

		if (operatorValue.contains("++")) {
			resultValue++;
		} else if (operatorValue.contains("--")) {
			resultValue--;
		} else if (operatorValue.contains("+=")) {
			resultValue += Double.parseDouble(getCalulationValue(operatorValue));
			;
		} else if (operatorValue.contains("-=")) {
			resultValue -= Double.parseDouble(getCalulationValue(operatorValue));
			;
		} else if (operatorValue.contains("*=")) {
			resultValue *= Double.parseDouble(getCalulationValue(operatorValue));
			;
		} else if (operatorValue.contains("/=")) {
			resultValue /= Double.parseDouble(getCalulationValue(operatorValue));
			;
		}

		return String.valueOf(resultValue);
	}

	private static String calculateNumber(String operatorValue, int symbolValue) {
		int resultValue = symbolValue;

		if (operatorValue.contains("++")) {
			resultValue++;
		} else if (operatorValue.contains("--")) {
			resultValue--;
		} else if (operatorValue.contains("+=")) {
			resultValue += Double.parseDouble(getCalulationValue(operatorValue));
			;
		} else if (operatorValue.contains("-=")) {
			resultValue -= Double.parseDouble(getCalulationValue(operatorValue));
			;
		} else if (operatorValue.contains("*=")) {
			resultValue *= Double.parseDouble(getCalulationValue(operatorValue));
			;
		} else if (operatorValue.contains("/=")) {
			resultValue /= Double.parseDouble(getCalulationValue(operatorValue));
			;
		}

		return String.valueOf(resultValue);
	}

	private static String getStringResult(String operatorValue, String symbolValue) {
		String result = symbolValue;

		String calulationValue = getCalulationValue(operatorValue);

		if (operatorValue.contains("++") && 1 <= result.length()) {
			result += result.charAt(0);
		} else if (isStringDecrementable(operatorValue, result)) {
			result = result.subSequence(0, result.length() - 1).toString();
		} else if (operatorValue.contains("+=")) {
			result += calulationValue;
		} else if (operatorValue.contains("-=")) {
			result = result.replace(calulationValue, "");
		} else if (operatorValue.contains("*=")) {
			result = getTimeOperationResult(result, calulationValue);
		} else if (operatorValue.contains("/=")) {
			result = result.replaceAll(calulationValue, "");
		}

		return result;
	}

	private static boolean isStringDecrementable(String operatorValue, String result) {
		return operatorValue.contains("--") && result.length() >= 1;
	}

	private static String getTimeOperationResult(String input, String calulationValue) {
		String result = input;

		if (isNumberOperation(calulationValue, calulationValue)) {
			String addString = result;
			for (long i = 0; i < Long.parseLong(calulationValue); ++i) {
				result += addString;
			}
		} else {
			result += calulationValue;
		}
		return result;
	}

	private static boolean isNumberOperation(String operatorValue, String symbolValue) {
		boolean isNumber = false;
		try {
			Double.parseDouble(symbolValue);
			if (operatorValue.length() > 2) {
				Double.parseDouble(getCalulationValue(operatorValue));
			}

			isNumber = true;
		} catch (NumberFormatException e) {
			isNumber = false;
		}

		return isNumber;
	}

	public static synchronized String replaceSymbol(String str) {
		String replacedString = "";
		String addedString = "";
		String symbolString = "";
		Matcher outterMatcher = SYMBOLPATTERN.matcher(str);

		while (true == outterMatcher.find()) {
			String outterGroup = outterMatcher.group();
			Matcher innerMatcher = ASSIGNPATTERN.matcher(outterGroup);

			if (true == innerMatcher.matches()) {
				int assignedIndex = innerMatcher.group().indexOf("=");
				StringTokenizer stok = new StringTokenizer(innerMatcher.group(), "= \t", false);

				if (true == stok.hasMoreTokens()) {
					symbolString = stok.nextToken();
					replacedString += innerMatcher.group().substring(assignedIndex + 1).trim();
					replacedString = replaceSymbol(replacedString);
					FitServer.setSymbol(symbolString, "null".equals(replacedString) ? null : replacedString);
					break;
				}
			}

			innerMatcher = INCLUDEDOLLARPATTERN.matcher(outterGroup);
			if (true == innerMatcher.matches()) {
				symbolString = innerMatcher.group();
				Object returnSymbol = FitServer.getSymbol(symbolString);

				if (null != returnSymbol) {
					addedString += returnSymbol.toString().trim();
				} else {
					addedString += symbolString;
				}
			}

			innerMatcher = OPERATORPATTERN.matcher(outterGroup);
			if (true == innerMatcher.matches()) {
				replacedString += executeOperator(innerMatcher.group(), addedString);
				FitServer.setSymbol(symbolString, "null".equals(replacedString) ? null : replacedString);
				symbolString = "";
				addedString = "";
			} else {
				innerMatcher = EXCLUDEDOLLARPATTERN.matcher(outterGroup);
				if (true == innerMatcher.matches()) {
					replacedString += addedString + outterGroup;
					addedString = "";
				}
			}
		}

		return replacedString;
	}

	// TODO-RcM I might be moving out of here. Can you help me find a home of my
	// own?
	private class CellComparator {
		private Object result = null;

		private Object expected = null;

		private TypeAdapter typeAdapter;

		private Parse cell;

		private void compareCellToResult(TypeAdapter a, Parse theCell) {
			typeAdapter = a;
			cell = theCell;

			try {
				result = typeAdapter.get();
				expected = parseCell();

				if (expected instanceof Unparseable)
					tryRelationalMatch();
				else
					compare();
			} catch (Exception e) {
				exception(cell, e);
			}
		}

		private void compare() {
			prepareCompare();

			if (typeAdapter.equals(expected, result)) {
				right(cell);
			} else {
				wrong(cell, typeAdapter.toString(result));
			}
		}

		private void prepareCompare() {
			if (expected instanceof String && FitServer.hasSymbol(expected.toString()))
				expected = replaceSymbol(expected.toString());

			if (result instanceof String && FitServer.hasSymbol(result.toString())) {
				result = replaceSymbol(result.toString());
			}
		}

		private Object parseCell() {
			try {
				String symbolResolved = replaceSymbol(cell.text());
				return typeAdapter.isRegex ? cell.text() : typeAdapter.parse(symbolResolved);
			}
			// Ignore parse exceptions, print non-parse exceptions,
			// return null so that compareCellToResult tries relational matching.
			catch (NumberFormatException e) {
			} catch (ParseException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new Unparseable();
		}

		private void tryRelationalMatch() {
			Class adapterType = typeAdapter.type;
			FitFailureException cantParseException = new CouldNotParseFitFailureException(cell.text(),
				adapterType.getName());
			if (result != null) {
				FitMatcher matcher = new FitMatcher(cell.text(), result);
				try {
					if (matcher.matches())
						right(cell);
					else
						wrong(cell);
					cell.body = matcher.message();
				} catch (FitMatcherException fme) {
					exception(cell, cantParseException);
				} catch (Exception e) {
					exception(cell, e);
				}
			} else {
				// TODO-RcM Is this always accurate?
				exception(cell, cantParseException);
			}
		}
	}

	private class Unparseable {
	}
}