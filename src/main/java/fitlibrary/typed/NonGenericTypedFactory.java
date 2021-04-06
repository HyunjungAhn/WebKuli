/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 3/11/2006
*/

package fitlibrary.typed;

import java.lang.reflect.Method;

public class NonGenericTypedFactory implements TypedFactory {
	public TypedObject asTypedObject(Object sut) {
		return new NonGenericTypedObject(sut);
	}
	public Typed asTyped(Class classType) {
		return new NonGenericTyped(classType);
	}
	public Typed asTyped(Method method) {
		return new NonGenericTyped(method.getReturnType(),true);
	}
}
