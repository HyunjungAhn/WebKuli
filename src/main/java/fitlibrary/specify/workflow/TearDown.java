/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify.workflow;

import fitlibrary.traverse.DomainAdapter;

public class TearDown implements DomainAdapter {
	public void anException() {
		throw new RuntimeException();
	}
	public void tearDown() {
		throw new RuntimeException();
	}
	public Object getSystemUnderTest() {
		return null;
	}
}
