/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package com.nhncorp.ntaf;

import fit.Parse;
import fit.RunningState;
import fitlibrary.SetUpFixture;
import fitlibrary.suite.FlowControl;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

/**
 * An alternative to fit.ActionFixture
 * 
 * @author rick mugridge, july 2003
 * 
 *         See the specifications for examples
 */
public class NtafDoFixture extends AbstractNtafFitLibraryFixture implements NtafDoEvaluator {
	private NtafDoTraverse doTraverse = new NtafDoTraverse(this);
	private NtafInFlowPageRunner inFlow = new NtafInFlowPageRunner(this, false);

	public NtafDoFixture() {
		super.setTraverse(doTraverse);
	}

	public NtafDoFixture(Object sut) {
		this();
		setSystemUnderTest(sut);
	}

	protected void setTraverse(NtafDoTraverse traverse) {
		this.doTraverse = traverse;
		super.setTraverse(traverse);
	}

	public int runNestedTables(Tables tables, int from, TableListener listener, RunningState state) {
		return inFlow.runTables(tables, from, listener, true, state);
	}

	public int getEndTableIndex(Tables tables, int from) {
		return inFlow.getEndTableIndex(tables, from);
	}
	
	public void interpretTables(Parse tables) {
		inFlow.run(new Tables(tables), 0, new TableListener(listener, testResults()));
	}
	
	
	public Object interpret(Table table, TestResults testResults) {
		return ((NtafDoTraverse)traverse()).interpretInFlow(table, testResults);
	}

	/**
	 * if (stopOnError) then we don't continue intepreting a table as there's
	 * been a problem
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
		return doTraverse.interpretInFlow(firstTable, testResults);
	}

	final public Object interpretWholeTable(Table table, TableListener tableListener) {
		return doTraverse.interpretWholeTable(table, tableListener);
	}

	final public Object interpretInnerTables(Tables tables, int fromRow, int toRow, TableListener tableListener,
		RunningState state) {
		return doTraverse.interpretInnerTables(tables, fromRow, toRow, tableListener, state);
	}

	public void setUp(Table firstTable, TestResults testResults) {
		doTraverse.setUp(firstTable, testResults);
	}

	public void tearDown(Table firstTable, TestResults testResults) {
		doTraverse.tearDown(firstTable, testResults);
	}

	// --------- Interpretation ---------------------------------------
	protected void setGatherExpectedForGeneration(boolean expected) {
		doTraverse.setGatherExpectedForGeneration(expected);
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
}
