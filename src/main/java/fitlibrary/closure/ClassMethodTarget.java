/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.closure;

import fitlibrary.table.Cell;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;

public class ClassMethodTarget implements MethodTarget {
	private Class componentType;
	private Evaluator evaluator;
	private Class actualType;

	public ClassMethodTarget(Class componentType, Evaluator evaluator, TypedObject typedObject) {
		this.componentType = componentType;
		this.evaluator = evaluator;
		this.actualType = typedObject.getClassType();
	}
	public void setTypedSubject(TypedObject typedObject) {
	}
	public String getResult() throws Exception {
		return actualType.getName();
	}
	public boolean matches(Cell expectedCell, TestResults testResults) {
		try {
			Class expectedType = LookupMethodTarget.findClassFromFactoryMethod(evaluator,componentType,expectedCell.text());
        	return actualType == expectedType;
		} catch (Exception e) {
			expectedCell.error(testResults, e);
			return false;
		}
	}
	public boolean invokeAndCheckCell(Cell expectedCell, boolean matchedAlready, TestResults testResults) {
		try {
        	if (matches(expectedCell,testResults)) {
        		if (matchedAlready)
        			expectedCell.pass(testResults);
        		return true;
        	} else if (matchedAlready)
        		expectedCell.fail(testResults,getResult());
		} catch (Exception e) {
			expectedCell.error(testResults,e);
		}
		return false;
	}
	public String getMethodClass() {
		return "ClassMethodTarget";
	}
	public Closure getClosure() {
		return null;
	}
}
