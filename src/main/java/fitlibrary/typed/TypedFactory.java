/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 3/11/2006
*/

package fitlibrary.typed;

import java.lang.reflect.Method;

public interface TypedFactory {
	TypedObject asTypedObject(Object sut);
	Typed asTyped(Class classType);
	Typed asTyped(Method method);
}
