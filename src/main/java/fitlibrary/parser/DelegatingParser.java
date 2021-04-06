/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.parser;

import fitlibrary.exception.parse.BadNumberException;
import fitlibrary.object.DomainObjectCheckTraverse;
import fitlibrary.object.DomainObjectSetUpTraverse;
import fitlibrary.table.Cell;
import fitlibrary.table.Table;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.Typed;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;

public class DelegatingParser implements Parser {
	protected final DelegateParser delegateParser;
	protected final Evaluator evaluator;
	protected final Typed typed;

	public DelegatingParser(DelegateParser delegateParser, Evaluator evaluator, Typed typed) {
		this.delegateParser = delegateParser;
		this.evaluator = evaluator;
		this.typed = typed;
	}
	public TypedObject parseTyped(Cell cell, TestResults testResults) throws Exception {
		return typed.typedObject(parse(cell,testResults));
	}
	private Object parse(Cell cell, TestResults testResults) throws Exception {
		if (cell.hasEmbeddedTable())
			return parseTable(cell.getEmbeddedTable(),testResults);
		try {
			return delegateParser.parse(cell.text());
		} catch (NumberFormatException e) {
			throw new BadNumberException();
		}
	}
    protected Object parseTable(Table embeddedTable, TestResults testResults) throws Exception {
    	Object newInstance = typed.newInstance();
    	DomainObjectSetUpTraverse setUp = new DomainObjectSetUpTraverse(newInstance);
    	setUp.callStartCreatingObjectMethod(newInstance);
		setUp.interpretInnerTable(embeddedTable,evaluator,testResults);
    	setUp.callEndCreatingObjectMethod(newInstance);
		return newInstance;
	}
    public boolean matches(Cell cell, Object result, TestResults testResults) throws Exception {
    	if (cell.hasEmbeddedTable())
    		return matchesTable(cell.getEmbeddedTable(),result,testResults);
        return delegateParser.matches(parse(cell,testResults),result);
    }
	protected boolean matchesTable(Table table, Object result, TestResults testResults) {
		DomainObjectCheckTraverse traverse = new DomainObjectCheckTraverse(result,typed);
		return traverse.doesInnerTablePass(table,evaluator,testResults);
	}
	public String show(Object result) throws Exception {
		return delegateParser.show(result);
	}
    public String toString() {
        return delegateParser.toString();
    }
	public Evaluator traverse(TypedObject object) {
		throw new RuntimeException("No Traverse available");
	}
}
