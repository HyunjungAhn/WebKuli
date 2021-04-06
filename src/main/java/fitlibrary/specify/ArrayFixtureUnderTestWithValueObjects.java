/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify;

import fitlibrary.ArrayFixture;

public class ArrayFixtureUnderTestWithValueObjects extends ArrayFixture {
	public ArrayFixtureUnderTestWithValueObjects() throws Exception {
		super(new Object[]{
    			new Pair(1,2),
				new Pair(3,4),
				new Pair(5,6)
				});
	}
	public static class Pair {
		private DoFixtureDelegation.MyClass one, two;
		
		public Pair(int i, int j) {
			this.one = new DoFixtureDelegation.MyClass(i);
			this.two = new DoFixtureDelegation.MyClass(j);
		}
		public DoFixtureDelegation.MyClass getOne() {
			return one;
		}
		public DoFixtureDelegation.MyClass getTwo() {
			return two;
		}
	}
}
