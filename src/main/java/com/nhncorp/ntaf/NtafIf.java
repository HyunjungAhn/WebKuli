package com.nhncorp.ntaf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class NtafIf extends NtafDoTraverse {
	private int fromRow;
	private int toRow;

	public NtafIf(NtafExecuteData data, NtafFlowState state, int fromRow, int toRow) {
		super();
		this.flowFixture = data.getFlowFixture();
		this.data = data;
		this.data.initializeIfAttributes(state, fromRow, toRow);
		this.fromRow = fromRow;
		this.toRow = toRow;
	}

	protected boolean checkPreConditions() {
		boolean result;

		//attribute "expr" is required. 
		if (data.getStrCondition().equals("")) {
			ntafInfo.printError("IF: EXPRESSION is required!");
			result = false;
		} else {
			result = true;
		}

		return result;
	}

	protected Object execute() {
		Object result = null;

		if (false == checkPreConditions()) {
			return null;
		}

		if (notSatisfyIfCondition()) {
			return null;
		}

		parent.setInitIfCondition(false);
		result = runInnerTables();
		parent.setInitIfCondition(true);

		return result;
	}

	private Object getNtafIfStateByTableIdx(int tableIndex) {
		Object bIfCondition = null;
		ArrayList<NtafFlowState> array = new ArrayList<NtafFlowState>();

		array.add(NtafFlowState.IF);
		array.add(NtafFlowState.ELSEIF);
		array.add(NtafFlowState.ELSE);

		Iterator<NtafFlowState> iterator = array.iterator();

		while (iterator.hasNext()) {
			NtafFlowState state = iterator.next();

			if (null != checkIfConditionValue(tableIndex, state)) {
				bIfCondition = checkIfConditionValue(tableIndex, state);
				break;
			}
		}

		return bIfCondition;
	}

	private Object getNtafIfStateByIfSyntax(NtafFlowState state) {
		Object bIfCondition = null;

		for (int i = toRow; i > 0; i--) {
			if (null != checkIfConditionValue(i, state)) {
				bIfCondition = checkIfConditionValue(i, state);
				break;
			}
		}

		return bIfCondition;
	}

	private Object checkIfConditionValue(int tableIndex, NtafFlowState state) {
		HashMap<Integer, NtafFlowState> hashMap = new HashMap<Integer, NtafFlowState>();
		hashMap.put(tableIndex - 2, state);

		return ntafInfo.getIfConditions().get(hashMap);
	}

	protected Boolean notSatisfyIfCondition() {
		Boolean result = null;
		Boolean bCondition = null;
		NtafFlowState state = data.getState();
		String condition = data.getStrCondition();
		String operator = "";
		
		bCondition = ntafInfo.checkConditions(condition, operator);
		ntafInfo.setIfCondition(state, bCondition, toRow);

		if (state.equals(NtafFlowState.IF) && false == bCondition) {
			result = true;
		} else if (state.equals(NtafFlowState.ELSEIF)
			&& (false == bCondition || getNtafIfStateByTableIdx(fromRow).equals(true))) {
			result = true;
		} else if (state.equals(NtafFlowState.ELSE) && !isRunElseClause() && isInvalidElseClause()) {
			result = true;
		} else {
			result = false;
		}

		return result;
	}

	private Boolean isRunElseClause() {
		HashMap<HashMap, Boolean> ifConditions = new HashMap<HashMap, Boolean>();
		ifConditions.putAll(ntafInfo.getIfConditions());

		Iterator<HashMap> iterator = ifConditions.keySet().iterator();

		while (iterator.hasNext()) {
			Iterator<Integer> innerIterator = iterator.next().keySet().iterator();

			while (innerIterator.hasNext()) {
				int key = innerIterator.next();

				if (key >= toRow) {
					iterator.remove();
				}
			}
		}

		if (ifConditions.containsValue(true)) {
			return false;
		}

		return true;
	}

	protected Boolean isInvalidElseClause() {
		Boolean result = false;

		if (null != getNtafIfStateByIfSyntax(NtafFlowState.IF)) {
			if (null != getNtafIfStateByIfSyntax(NtafFlowState.ELSEIF)) {
				if ((getNtafIfStateByIfSyntax(NtafFlowState.IF).equals(true))
					|| (getNtafIfStateByIfSyntax(NtafFlowState.ELSEIF).equals(true))) {
					result = true;
				}
			} else {
				if ((getNtafIfStateByIfSyntax(NtafFlowState.IF).equals(true))) {
					result = true;
				}
			}
		} else {
			result = false;
		}

		return result;
	}
}