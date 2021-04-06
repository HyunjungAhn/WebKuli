/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 5/09/2006
*/

package fitlibrary.utility;

import java.util.Iterator;

public class CollectionUtility {
	public static boolean equalsIterator(Iterator it, Iterator it2) {
		if (it == it2)
			return true;
		while (it.hasNext() && it2.hasNext()) {
			if (!it.next().equals(it2.next()))
				return false;
		}
		return !it.hasNext() && !it2.hasNext();
	}
}
