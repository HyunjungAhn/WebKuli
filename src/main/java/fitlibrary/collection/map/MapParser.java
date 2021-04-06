/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.collection.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import fit.Fixture;
import fitlibrary.exception.parse.InvalidMapString;
import fitlibrary.parser.Parser;
import fitlibrary.parser.lookup.ParserFactory;
import fitlibrary.table.Cell;
import fitlibrary.table.Table;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.typed.NonGenericTyped;
import fitlibrary.typed.Typed;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;

/** We have to assume that the Map is a Map<String,String>
 */
public class MapParser implements Parser {
	protected final Parser parser, showParser;
	protected final Evaluator evaluator;
    protected final Typed typed;
	protected Typed keyTyped = new NonGenericTyped(String.class);
	protected Typed valueTyped = new NonGenericTyped(String.class);

	public static boolean applicableType(Class type) {
		return Map.class.isAssignableFrom(type);
	}
	public MapParser(Evaluator evaluator, Typed typed) {
		this(evaluator,typed,new NonGenericTyped(String.class),
				new NonGenericTyped(String.class));
	}
	public MapParser(Evaluator evaluator, Typed typed, Typed keyTyped, Typed valueTyped) {
		this.evaluator = evaluator;
        this.typed = typed;
		this.keyTyped = keyTyped;
		this.valueTyped = valueTyped;
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
		MapSetUpTraverse setUp = new MapSetUpTraverse(keyTyped,valueTyped);
		setUp.interpretInnerTable(table,evaluator,testResults);
		return setUp.getResults();
	}
	public boolean matches(Cell cell, Object result, TestResults testResults) throws Exception {
		if (result == null)
			return !cell.hasEmbeddedTable() && cell.isBlank();
		Map map = (Map) result;
		if (cell.hasEmbeddedTable())
			return tableMatches(cell.getEmbeddedTable(),map,testResults);
		return parse(cell,testResults).equals(result);
    }
	protected boolean tableMatches(Table table, Map map, TestResults testResults) {
		Traverse traverse = new MapTraverse(map);
		return traverse.doesInnerTablePass(table,evaluator,testResults);
	}
	private Object parse(String s, TestResults testResults) throws Exception {
		s = Fixture.replaceSymbol(s);
		String spliter;
		
		if (s.contains("->")) {
			spliter = "->";
		} else {
			s = s.substring(1, s.length()-1);
			spliter = "=";
		}
		
		StringTokenizer t = new StringTokenizer(s, ",");
        Map map = new HashMap();
		while (t.hasMoreTokens()) {
			String mapString = t.nextToken();
			String[] split = mapString.split(spliter);
			if (split.length != 2)
				throw new InvalidMapString(mapString);
			map.put(parser.parseTyped(new Cell(split[0]),testResults).getSubject(),
					parser.parseTyped(new Cell(split[1]),testResults).getSubject());
		}
		return map;
	}
	public String show(Object object) throws ArrayIndexOutOfBoundsException, IllegalArgumentException, Exception {
		if (object == null)
			return "";
		String result = "";
        boolean first = true;
        Map map = (Map)object;
		for (Iterator it = map.keySet().iterator(); it.hasNext(); ){
			Object key = it.next();
			String element = showParser.show(key)+"->"+map.get(key);
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
    			return new MapParser(evaluator,typed);
    		}
    	};
    }
	public Evaluator traverse(TypedObject typedObject) {
		return new MapTraverse((Map) typedObject.getSubject());
	}
}
