package com.nhncorp.ntaf;

import java.util.Calendar;

import fit.FitServer;
import fit.Parse;

/**
 * @author nhn
 * @version 1.0
 * @created 04-8-2008 PM 5:40:12
 */
public class FlowFixture extends NtafSequenceFixture {
	public void doTable(Parse table) {
		this.interpretTables(table);
	}
	
	public boolean command_var(String symbol, String expected) {
		if (true == expected.equals(replaceSymbol(symbol))) {
			return true;
		}
		
		return false;
	}
	
	public static void clearSymbols() {
		FitServer.ClearSymbols();
	}

	public void setStartTime(String startTime) {
		FitServer.setSymbol(startTime, Calendar.getInstance().getTimeInMillis());
	}

	public void setDuration(String startTime, String duration) {
		long now = Calendar.getInstance().getTimeInMillis();
		long start = Long.valueOf(startTime);

		FitServer.setSymbol(duration, now - start);
	}
}