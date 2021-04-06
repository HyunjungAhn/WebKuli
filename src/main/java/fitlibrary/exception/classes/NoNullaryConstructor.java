/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.exception.classes;

import java.lang.reflect.Modifier;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.utility.ClassUtility;

public class NoNullaryConstructor extends FitLibraryException {
	public NoNullaryConstructor(String className) {
		super("Class has no default constructor: "+ClassUtility.camelClassName(className));
	}

	public NoNullaryConstructor(Class type) {
		super( (Modifier.isAbstract(type.getModifiers())?"Class is abstract: ": "Class has no default constructor: ")
					+type.getName());
	}
}
