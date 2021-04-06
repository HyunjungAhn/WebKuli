package com.nhncorp.ntaf;

import java.util.Map;

import fit.FitServer;
import fit.Parse;

public class NtafParallelIterate extends NtafDoTraverse {
	public NtafParallelIterate(NtafExecuteData data) {
		super();
		this.flowFixture = data.getFlowFixture();
		this.data = data;
		this.bContinue = false;
		this.data.initializeParallelIterateAttributes();
	}

	protected Object execute() {
		Object result = null;

		if (false == checkPreConditions("Paralleliterate:")) {
			return null;
		}

		for (cnt = 0; cnt < data.getIterateList().size(); cnt++) {
			prepareIterateValue();

			createParallelIterateThreads();

			runThreads();

			if (bContinue) {
				bContinue = false;
				continue;
			}
		}

		//remove the value of attributes from the symbol list
		data.teardownAttributesAndSymbols();

		return result;
	}

	
	private void changeTableBody(Parse parse, String keywordname) {
		parse.body = keywordname;
		parse.orgbody = keywordname;
	}

	
	private void createParallelIterateThreads() {
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
					NtafThread thread = new NtafThread(data.getTables(), tableIndex, data.getTableListener(),
						data.getChildState(), flowFixture);
					addThread(thread);

					//Move index to next table group
					tableIndex = ((NtafDoFixture)flowFixture).getEndTableIndex(data.getTables(), tableIndex);
				} else if (runSpecialKeyword(keywordname)) {
					break;
				}
			} else {
				if (true == isSymbolKeyword) {
					changeTableBody(data.getTables().table(tableIndex).parse.parts.parts, keywordname);
				}

				NtafThread thread = executeParallelFixture(data.getTables().table(tableIndex), data.getTestResults());
				addThread(thread);

				if (true == isSymbolKeyword) {
					changeTableBody(data.getTables().table(tableIndex).parse.parts.parts, orgKeywordname);
				}
				
				isSymbolKeyword = false;				
			}
		}
	}
}
