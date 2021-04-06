package com.nhncorp.ntaf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fit.FitServer;
import fit.Fixture;

public class NtafInfo {
	private static final String ALLWORDS = "(.*)";
	private static final double DOUBLE_PRECISION = 0.000001;
	public static final String TRUE_ONE = "1";
	public static final String TRUE = "true";
	static private String keywords;
	static private HashMap<String, String> attributes = new HashMap<String, String>();
	static private HashMap<HashMap<Integer, NtafFlowState>, Boolean> ifConditions = new HashMap<HashMap<Integer, NtafFlowState>, Boolean>();
	static private LinkedList<NtafFlowState> stateList = new LinkedList<NtafFlowState>();
	static private ArrayList<String> inList = new ArrayList<String>();

	protected void parseCommand(String keyword, String command, int innerTableIndex) {
		if (0 != inList.size()) {
			inList.clear();
		}

		StringTokenizer stok = new StringTokenizer(command, "{}", false);

		while (stok.hasMoreTokens()) {
			StringTokenizer stokElement = new StringTokenizer(stok.nextToken(), ":", false);
			String attrName = stokElement.nextToken().trim();

			while (stokElement.hasMoreTokens()) {
				String attrValue = stokElement.nextToken();
				
				if ("expr".equals(attrName) && ("start_if".equals(keyword) || "start_elseif".equals(keyword))) {
					attributes.put(innerTableIndex + attrName, attrValue);
				} else {
					attributes.put(attrName, attrValue);
				}

				attributes.put(attrName, attrValue);

				if (keyword.contains("iterate") && attrName.equals("in")) {
					inList.addAll(getAllValuesInCommaList(attrValue));
				}
			}
		}
	}

	static protected List<String> getAllValuesInCommaList(final String attrValue) {
		List<String> values = new ArrayList<String>();
		String trimedAttrValue = attrValue.trim();

		if (!trimedAttrValue.equals("")) {
			for (String token : trimedAttrValue.split(",")) {
				token = token.trim();

				if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
					token = token.substring(1, token.length() - 1);
				}

				values.add(token);
			}
		}

