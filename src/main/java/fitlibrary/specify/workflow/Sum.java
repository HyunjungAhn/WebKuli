/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify.workflow;

public class Sum {
	private int sum = 0;

	public void add(int i) {
		sum  += i;
	}
	public int getSum() {
		return sum;
	}
}
