/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.object;

import java.lang.reflect.InvocationTargetException;

import fitlibrary.closure.Closure;
import fitlibrary.closure.LookupMethodTarget;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.parser.Parser;
import fitlibrary.ref.EntityReference;
import fitlibrary.table.Cell;
import fitlibrary.table.Table;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.Typed;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.TestResults;

public class DomainObjectParser implements Parser {
	public static final String FIND = "find";
	public static final String SHOW = "show";
    private Closure findIntMethod, findStringMethod;
	private Closure showMethod;
    private EntityReference referenceParser;
    private String findExceptionMessage, showExceptionMethod;
    private String shortClassName;
    protected Evaluator evaluator;
	private Typed typed;
	
	public DomainObjectParser(Evaluator evaluator, Typed typed) {
		this.evaluator = evaluator;
		this.typed = typed;
		shortClassName = typed.simpleClassName();
		referenceParser = EntityReference.create(shortClassName.toLowerCase());
		getFindAndShowMethods();
	}
	public TypedObject parseTyped(Cell cell, TestResults testResults) throws Exception {
		return typed.typedObject(parse(cell,testResults));
	}
    private Object parse(Cell cell, TestResults testResults) throws Exception {
    	if (cell.hasEmbeddedTable())
    		return parseTable(cell.getEmbeddedTable(),testResults);
//    	if (cell.text().equals(""))
//    		return null;
		return find(cell.text());
    }
	private Object find(final String text) throws Exception, IllegalAccessException, InvocationTargetException {
		if (findIntMethod != null) {
            int index = 0;
            try {
                index = referenceParser.getIndex(text);
            } catch (FitLibraryException e) {
                return callFindStringMethod(text);
            }
			return findIntMethod.invoke(new Integer[]{ new Integer(index) });
        }
        return callFindStringMethod(text);
	}
	private Object callFindStringMethod(String text) throws Exception {
        if (findStringMethod != null)
            return findStringMethod.invoke(new String[]{ text });
        if ("".equals(text))
        	return null;
        throw new FitLibraryException(findExceptionMessage);
    }
    protected Object parseTable(Table embeddedTable, TestResults testResults) throws Exception {
    	TypedObject newInstance = null;
    	try {
    		newInstance = typed.newTypedInstance();
    	} catch (Exception ex) {
    		// So instead, we'll try creating it from the class that's specified in the table
    	}
    	DomainObjectSetUpTraverse setUp = new DomainObjectSetUpTraverse(newInstance,typed);
    	if (newInstance != null) {
    		setUp.setOuterContext(evaluator);
    		setUp.callStartCreatingObjectMethod(newInstance);
    	}
		setUp.interpretInnerTable(embeddedTable,evaluator,testResults);
		return setUp.getSystemUnderTest();
	}
    public boolean matches(Cell cell, Object result, TestResults testResults) throws Exception {
		if (result == null)
			return !cell.hasEmbeddedTable() && cell.isBlank();
    	if (cell.hasEmbeddedTable())
    		return matchesTable(cell.getEmbeddedTable(),result,testResults);
        return matches(parse(cell,testResults),result);
    }
	protected boolean matchesTable(Table table, Object result, TestResults testResults) {
		DomainObjectCheckTraverse traverse = new DomainObjectCheckTraverse(result,typed);
		return traverse.doesInnerTablePass(table,evaluator,testResults);
	}
	public boolean matches(Object a, Object b) {
		if (a == null)
			return b == null;
		return a.equals(b);
	}
    private void getFindAndShowMethods() {
		final Class[] intArg = { int.class };
		final Class[] stringArg = { String.class };
		final Class[] showArg = { typed.asClass() };
		final String findName = ExtendedCamelCase.camel(FIND+" "+shortClassName);
		final String showMethodName = ExtendedCamelCase.camel(SHOW+" "+shortClassName);
		final String showMethodSignature = showMethodName+"("+shortClassName+" arg) { }";
		String potentialClasses = LookupMethodTarget.identifiedClassesInOutermostContext(evaluator, true);
		
		findExceptionMessage = "EITHER "+shortClassName+
			" is (1) a Value Object. So missing parse method: "+
			"public static "+shortClassName+" parse(String s) { } in class "+typed.getClassName()+
			"; OR (2) an Entity. So missing finder method: "+
			"public "+shortClassName+" find"+shortClassName+"(String key) { } in "+potentialClasses;
		showExceptionMethod = "Missing show method: public String "+showMethodSignature+" in "+potentialClasses;
		
		findIntMethod = LookupMethodTarget.findFixturingMethod(evaluator,findName,intArg);
		findStringMethod = LookupMethodTarget.findFixturingMethod(evaluator,findName,stringArg);
		showMethod = LookupMethodTarget.findFixturingMethod(evaluator,showMethodName,showArg);
	}
	public String show(Object result) throws Exception {
        Object[] args = new Object[]{ result };
		if (showMethod != null)
            return showMethod.invoke(args).toString();
		throw new FitLibraryException(showExceptionMethod);
	}
	public Evaluator traverse(TypedObject typedObject) {
		return new DomainObjectCheckTraverse(typedObject);
	}
	public boolean hasFinder() {
		return findIntMethod != null || findStringMethod != null;
	}
}
