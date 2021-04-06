package com.nhncorp.ntaf;

public class NtafSequence extends NtafDoTraverse {
	public NtafSequence(NtafExecuteData data) {
		super();
		this.flowFixture = data.getFlowFixture();
		this.data = data;
		this.data.initializeSequenceAttributes();
	}

	protected Object execute() {
		return runInnerTables();
	}
}
