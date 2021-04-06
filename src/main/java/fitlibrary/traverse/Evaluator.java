/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse;

import fitlibrary.table.Table;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;

public interface Evaluator extends DomainAdapter {
	Object getOutermostContext();
	Evaluator getNextOuterContext();
	void setOuterContext(Evaluator outerContext);
	Object interpret(Table table, TestResults testResults);
	TypedObject getTypedSystemUnderTest();
}
