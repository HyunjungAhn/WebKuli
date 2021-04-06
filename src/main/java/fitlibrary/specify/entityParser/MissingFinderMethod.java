/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 18/08/2006
*/

package fitlibrary.specify.entityParser;

import java.util.HashMap;
import java.util.Map;

import fitlibrary.specify.eg.User;

public class MissingFinderMethod {
	User user;
	Map users = new HashMap();
	
	public boolean addDebt(User user2, double amount) {
		user2.addDebt(amount);
		return true;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
		users.put(user.getName(),user);
	}
}
