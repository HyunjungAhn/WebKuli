/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.parser.lookup;

import java.beans.PropertyEditor;

import fit.Fixture;
import fitlibrary.exception.parse.BadNumberException;
import fitlibrary.object.DomainObjectCheckTraverse;
import fitlibrary.object.DomainObjectSetUpTraverse;
import fitlibrary.parser.Parser;
import fitlibrary.table.Cell;
import fitlibrary.table.Table;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.Typed;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;

public class PrimitiveParser implements Parser {
	private PropertyEditor editor;
	private Evaluator evaluator;
	private Typed typed;
	private boolean nullOK;

	public PrimitiveParser(Evaluator evaluator, Typed typed, PropertyEditor editor, boolean nullOK) {
		this.evaluator = evaluator;
		this.typed = typed;
		this.editor = editor;
		this.nullOK = nullOK;
	}

	public TypedObject parseTyped(Cell cell, TestResults testResults) throws Exception {
		return typed.typedObject(parse(cell, testResults));
	}

	private Object parse(Cell cell, TestResults testResults) throws Exception {
		if (cell.hasEmbeddedTable())
			return parseTable(cell.getEmbeddedTable(), testResults);
		if (nullOK && cell.isBlank()) {
			return null;
		}
		try {
			return parse(cell.text());
		} catch (NumberFormatException e) {
			throw new BadNumberException();
		}
	}

	private Object parse(String text) throws Exception {
		String symbol = Fixture.replaceSymbol(text);
		if (null != symbol) {
			text = symbol;
		}

		editor.setAsText(text);
		return editor.getValue();
	}

	private Object parseTable(Table embeddedTable, TestResults testResults) throws Exception {
		Object newInstance = typed.newInstance();
		DomainObjectSetUpTraverse setUp = new DomainObjectSetUpTraverse(newInstance);
		setUp.callStartCreatingObjectMethod(newInstance);
		setUp.interpretInnerTable(embeddedTable, evaluator, testResults);
		setUp.callEndCreatingObjectMethod(newInstance);
		return newInstance;
	}

	public boolean matches(Cell cell, Object result, TestResults testResults) throws Exception {
		if (cell.hasEmbeddedTable())
			return matchesTable(cell.getEmbeddedTable(), result, testResults);
		return matches(parse(cell, testResults), result);
	}

	private boolean matches(Object a, Object b) {
		if (a == null)
			return b == null;
		return a.equals(b);
	}

	private boolean matchesTable(Table table, Object result, TestResults testResults) {
		DomainObjectCheckTraverse traverse = new DomainObjectCheckTraverse(result, typed);
		return traverse.doesInnerTablePass(table, evaluator, testResults);
	}

	public String show(Object object) {
		if (object == null)
			return "null";
		return object.toString();
	}

	public String toString() {
		return "PropertyAdapter[" + editor.toString() + "]";
	}

	public Evaluator traverse(TypedObject typedObject) {
		throw new RuntimeException("No Traverse available");
	}
}
