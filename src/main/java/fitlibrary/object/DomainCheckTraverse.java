/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.object;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.closure.LookupMethodTarget;
import fitlibrary.exception.NoSystemUnderTestException;
import fitlibrary.exception.method.MissingMethodException;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.traverse.SwitchingEvaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class DomainCheckTraverse extends Traverse implements SwitchingEvaluator {
	private DomainTraverse domainTraverse;
	
	public DomainCheckTraverse() {
		//
	}
    public DomainCheckTraverse(Object sut) {
		super(sut);
	}
    public Object interpret(Table table, TestResults testResults) {
        table.error(testResults,new RuntimeException("Don't expect to have this called!"));
        return null;
    }
	public void setDomainTraverse(DomainTraverse domainTraverse) {
		this.domainTraverse = domainTraverse;
	}
	public void runTable(Table table, TableListener tableListener) {
        if (switchOnExpected(table)) {
            domainTraverse.setCurrentAction();
            return;
        }
        try {
            for (int rowNo = 0; rowNo < table.size(); rowNo++) {
                Row row = table.row(rowNo);
                if (row.text(0).equals("comment"))
                	return;
				processRow(row,tableListener.getTestResults());
            }
        } catch (Exception e) {
            table.error(tableListener,e);
        }
    }
	private void processRow(Row row, TestResults testResults) {
		for (int i = 0; i < row.size(); i += 2) {
			Cell cell = row.cell(i);
			Cell cell2 = row.cell(i+1);
			if (DomainObjectSetUpTraverse.givesClass(cell)) {
				if (getSystemUnderTest() == null)
					throw new NoSystemUnderTestException();
				checkClass(testResults, cell, cell2);
//				ClassMethodTarget target = new ClassMethodTarget(getSystemUnderTest());
//				target.invokeAndCheckCell(cell2,true,testResults);
			} else {
				String name = cell.text();
				try {
					TypedObject typedSystemUnderTest = getTypedSystemUnderTest();
			    	if (typedSystemUnderTest == null)
			    		throw new NoSystemUnderTestException();
					CalledMethodTarget target = typedSystemUnderTest.findGetterOnTypedObject(name,this);
					target.invokeAndCheck(new Row(),cell2,testResults,false);
				} catch (MissingMethodException ex) {
					cell.error(testResults,ex);
				}
			}
		}
	}
	private void checkClass(TestResults testResults, Cell cell, Cell classCell) {
		String typeName = classCell.text();
		try {
			Class sutClass = LookupMethodTarget.findClassFromFactoryMethod(this,getTypedSystemUnderTest().getClassType(),typeName);
			if (getSystemUnderTest().getClass().equals(sutClass))
				classCell.pass(testResults);
			else
				classCell.fail(testResults);
		} catch (Exception e) {
			cell.error(testResults,e);
		}
	}
    private boolean switchOnExpected(Table table) {
        return domainTraverse != null && table.size() == 1 && 
                table.row(0).size() == 1 && 
                table.row(0).cell(0).matchesText("expected");
    }
}
