/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.collection.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fitlibrary.exception.table.ExtraCellsException;
import fitlibrary.parser.Parser;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.traverse.Traverse;
import fitlibrary.typed.Typed;
import fitlibrary.utility.TestResults;

public class MapTraverse extends Traverse {
	private Map map;
	protected Parser keyParser, valueParser;

	public MapTraverse(Map map) {
		this.map = map;
	}
	public MapTraverse(Map map, Typed keyTyped, Typed valueTyped) {
		this(map);
		this.keyParser = keyTyped.parser(this);
		this.valueParser = valueTyped.parser(this);
	}
	public Object interpret(Table table, TestResults testResults) {
		if (map.isEmpty()) {
			if (table.size() == 1)
				table.pass(testResults);
			else
				for (int rowNo = 1; rowNo < table.size(); rowNo++)
					table.row(rowNo).missing(testResults);
		} else {
			// Base the parsing on some element of the map
			determineTypes();
			HashMap copiedMap = new HashMap(map);
			for (int rowNo = 1; rowNo < table.size(); rowNo++) {
				interpret(table.row(rowNo), copiedMap, keyParser, valueParser, testResults);
			}
			addSurplusRows(table,copiedMap,keyParser,valueParser, testResults);
		}
		return map;
	}
	protected void determineTypes() {
		if (keyParser != null)
			return;
		Object someKey = map.keySet().iterator().next();
		keyParser = asTyped(someKey).parser(this);
		valueParser = asTyped(map.get(someKey)).parser(this);
	}
	private void interpret(Row row, Map copiedMap, Parser keyParser2, Parser valueParser2, TestResults testResults) {
		try {
			if (row.size() > 2)
				throw new ExtraCellsException("MapTraverse");
			Object key = keyParser2.parseTyped(row.cell(0),testResults).getSubject();
			Object value = copiedMap.get(key);
			if (value == null)
				row.cell(0).expectedElementMissing(testResults);
			else if (valueParser2.matches(row.cell(1),value,testResults))
				row.pass(testResults);
			else
				row.cell(1).fail(testResults,valueParser2.show(value));
			copiedMap.remove(key);
		} catch (Exception e) {
			row.error(testResults, e);
		}
	}
	private static void addSurplusRows(Table table, Map surplus,
			Parser keyParser, Parser valueParser, TestResults testResults) {
        for (Iterator it = surplus.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            Object value = surplus.get(key);
			Row row = table.newRow();
			try {
				row.addCell(keyParser.show(key));
				row.addCell(valueParser.show(value));
				row.cell(0).actualElementMissing(testResults);
			} catch (Exception e) {
				if (row.isEmpty())
					table.error(testResults, e);
				else
					row.cell(0).error(testResults,e);
			}
        }
    }
}
