package com.nhncorp.ntaf;

public class NtafIterate extends NtafDoTraverse {
	public NtafIterate(NtafExecuteData data) {
		super();
		this.flowFixture = data.getFlowFixture();
		this.data = data;
		this.bContinue = false;
		this.data.initializeIterateAttributes();
	}

	protected Object execute() {
		Object result = null;

		if (false == checkPreConditions("Iterate:")) {
			return null;
		}

		for (cnt = 0; cnt < data.getIterateList().size(); cnt++) {
			prepareIterateValue();

			if (isTimedout()) {
				break;
			}

			result = runInnerLoopTables();
		}

		//remove the value of attributes from the symbol list
		data.teardownAttributesAndSymbols();
		
		return result;
	}
}
