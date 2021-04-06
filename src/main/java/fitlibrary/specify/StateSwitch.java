/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify;

import fitlibrary.DoFixture;
import fitlibrary.SetUpFixture;

public class StateSwitch extends DoFixture {
	private SetUpFixture setUp = new MySetUpFixture(this);
	
	public boolean inDo() {
		return true;
	}
	public boolean inSetUp() {
		return false;
	}
	
	public static class MySetUpFixture extends SetUpFixture {
		public MySetUpFixture(StateSwitch switcher) {
			switcher.setSetUpFixture(this);
		}
		public boolean inSetUp() {
			return true;
		}
		public boolean inDo() {
			return false;
		}
		public boolean startAction() {
			setUpFinished();
			return true;
		}
	}
}
