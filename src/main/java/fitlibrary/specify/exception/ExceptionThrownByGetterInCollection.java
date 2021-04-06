/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 19/08/2006
*/

package fitlibrary.specify.exception;

import java.util.ArrayList;
import java.util.List;

public class ExceptionThrownByGetterInCollection {
	public List users() {
		ArrayList list = new ArrayList();
		list.add(new User());
		return list;
	}		
	public List departments() {
		ArrayList list = new ArrayList();
		list.add(new Department());
		return list;
	}
	public static class Department {
		public List getUsers() {
			ArrayList list = new ArrayList();
			list.add(new User());
			return list;
		}		
	}
	public static class User {
		public String getName() {
			throw new ForcedException();
		}
		public String toString() {
			return "A user";
		}
	}
}
