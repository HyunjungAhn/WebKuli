/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary;

import fitlibrary.collection.CollectionSetUpTraverse;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.DoTraverse;

public class SetUpFixture extends DoFixture {
    private CollectionSetUpTraverse setUpTraverse = new CollectionSetUpTraverse(this);

	public SetUpFixture() {
	    super();
    	setTraverse(setUpTraverse);
	}	
	public SetUpFixture(Object sut) {
		this();
		setSystemUnderTest(sut);
	}
	// The following is just used in specification storytests
    protected void setUpFinished() {
    	Evaluator outer = this.getNextOuterContext();
    	if (outer == null)
    		throw new FitLibraryException("SetUp unable to finish as no outer context.");
    	if (outer instanceof DoFixture)
    		((DoFixture)outer).finishSettingUp();
    	else if (outer instanceof DoTraverse)
    		((DoTraverse)outer).finishSettingUp();
    	else
    		throw new FitLibraryException("SetUp unable to finish as outer context is not DoEmu.");
    	
    }
	public CollectionSetUpTraverse getSetUpTraverse() {
		return setUpTraverse;
	}
}

