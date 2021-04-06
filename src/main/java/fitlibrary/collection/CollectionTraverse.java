/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.collection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.closure.ClassMethodTarget;
import fitlibrary.closure.ConstantMethodTarget;
import fitlibrary.closure.MethodTarget;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.method.NoSuchPropertyException;
import fitlibrary.object.DomainObjectSetUpTraverse;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.traverse.Traverse;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.MapElement;
import fitlibrary.utility.TestResults;

public abstract class CollectionTraverse extends Traverse {
	protected boolean[] usedFields;
	protected Collection actuals;
	protected boolean showSurplus = true;
	private Class componentType = null; // Is only set if it is known
	
	protected CollectionTraverse(Object sut) {
		super(sut);
	}
	protected CollectionTraverse(Object sut, Object actuals) {
		super(sut);
		setActualCollection(actuals);
	}
	public void setActualCollection(Object actuals) {
		if (actuals instanceof Collection)
			setActualCollection((Collection)actuals);
		else if (actuals instanceof Iterator)
			setActualCollection((Iterator)actuals);
		else if (actuals instanceof Map)
			setActualCollection((Map)actuals);
		else if (actuals instanceof Object[])
			setActualCollection((Object[])actuals);
		else if (actuals.getClass().isArray()) 
			setActualCollectionAsArray(actuals);
		else 
			throw new RuntimeException("Unable to handle an object of type "+actuals.getClass());
	}
	private void setActualCollectionAsArray(Object actuals) {
		List list = new ArrayList();
		int length = Array.getLength(actuals);
		for (int i = 0; i < length; i++)
			list.add(Array.get(actuals,i));
		setActualCollection(list);
	}
	public void setActualCollection(Collection actuals) {
		this.actuals = actuals;
	}
	public void setActualCollection(Object[] actuals) {
	    setActualCollection(Arrays.asList(actuals));
	}
	public void setActualCollection(Iterator it) {
	    List actualCollection = new ArrayList();
		while (it.hasNext())
			actualCollection.add(it.next());
		setActualCollection(actualCollection);
	}
	public void setActualCollection(Map map) {
		setActualCollection(mapMapToSet(map));
	}
	public static List mapMapToSet(Map map) {
		List elements = new ArrayList();
		Set keySet = map.keySet();
		for (Iterator it = keySet.iterator(); it.hasNext(); ) {
			Object key = it.next();
			elements.add(new MapElement(key,map.get(key)));
		}
		return elements;
	}
    public Object interpret(Table table, TestResults testResults) {
        if (actuals == null)
        	throw new FitLibraryException("Actual list missing");
        Row firstRow = table.row(1);
        try {
            List getters = new ArrayList();
            if (!actuals.isEmpty())
                getters = bindGettersForAllActuals(firstRow,testResults);
            else if (!table.rowExists(2))
                table.row(1).pass(testResults);
            for (int rowNo = 2; rowNo < table.size(); rowNo++) {
                firstRow = table.row(rowNo);
                interpret(firstRow,getters,testResults);
            }
            if (showSurplus)
            	showSurplus(getters,table,testResults);
        } catch (IgnoredException e) {
        	//
        } catch (Exception e) {
            firstRow.error(testResults,e);
        }
        return actuals;
    }
	public void setShowSurplus(boolean showSurplus) {
		this.showSurplus = showSurplus;
	}
	public void setComponentType(Class componentType) {
		this.componentType = componentType;
	}
    /** List<Parser[]> */
    protected final List bindGettersForAllActuals(Row row, TestResults testResults) throws Exception {
    	usedFields = new boolean[row.size()];
        for (int i = 0; i < usedFields.length; i++)
            usedFields[i] = false;
        List bindings = new ArrayList();
        for (Iterator it = actuals.iterator(); it.hasNext(); ) {
			TypedObject typedObject = asTypedObject(it.next());
			bindings.add(bindGettersForOneElement(row, testResults, typedObject));
		}
        for (int i = 0; i < usedFields.length; i++)
            if (!usedFields[i]) {
                String propertyName = camelCase(row.cell(i).text());
                String classNames = ClassUtility.allElementClassNames(actuals);
				row.cell(i).error(testResults,new NoSuchPropertyException(
                		propertyName,classNames));
                throw new IgnoredException();
            }
        return bindings;
    }
	private MethodTarget[] bindGettersForOneElement(Row row, TestResults testResults, TypedObject typedObject) {
		MethodTarget[] columnBindings = new MethodTarget[row.size()];
        for (int i = 0; i < columnBindings.length; i++) {
            Cell cell = row.cell(i);
            if (componentType != null && DomainObjectSetUpTraverse.givesClass(cell)) {
                columnBindings[i] = new ClassMethodTarget(componentType,this,typedObject);
                usedFields[i] = true;
            } else {
                String fieldName = ExtendedCamelCase.camel(cell.text());
                try {
					columnBindings[i] = bindPropertyGetterForTypedObject(fieldName, typedObject);
                    if (columnBindings[i] != null)
                        usedFields[i] = true;
                } catch (NoSuchPropertyException e) {
                    throw new IgnoredException();
                }
            }
        }
        return columnBindings;
	}
    protected CalledMethodTarget bindPropertyGetterForTypedObject(String name, TypedObject typedObject) {
		String mappedName = camelCase(name);
    	if (typedObject.getSubject() instanceof Map) {
    		Object value = ((Map)typedObject.getSubject()).get(mappedName);
    		if (value == null)
    			return null;
    		return new ConstantMethodTarget(value,this);
    	}
		return typedObject.optionallyFindGetterOnTypedObject(name,this);
	}
    protected final boolean matchRow(Row row, MethodTarget[] columnBindings, TestResults testResults) throws Exception {
        boolean matchedAlready = false;
        for (int i = 0; i < columnBindings.length; i++) {
            Cell expectedCell = row.cell(i);
            MethodTarget getter = columnBindings[i];
            if (getter == null)
            	expectedCell.passIfBlank(testResults);
            else {
            	boolean matched = getter.invokeAndCheckCell(expectedCell,matchedAlready,testResults);
            	if (!matchedAlready && !matched)
            		return false;
                matchedAlready = true;
            }
        }
        return true;
    }
    protected void showSurplus(List bindings, Table table, TestResults testResults) {
        if (!bindings.isEmpty())
			addSurplusRows(table,bindings,testResults);
	}
	private static void addSurplusRows(Table table, List surplusBindings,
			TestResults testResults) {
        for (Iterator it = surplusBindings.iterator(); it.hasNext(); ) {
            MethodTarget[] getter = (MethodTarget[])it.next();
			Row row = table.newRow();
			buildSurplusRow(row,getValues(getter),testResults);
            row.cell(0).actualElementMissing(testResults);
        }
    }
    private static void buildSurplusRow(Row row, Object[] values, TestResults testResults) {
        if (values.length == 0) {
        	row.addCell("null",values.length);
        	return;
        }
        for (int i = 0; i < values.length; i++) {
        	Cell addCell = row.addCell("&nbsp;");
            Object value = values[i];
            if (value == null)
            	addCell.ignore(testResults);
            else if (value instanceof Exception)
            	addCell.error(testResults,(Exception)value);
            else
            	addCell.setUnvisitedEscapedText(value.toString());
        }
    }
    private static Object[] getValues(MethodTarget[] getters) {
        Object[] values = new Object[getters.length];
        for (int i = 0; i < getters.length; i++) {
        	MethodTarget getter = getters[i];
            if (getter == null)
                values[i] = null;
            else
                try {
                    values[i] = getter.getResult();
                } catch (Exception e) {
                    values[i] = e;
                }
        }
        return values;
    }

    public abstract void interpret(Row row, List theActuals, TestResults testResults) throws Exception;
}
