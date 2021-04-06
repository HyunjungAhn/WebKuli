package com.nhncorp.ntaf;

import fit.FitServer;

/* Loop: Run a Task Repeatedly
 * 
 * The loop element contains a single task element which may be executed a specified number of times, 
 * allowing specification of an upper and lower bound with an increment value and where the index 
 * counter is available to the contained task element. In addition, specification of a while and/or 
 * until expression is allowed. If no constraint attributes (e.g. to, until, or while) are specified 
 * for the loop element, then it loops "forever". The loop element has the following attributes:
 * 
 * 		var - is the name of the variable which will contain the loop index variable. It is optional.
 * 		from - is the starting value of the loop index variable. It defaults to 1 if not specified.
 * 		to - is the maximum value of the loop index variable. It is optional.
 * 		by - is the increment value for the loop index variable. It defaults to 1 if not specified.
 * 		while - is an expression that must evaluate to a boolean value and is performed at the top 
 *      		of each loop. If it evaluates to false, it breaks out of the loop. It is optional.
 * 		until - is an expression that must evaluate to a boolean value and is performed at the bottom 
 * 				of each loop. If it evaluates to true, it breaks out of the loop. It is optional.
 */

public class NtafLoop extends NtafDoTraverse {
	public NtafLoop(NtafExecuteData data) {
		super();
		this.flowFixture = data.getFlowFixture();
		this.data = data;
		this.bContinue = false;
		this.data.initializeLoopAttributes();
	}

	protected boolean checkBreakCondition() {
		boolean result;

		if (isTimedout() || false == checkWhileCondition()) {
			result = true;
		} else {
			result = false;
		}

		data.adjustLoopToValue();

		return result;
	}

	protected Object execute() {
		Object result = null;

		for (cnt = data.getLoopFrom(); cnt <= data.getLoopTo(); cnt += data.getIncrementBy()) {
			prepareLoopValue();

			if (checkBreakCondition()) {
				break;
			}

			if (checkUntilCondition()) {
				break;
			}

			result = runInnerLoopTables();

			if (cnt + data.getIncrementBy() > data.getLoopTo()) {
				break;
			}

			if (!data.getStrVar().equals("")) {
				cnt = (int)Double.parseDouble((FlowFixture.replaceSymbol(data.getStrVar())));
			}
		}

		data.teardownAttributesAndSymbols();

		return result;
	}

	protected void prepareLoopValue() {
		if (false == data.getStrVar().equals("")) {
			FitServer.setSymbol(data.getStrVar(), cnt);
		}
		setLoopToValue(cnt);
	}
	
	protected void setLoopToValue(int cnt) {
		if (data.getStrTo().equals("")) {
			data.setLoopTo(cnt + data.getIncrementBy());
		}
	}

	protected boolean checkUntilCondition() {
		boolean bUntil;

		if (!data.getStrUntil().equals("")) {
			bUntil = ntafInfo.checkCondition(data.getStrUntil());
		} else {
			bUntil = false;
		}

		return bUntil;
	}

	protected boolean checkWhileCondition() {
		boolean bWhile;

		if (data.getStrWhile().equals("")) {
			bWhile = true;
		} else {
			bWhile = ntafInfo.checkCondition(data.getStrWhile());
		}

		return bWhile;
	}
}
