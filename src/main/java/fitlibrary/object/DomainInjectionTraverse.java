/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.object;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.closure.LookupMethodTarget;
import fitlibrary.exception.IgnoredException;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.traverse.SwitchingEvaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class DomainInjectionTraverse extends Traverse implements SwitchingEvaluator {
	private DomainTraverse domainTraverse = null;

    public DomainInjectionTraverse() {
    	//
	}
	public void setDomainTraverse(DomainTraverse domainTraverse) {
        this.domainTraverse = domainTraverse;
	}
    public void runTable(Table table, TableListener tableListener) {
        if (switchOnActions(table)) {
            domainTraverse.setCurrentAction();
            return;
        }
        if (switchOnChecks(table)) {
            domainTraverse.setCurrentCheck();
            return;
        }
        try {
        	for (int rowNo = 0; rowNo < table.size(); rowNo++)
        		processRow(table.row(rowNo),tableListener.getTestResults());
        } catch (IgnoredException e) {
        	//
        } catch (Exception e) {
        	table.error(tableListener,e);
        }
    }
    private boolean switchOnActions(Table table) {
        return domainTraverse != null && table.size() == 1 && 
                table.row(0).size() == 1 && 
                table.row(0).cell(0).matchesText("actions");
    }
    private boolean switchOnChecks(Table table) {
        return domainTraverse != null && table.size() == 1 && 
                table.row(0).size() == 1 && 
                table.row(0).cell(0).matchesText("checks");
    }
    public void processRow(Row row, TestResults testResults) {
    	for (int i = 0; i < row.size(); i += 2) {
    		Cell cell = row.cell(i);
    		try {
    			CalledMethodTarget target = LookupMethodTarget.findSetter(cell.text(),this);
    			Cell nextCell = row.cell(i+1);
    			try {
    				target.invoke(nextCell,testResults);
    			} catch (IgnoredException e) {
    				//
    			} catch (Exception e) {
    				nextCell.error(testResults,e);
    			}
    		} catch (Exception e) {
    			cell.error(testResults,e);
    		}
    	}
    }
    public Object interpret(Table table, TestResults testResults) {
        table.error(testResults,new RuntimeException("Don't expect to have this called!"));
        return null;
	}
}
