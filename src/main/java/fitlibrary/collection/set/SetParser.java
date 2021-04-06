/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.collection.set;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import fit.Fixture;
import fitlibrary.collection.CollectionSetUpTraverse;
import fitlibrary.collection.CollectionTraverse;
import fitlibrary.parser.Parser;
import fitlibrary.parser.lookup.ParserFactory;
import fitlibrary.table.Cell;
import fitlibrary.table.Table;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.FitLibrarySelector;
import fitlibrary.traverse.Traverse;
import fitlibrary.typed.Typed;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;

public class SetParser implements Parser {
	protected final Parser parser, showParser;
	protected final Evaluator evaluator;
    protected final Typed typed;
	
	public static boolean applicableType(Class type) {
		return Set.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
	}
	public SetParser(Evaluator evaluator, Typed typed) {
		this.evaluator = evaluator;
        this.typed = typed;
		parser = Traverse.asTyped(String.class).resultParser(evaluator);
		showParser = Traverse.asTyped(Object.class).resultParser(evaluator);
	}
	public TypedObject parseTyped(Cell cell, TestResults testResults) throws Exception {
		return typed.typedObject(parse(cell,testResults));
	}
	private Object parse(Cell cell, TestResults testResults) throws Exception {
		if (cell.hasEmbeddedTable()) 
			return parseTable(cell.getEmbeddedTable(),testResults);
		return parse(cell.text(),testResults);
	}
	protected Object parseTable(Table table, TestResults testResults) {
		Set set = new HashSet();
		CollectionSetUpTraverse setUp = new CollectionSetUpTraverse(evaluator,set,true);
		setUp.interpretInnerTable(table,evaluator,testResults);
		return set;
	}
	public boolean matches(Cell cell, Object result, TestResults testResults) throws Exception {
		if (result == null)
			return !cell.hasEmbeddedTable() && cell.isBlank();
		if (cell.hasEmbeddedTable())
			return tableMatches(cell.getEmbeddedTable(),result,testResults);
		return parse(cell,testResults).equals(result);
    }
	protected boolean tableMatches(Table table, Object result, TestResults testResults) {
		CollectionTraverse traverse = FitLibrarySelector.selectSet(result);
		return traverse.doesInnerTablePass(table,evaluator,testResults);
	}
	private Object parse(String s, TestResults testResults) throws Exception {
		s = Fixture.replaceSymbol(s);
		if (s.startsWith("[") && s.endsWith("]")) {
			s = s.substring(1, s.length()-1);
		}
		
		StringTokenizer t = new StringTokenizer(s, ",");
        Set set = new HashSet();
		while (t.hasMoreTokens())
			set.add(parser.parseTyped(new Cell(t.nextToken()),testResults).getSubject());
		return set;
	}
	public String show(Object object) throws ArrayIndexOutOfBoundsException, IllegalArgumentException, Exception {
		if (object == null)
			return "";
		String result = "";
        boolean first = true;
        Set set = (Set)object;
		for (Iterator it = set.iterator(); it.hasNext(); ){
			String element = showParser.show(it.next());
            if (first)
                first = false;
            else
                result += ", ";
			result += element;
		}
		return result;
	}
    public static ParserFactory parserFactory() {
    	return new ParserFactory() {
    		public Parser parser(Evaluator evaluator, Typed typed) {
    			return new SetParser(evaluator,typed);
    		}
    	};
    }
	public Evaluator traverse(TypedObject typedObject) {
		return new SetTraverse(typedObject);
	}
}
