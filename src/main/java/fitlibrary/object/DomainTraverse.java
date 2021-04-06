/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.object;

import fitlibrary.exception.NoSystemUnderTestException;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.traverse.SwitchingEvaluator;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class DomainTraverse extends DoTraverse implements SwitchingEvaluator {
	private DomainInjectionTraverse domainInject = new DomainInjectionTraverse();
	private DomainCheckTraverse domainCheck = new DomainCheckTraverse();
    private SwitchingEvaluator current;
	
	public DomainTraverse(Object sut) {
		super(sut);
		current = domainInject;
    	domainInject.setDomainTraverse(this);
    	domainCheck.setDomainTraverse(this);
    	setSystemUnderTest(sut);
	}
    public void runTable(Table table, TableListener tableListener) {
        super.interpretWholeTable(table,tableListener);
    }
	public void setSystemUnderTest(Object sut) {
        super.setSystemUnderTest(sut);
        if (domainCheck != null)
        	domainCheck.setSystemUnderTest(sut);
        if (domainInject != null)
        	domainInject.setSystemUnderTest(sut);
    }
    public void checks(Row row, TestResults testResults) {
    	setCurrentCheck();
    }
	public Object interpretWholeTable(Table table, TableListener tableListener) {
        if (current == null)
            throw new NoSystemUnderTestException();
        int phaseBreaks = table.phaseBreaks();
		if (phaseBreaks > 0) {
        	for (int i = 0; i < phaseBreaks; i++) {
        		if (current == domainInject)
        			setCurrentAction();
        		else if (current == this)
        			setCurrentCheck();
        		else
        			this.current = domainInject; // wrap around
        	}
        }
        current.runTable(table,tableListener);
        return current;
    }
	public void setCurrentCheck() {
		this.current = domainCheck;
	}
	public void setCurrentAction() {
		this.current = this;
	}
}
