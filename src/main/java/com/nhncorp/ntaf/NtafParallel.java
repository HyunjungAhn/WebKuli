package com.nhncorp.ntaf;

public class NtafParallel extends NtafDoTraverse {
	public NtafParallel(NtafExecuteData data) {
		super();
		this.flowFixture = data.getFlowFixture();
		this.data = data;
		this.data.initializeParallelAttributes();
	}

	protected Object execute() {
		createThreads();

		runThreads();

		return null;
	}

	protected void createThreads() {
		for (tableIndex = data.getFrom(); tableIndex < data.getTo(); tableIndex++) {
			if (isTimedout()) {
				break;
			}

			String keywordname = data.getKeywordText(tableIndex);

			if (NtafInfo.isKeyword(keywordname)) {
				if (isRunnableKeyword(keywordname)) {
					NtafThread thread = new NtafThread(data.getTables(), tableIndex, data.getTableListener(),
						data.getChildState(), flowFixture);
					addThread(thread);

					//Move index to next table group
					tableIndex = ((NtafDoFixture)flowFixture).getEndTableIndex(data.getTables(), tableIndex);
				} else if (true != handleCommand(data.getTables().table(tableIndex).row(0), keywordname, ntafInfo,
					data.getChildState(), data.getTestResults())) {
					break;
				}
			} else {
				NtafThread thread = executeParallelFixture(data.getTables().table(tableIndex), data.getTestResults());
				addThread(thread);
			}
		}
	}
}
