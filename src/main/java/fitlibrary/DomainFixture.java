/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary;

import fitlibrary.object.DomainTraverse;
import fitlibrary.table.Row;
import fitlibrary.utility.TestResults;

public class DomainFixture extends DoFixture {
    private DomainTraverse domainTraverse = new DomainTraverse(this);
    
    public DomainFixture() {
    	setTraverse(domainTraverse);
    }
    public DomainFixture(Object sut) {
    	this();
    	setSystemUnderTest(sut);
    }
    public void checks(Row row, TestResults testResults) {
    	domainTraverse.setCurrentCheck();
    }
}
