package fitlibrary.traverse.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.suite.BatchFitLibrary;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class DefineTemplateTraverse extends Traverse {
	private Tables body;

	public DefineTemplateTraverse() {
	}
	public Object interpret(Table table, TestResults testResults) {
		// Just check the parameter names here. Things happen in the call...
		Row header = table.row(1);
		Set parameterNames = new HashSet();
		for (int c = 0; c < header.size(); c++) {
			Cell parameterCell = header.cell(c);
			String parameterName = parameterCell.text();
			if (parameterNames.contains(parameterName))
				parameterCell.error(testResults, new FitLibraryException("Duplicate parameter names"));
			parameterNames.add(parameterName);
		}
		return null;
	}
	protected DefineTemplateTraverse(Table defTable, int parameterCount) {
		Row header = defTable.row(1);
		if (header.size() != parameterCount)
			throw new FitLibraryException("Mismatch in number of parameters to template");
		Map mapToRef = new HashMap();
		for (int c = 0; c < header.size(); c++)
			mapToRef.put(header.text(c),paramRef(c));
		body = new Tables(ParseUtility.copyParse(defTable.row(2).cell(0).innerTables().parse()));
		macroReplace(body,mapToRef);
	}
	public Tables call(List parameters, TestResults results) {
		Tables copy = new Tables(ParseUtility.copyParse(body.parse()));
		Map mapFromRef = new HashMap();
		for (int i = 0; i < parameters.size(); i++)
			mapFromRef.put(paramRef(i), parameters.get(i));
		macroReplace(copy, mapFromRef);
		new BatchFitLibrary(new TableListener(results)).doTables(copy);
		return copy;
	}
	private void macroReplace(Tables tables, Map mapToRef) {
		List reverseSortOrder = new ArrayList(mapToRef.keySet());
		Collections.sort(reverseSortOrder,new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return ((String)arg1).compareTo((String)arg0);
			}
		});
		for (int t = 0; t < tables.size(); t++) {
			Table table = tables.table(t);
			for (int r = 0 ; r < table.size(); r++) {
				Row row = table.row(r);
				for (int c = 0; c < row.size(); c++) {
					Cell cell = row.cell(c);
					String text = cell.text();
					for (Iterator it = reverseSortOrder.iterator(); it.hasNext(); ) {
						String s = (String) it.next();
						text = text.replaceAll(s,(String)mapToRef.get(s));
					}
					if (!text.equals(cell.text()))
							cell.setText(text);
				}
			}
		}
	}
	private String paramRef(int c) {
		return "%__%"+c+"%__%";
	}
}
