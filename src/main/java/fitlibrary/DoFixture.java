/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary;

import fit.Parse;
import fitlibrary.suite.FlowControl;
import fitlibrary.suite.InFlowPageRunner;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

/** An alternative to fit.ActionFixture
	@author rick mugridge, july 2003
  * 
  * See the specifications for examples
*/
public class DoFixture extends FitLibraryFixture implements DoEvaluator {
	private DoTraverse doTraverse = new DoTraverse(this);
	private InFlowPageRunner inFlow = new InFlowPageRunner(this,false);
	
	public DoFixture() {
    	setTraverse(doTraverse);
	}
	public DoFixture(Object sut) {
		this();
	    setSystemUnderTest(sut);
	}

    protected void setTraverse(DoTraverse traverse) {
    	this.doTraverse = traverse;
    	super.setTraverse(traverse);
    }
    // Dispatched to from Fixture when a DoFixture is the first fixture in a storytest
    final public void interpretTables(Parse tables) {
		inFlow.run(new Tables(tables),0,new TableListener(listener,testResults()));
    }
	final public Object interpret(Table table, TestResults testResults) {
    	return ((DoTraverse)traverse()).interpretInFlow(table,testResults);
    }
	/**
	 * if (stopOnError) then we don't continue intepreting a table
	 * as there's been a problem
	 */
	public void setStopOnError(boolean stopOnError) {
		inFlow.setStopOnError(stopOnError);
	}
	protected void abandon() {
		inFlow.setAbandon(true);
		doTraverse.abandonStorytest(null, null);
	}
	protected Object getExpectedResult() {
		return doTraverse.getExpectedResult();
	}
	public Object interpretInFlow(Table firstTable, TestResults testResults) {
		return doTraverse.interpretInFlow(firstTable,testResults);
	}
	final public Object interpretWholeTable(Table table, TableListener tableListener) {
		return doTraverse.interpretWholeTable(table,tableListener);
	}
	public void setUp(Table firstTable, TestResults testResults) {
		doTraverse.setUp(firstTable,testResults);
	}
	public void tearDown(Table firstTable, TestResults testResults) {
		doTraverse.tearDown(firstTable,testResults);
	}
	// --------- Interpretation ---------------------------------------
	protected void setGatherExpectedForGeneration(boolean gatherExpectedForGeneration) {
		doTraverse.setGatherExpectedForGeneration(gatherExpectedForGeneration);
	}
	public void setSetUpFixture(SetUpFixture setUpFixture) {
		doTraverse.setSetUpTraverse(setUpFixture.getSetUpTraverse());
	}
	void finishSettingUp() {
		doTraverse.setSettingUp(false);
	}
	public void doNotTearDownAutomatically() {
		doTraverse.doNotTearDownAutomatically();
	}
	public void registerFlowControl(FlowControl flowControl) {
		doTraverse.registerFlowControl(flowControl);
	}

	//Add by Seokmoon Ryoo to support with Import
	public void doTable(Parse table){
		this.interpretTables(table);		
	}

}
