package com.nhncorp.ntaf;

import java.util.concurrent.CountDownLatch;

import fit.Fixture;
import fit.RunningState;

import fitlibrary.traverse.AlienTraverseHandler;
import fitlibrary.table.Tables;
import fitlibrary.table.Table;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class NtafThread implements Runnable {
	protected AlienTraverseHandler traverseHandler;
	protected Table table;
	protected TestResults testResults;
	protected Object fixture;
	private CountDownLatch doneLatch = null;

	private int runType;

	//This is for table group
	protected Tables tables;
	protected TableListener tableListener;
	protected RunningState childState;
	protected Fixture parentFixture;
	protected int from;

	NtafThread(AlienTraverseHandler handler, Object fixture, Table table, TestResults testResults) {
		this.table = table;
		this.traverseHandler = handler;
		this.testResults = testResults;
		this.fixture = fixture;

		runType = 1;
	}

	NtafThread(Tables tables, int from, TableListener tableListener, RunningState childState, Fixture parentFixture) {
		this.tables = tables;
		this.tableListener = tableListener;
		this.from = from;
		this.childState = childState;
		this.parentFixture = parentFixture;

		runType = 2;
	}

	public void run() {
		if (null != fixture && 1 == runType) {
			traverseHandler.doTable(fixture, table, testResults);
		} else if (null != parentFixture && 2 == runType && parentFixture instanceof NtafDoFixture) {
			synchronized (childState) {
				((NtafDoFixture)parentFixture).runNestedTables(tables, from, tableListener, childState);	
			}
		}
		if (null != doneLatch) {
			doneLatch.countDown();
		}
	}

	public void setLatch(CountDownLatch doneLatch) {
		this.doneLatch = doneLatch;
	}
}