		return values;
	}

	public String replaceSymbol(String str) {
		return Fixture.replaceSymbol(str);
	}

	static final private String[] OPERATORS = {"<", ">", "==", "!=", "<=", ">="};

	protected boolean isOperator(String str) {
		for (String op : OPERATORS) {
			if (str.equals(op)) {
				return true;
			}
		}

		return false;
	}

	protected void removeSymbol(String key) {
		FitServer.getSymbolMap().remove(key);
	}

	public static void setSymbol(String str, Object val) {
		if (str.equals("")) {
			return;
		}

		StringTokenizer stok = new StringTokenizer(str, " ", false);

		while (stok.hasMoreTokens()) {
			String token = stok.nextToken();

			if (token.startsWith("$") && token.endsWith("$")) {
				if (false == FitServer.getSymbolMap().containsKey(token)) {
					FitServer.setSymbol(str, val);
				} else {
					FitServer.getSymbolMap().put(token, val);
				}
			}
		}
	}

	protected boolean checkCondition(final String val) {
		boolean result = false;

		if (val.equals("")) {
			return false;
		}

		if (true == checkConstant(val)) {
			return true;
		}

		Object symbolObj = null;

		ArrayList<String> tokens = tokenizeByOperators(val);

		StringBuffer buf = new StringBuffer();

		for (String token : tokens) {
			buf.append(token);
			buf.append(' ');
		}

		String spaceAdjustedVal = buf.toString();
		StringTokenizer stok = new StringTokenizer(spaceAdjustedVal, " \t", false);

		while (stok.hasMoreTokens()) {
			String token = stok.nextToken();

			if (FitServer.hasSymbol(token)) {
				symbolObj = FitServer.getSymbol(token);
			} else if (false == isOperator(token)) {
				symbolObj = token;
			}

			if (null != symbolObj) {
				result = checkObjectAndChangeValueType(stok, token, symbolObj);
			} else {
				printError("There are no proper operators or operands.");
			}
		}

		return result;
	}

	private ArrayList<String> tokenizeByOperators(String val) {
		ArrayList<String> tokens = new ArrayList<String>();
		int position = 0;

		while (true) {
			int firstPosition = -1;
			int length = 0;

			for (String operator : OPERATORS) {
				int index = val.indexOf(operator, position);

				if (index != -1 && (firstPosition == -1 || firstPosition <= index)) {
					firstPosition = index;
					length = operator.length();
				}
			}
			if (firstPosition == -1) {
				break;
			}

			String previousToken = val.substring(position, firstPosition).trim();
			if (previousToken.length() > 0) {
				tokens.add(previousToken);
			}

			tokens.add(val.substring(firstPosition, firstPosition + length));
			position = firstPosition + length;
		}

		String lastToken = val.substring(position).trim();
		
		if (lastToken.length() > 0) {
			tokens.add(lastToken);
		}

		return tokens;
	}

	public boolean checkObjectAndChangeValueType(StringTokenizer stok, String token, Object symbolObj) {
		boolean result = false;

		if (isOperable(stok, token)) {
			StringBuffer rightStr = new StringBuffer();

			while (stok.hasMoreTokens()) {
				rightStr.append(replaceSymbol(stok.nextToken()));
				rightStr.append(' ');
			}

			String resultStr = rightStr.toString().trim();

			if (isNumberCompare(resultStr, symbolObj)) {
				result = evaluateNumberConditionOperator(castingNumber(symbolObj), Double.parseDouble(resultStr), token);
			} else {
				String leftStr = symbolObj.toString();
				result = evaluateStringConditionOperator(leftStr, resultStr, token);
			}
		}

		return result;
	}

	protected boolean evaluateNumberConditionOperator(double leftVal, double rightVal, String operator) {
		boolean result = true;

		if (operator.equals("==")) {
			result = isSameDouble(leftVal, rightVal);
		} else if (operator.equals("!=")) {
			result ^= isSameDouble(leftVal, rightVal);
		} else if (operator.equals("<=")) {
			result = isLessThanEqualDouble(leftVal, rightVal);
		} else if (operator.equals(">=")) {
			result = isGreaterThanEqualDouble(leftVal, rightVal);
		} else if (operator.equals("<")) {
			result = isLessThanDouble(leftVal, rightVal);
		} else if (operator.equals(">")) {
			result = isGreaterThanDouble(leftVal, rightVal);
		} else {
			printUnsupportedOperatorError(operator);
			result = false;
		}

		return result;
	}

	public static boolean isGreaterThanDouble(double leftVal, double rightVal) {
		return doubleCompare(leftVal, rightVal) > 0;
	}

	public static boolean isLessThanDouble(double leftVal, double rightVal) {
		return doubleCompare(leftVal, rightVal) < 0;
	}

	public static boolean isGreaterThanEqualDouble(double leftVal, double rightVal) {
		return doubleCompare(leftVal, rightVal) >= 0;
	}

	public static boolean isLessThanEqualDouble(double leftVal, double rightVal) {
		return doubleCompare(leftVal, rightVal) <= 0;
	}

	public static boolean isSameDouble(double leftVal, double rightVal) {
		return doubleCompare(leftVal, rightVal) == 0;
	}

	private static int doubleCompare(double leftVal, double rightVal) {
		int result;

		if (Math.abs(leftVal - rightVal) < DOUBLE_PRECISION) {
			result = 0;
		} else if (leftVal > rightVal) {
			result = 1;
		} else {
			result = -1;
		}

		return result;
	}

	private void printUnsupportedOperatorError(String operator) {
		String errorMessage = "Unsupported operator:" + operator;
		printError(errorMessage);
	}

	public static double castingNumber(Object symbolObj) {
		double result = 0.0;

		if (symbolObj instanceof Number) {
			result = ((Number)symbolObj).doubleValue();
		} else {
			try {
				String resultStr = symbolObj.toString();

				if (FitServer.hasSymbol(resultStr)) {
					resultStr = Fixture.replaceSymbol(resultStr);
				}

				result = Double.parseDouble(resultStr);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public static boolean isNumberCompare(String rightStr, Object symbolObj) {
		boolean isSymbolNumber = false;
		boolean isRightStrNumber = false;

		if (symbolObj instanceof Number) {
			isSymbolNumber = true;
		} else {
			try {
				Double.parseDouble(symbolObj.toString());
				isSymbolNumber = true;
			} catch (NumberFormatException e) {
				isSymbolNumber = false;
			}
		}

		try {
			String resultStr = rightStr;

			if (FitServer.hasSymbol(resultStr)) {
				resultStr = Fixture.replaceSymbol(resultStr);
			}

			Double.parseDouble(resultStr);
			isRightStrNumber = true;
		} catch (NumberFormatException e) {
			isRightStrNumber = false;
		}

		return isSymbolNumber && isRightStrNumber;
	}

	protected boolean evaluateStringConditionOperator(String leftStr, String rightStr, String operator) {
		boolean result = true;

		if (operator.equals("==")) {
			result = leftStr.equals(rightStr);
		} else if (operator.equals("!=")) {
			result ^= leftStr.equals(rightStr);
		} else if (operator.equals("<=")) {
			result = isStringSmallerThanEqual(leftStr, rightStr);
		} else if (operator.equals(">=")) {
			result = isStringGreaterThanEqual(leftStr, rightStr);
		} else if (operator.equals("<")) {
			result = isStringSmallerThan(leftStr, rightStr);
		} else if (operator.equals(">")) {
			result = isStringGreaterThan(leftStr, rightStr);
		} else {
			printUnsupportedOperatorError(operator);
			result = false;
		}

		return result;
	}

	public static boolean isStringGreaterThan(String leftStr, String rightStr) {
		return stringCompare(leftStr, rightStr) < 0;
	}

	public static boolean isStringSmallerThan(String leftStr, String rightStr) {
		return stringCompare(leftStr, rightStr) > 0;
	}

	public static boolean isStringGreaterThanEqual(String leftStr, String rightStr) {
		return stringCompare(leftStr, rightStr) <= 0;
	}

	public static boolean isStringSmallerThanEqual(String leftStr, String rightStr) {
		return stringCompare(leftStr, rightStr) >= 0;
	}

	private static int stringCompare(String leftStr, String rightStr) {
		return leftStr.compareTo(rightStr);
	}

	private boolean isOperable(StringTokenizer stok, String token) {
		return isOperator(token) && stok.hasMoreTokens();
	}

	public boolean checkConstant(String val) {
		if (val.equalsIgnoreCase(TRUE) || val.equalsIgnoreCase(TRUE_ONE)) {
			return true;
		}

		return false;
	}

	protected void addState(String keywords) {
		if (isStartKeyword(keywords)) {
			String keyword = keywords.toUpperCase(Locale.US);
			StringTokenizer stok = new StringTokenizer(keyword, "_", false);
			stok.nextToken();
			String command = stok.nextToken();
			stateList.addLast(NtafFlowState.valueOf(command));
		}
	}

	protected void removeState(String keywords) {
		if (isEndKeyword(keywords)) {
			String keyword = keywords.toUpperCase(Locale.US);
			StringTokenizer stok = new StringTokenizer(keyword, "_", false);
			stok.nextToken();
			String command = stok.nextToken();
			stateList.remove(NtafFlowState.valueOf(command));
		}
	}

	protected LinkedList<NtafFlowState> getStateList() {
		return stateList;
	}

	protected int getStateListSize() {
		return stateList.size();
	}

	public void setInList(String machName) {
		inList.add(machName);
	}

	public static ArrayList<String> getInList() {
		return inList;
	}

	protected boolean isStartKeyword(String keywordnames) {
		String keywordname = keywordnames.toLowerCase(Locale.US).trim();
		String startWord = "(start_)";
		String commandWord = "(loop|elseif|if|else|sequence|paralleliterate|parallel|iterate|timer)";
		Pattern startPattern = Pattern.compile(ALLWORDS + startWord + commandWord + ALLWORDS);
		Matcher startMatcher = startPattern.matcher(keywordname);

		if (startMatcher.find()) {

			if (!startMatcher.group(1).equals("")) {
				return false;
			}

			if (startMatcher.group(4) == null || !startMatcher.group(4).equals("")) {
				return false;
			}

			return true;
		}

		return false;
	}

	protected boolean isEndKeyword(String keywordnames) {
		String keywordname = keywordnames.toLowerCase(Locale.US).trim();
		String endWord = "(end_)";
		String commandWord = "(loop|elseif|if|else|sequence|paralleliterate|parallel|iterate|timer)";
		Pattern endKeywordPattern = Pattern.compile(ALLWORDS + endWord + commandWord + ALLWORDS);
		Matcher endKeywordMatcher = endKeywordPattern.matcher(keywordname);

		if (endKeywordMatcher.find()) {

			if (!endKeywordMatcher.group(1).equals("")) {
				return false;
			}

			if (!endKeywordMatcher.group(4).equals("")) {
				return false;
			}

			return true;
		}

		return false;
	}

	static protected String getKeywords() {
		try {
			keywords = Fixture.label(NtafFixture.parsingXML("FlowFixture", "NoComment"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return keywords;
	}

	static protected boolean isKeyword(String keywordname) {
		return getKeywords().contains(keywordname);
	}

	protected boolean findEndKeyword(String keywordname, String endkeywordname) {
		if (isEndKeyword(endkeywordname) == true && isStartKeyword(keywordname) == true) {
			if (keywordname.subSequence(6, keywordname.length()).equals(
				endkeywordname.subSequence(4, endkeywordname.length())) == true) {
				return true;
			}
		}

		return false;
	}

	protected String getAttribute(String attrKey) {
		String result = attributes.get(attrKey);

		if (null == result) {
			result = "";
		}

		return result;
	}

	protected String getReplaceSymbolAttribute(String attrKey) {
		String result = getAttribute(attrKey);

		if (FitServer.hasSymbol(result.trim())) {
			result = replaceSymbol(result.trim());
		}

		return result.trim();
	}

	protected void removeAttribute(String attrKey) {
		attributes.remove(attrKey);
	}

	protected void setIfCondition(NtafFlowState state, Boolean value, int toRow) {
		HashMap<Integer, NtafFlowState> map = new HashMap<Integer, NtafFlowState>();
		map.put(toRow, state);

		if (state.equals(NtafFlowState.ELSE)) {
			ifConditions.put(map, true);
		} else {
			ifConditions.put(map, value);
		}
	}

	protected HashMap<HashMap<Integer, NtafFlowState>, Boolean> getIfConditions() {
		return ifConditions;
	}

	protected void removeIfConditionAll() {
		ifConditions.clear();
	}

	protected String printLog(String message) {
		String resulMessage = replaceSymbol(message);
		String line = "[LOG] " + resulMessage;
		System.out.println(line);
		return line;
	}

	protected String printError(String message) {
		String resultMessage = replaceSymbol(message);
		String line = "[ERROR] " + resultMessage;
		System.out.println(line);
		return line;
	}

	protected long getTimeout(String duration) {
		long timeout = 0;
		String amount = duration;

		Pattern pattern = Pattern.compile("[a-zA-Z]");
		Matcher matcher = pattern.matcher(duration);

		if (matcher.find()) {
			amount = matcher.replaceAll("");
			amount = amount.trim();

			if (duration.contains("s")) {
				timeout = Long.parseLong(amount) * 1000;
			} else if (duration.contains("m")) {
				timeout = Long.parseLong(amount) * 1000 * 60;
			} else if (duration.contains("h")) {
				timeout = Long.parseLong(amount) * 1000 * 60 * 60;
			} else if (duration.contains("d")) {
				timeout = Long.parseLong(amount) * 1000 * 60 * 60 * 24;
			} else if (duration.contains("w")) {
				timeout = Long.parseLong(amount) * 1000 * 60 * 60 * 24 * 7;
			} else if (duration.contains("y")) {
				timeout = Long.parseLong(amount) * 1000 * 60 * 60 * 24 * 7 * 365;
			} else {
				timeout = Long.parseLong(amount);
			}
		} else {
			timeout = Long.parseLong(amount);
		}

		return timeout;
	}

	public static HashMap<String, String> getAttributes() {
		return attributes;
	}

	Boolean checkConditions(String condition, String operator) {
		Boolean bCondition;
		if (true == condition.contains("&&")) {
			operator = "&&";
		} else if (true == condition.contains("||")) {
			operator = "||";
		}
		
		if (false == operator.isEmpty()) {
			List<String> conditionList = separateConditions(condition, operator);
			List<Boolean> conditionResultList = getCheckConditionResults(conditionList);
			
			bCondition = getFinalConditionResult(conditionResultList, operator);
		} else {
			bCondition = checkCondition(condition);
		}
		return bCondition;
	}
	
	public static List<String> separateConditions(String conditions, String delim) {
		List<String> conditionList = new ArrayList<String>();
		StringTokenizer stringTokenizer = new StringTokenizer(conditions, delim);
		
		while (true == stringTokenizer.hasMoreTokens()) {
			conditionList.add(stringTokenizer.nextToken());
		}
		
		return conditionList;
	}

	public List<Boolean> getCheckConditionResults(List<String> conditionList) {
		List<Boolean> checkConditionResultList = new ArrayList<Boolean>();
		
		for (String condition : conditionList) {
			checkConditionResultList.add(checkCondition(condition));
		}
		
		return checkConditionResultList;
	}

	public static Boolean getFinalConditionResult(List<Boolean> conditionResultList, String operator) {
		Boolean result = conditionResultList.get(0);
		Boolean conditionResult = null;
		
		for (int index=1 ; index < conditionResultList.size(); index++) {
			conditionResult = conditionResultList.get(index);
			
			if (true == operator.equals("&&")) {
				result &= conditionResult;
			} else {
				result |= conditionResult;
			}
		}
		
		return result;
	}
	
}