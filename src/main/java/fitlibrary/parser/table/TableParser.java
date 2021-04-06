/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.parser.table;

import fit.Parse;
import fitlibrary.exception.table.MissingTableException;
import fitlibrary.parser.HtmlStructureParser;
import fitlibrary.parser.Parser;
import fitlibrary.parser.lookup.ParserFactory;
import fitlibrary.table.Cell;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.Typed;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;

public class TableParser extends HtmlStructureParser {
	public TableParser(Typed typed) {
		super(typed);
	}
	public static boolean applicableType(Class type) {
		return  fitlibrary.parser.table.TableInterface.class.isAssignableFrom(type);
	}
	protected Object parse(Cell cell, TestResults testResults) throws Exception {
		if (!cell.hasEmbeddedTable())
			throw new MissingTableException();
        Parse parse = cell.getEmbeddedTable().parse();
		Object[] args = new Object[]{parse};
		Class[] argTypes = new Class[]{ Parse.class };
		return callReflectively("parseTable",args,argTypes,null);
    }
	public String show(Object object) {
		if (object == null)
			return "null";
		return callReflectively("toTable",new Object[]{},new Class[]{},object).toString();
	}
	protected boolean areEqual(Object expected, Object actual) {
		if (expected == null)
			return actual == null;
		return Table.equals(expected,actual);
	}
    // Is registered in LibraryTypeAdapter.on()
    public static ParserFactory parserFactory() {
    	return new ParserFactory() {
    		public Parser parser(Evaluator evaluator, Typed typed) {
    			return new TableParser(typed);
    		}
    	};
    }
	public Evaluator traverse(TypedObject typedObject) {
		throw new RuntimeException("No Traverse available");
	}
}
