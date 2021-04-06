/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify.mapTraverse;

import java.util.HashMap;
import java.util.Map;

public class StringMap {
	public Map getStringMap() {
		HashMap map = new HashMap();
		map.put("a","b");
		map.put("A","B");
		return map;
	}
}
