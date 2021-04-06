package com.nhncorp.ntaf;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import fit.FlowState;
import fit.RunningState;

public class NtafTimer extends NtafDoTraverse {
	private long timeout;

	public NtafTimer(NtafExecuteData data) {
		super();
		this.flowFixture = data.getFlowFixture();
		this.data = data;
		this.data.initializeTimerAttributes();
	}

	protected boolean checkPreConditions(String keyWord) {
		boolean result = true;
		
		//attribute "duration" is required. 
		if (data.getStrDuration().equals("")) {
			ntafInfo.printError(keyWord + " DURATION is required!");
			result = false;
		}

		return result;
	}

	protected Object runInnerTables() {

		if (null != flowFixture) {
			RunningState childState = new RunningState();
			NtafThread thread = new NtafThread(data.getTables(), data.getFrom(), data.getTableListener(), childState,
				flowFixture);
			thread.setLatch(new CountDownLatch(1));

			Thread workingThread = new Thread(thread);
			workingThread.start();

			try {
				workingThread.join(timeout);
				
				if (workingThread.isAlive()) {
					childState.setRunState(FlowState.TIMEOUT);
				}
				
				workingThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return flowFixture;
	}

	protected Object execute() {
		//attribute "duration" is required. 
		if (false == checkPreConditions("timer:")) {
			return flowFixture;
		}

		setTimeout();
		runInnerTables();
		data.teardownAttributesAndSymbols();
		return flowFixture;
	}

	protected void setTimeout() {
		//get the timeout time
		timeout = ntafInfo.getTimeout(data.getStrDuration());

		//the timeout to use in milliseconds (must be >= 1)
		if (timeout < 1) {
			throw new IllegalArgumentException("timeout less than 1.");
		}
	}
}
