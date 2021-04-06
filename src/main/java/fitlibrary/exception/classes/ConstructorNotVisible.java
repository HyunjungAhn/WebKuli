/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.exception.classes;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.utility.ClassUtility;

public class ConstructorNotVisible extends FitLibraryException {
	public ConstructorNotVisible(String className) {
		super("Constructor for class is not visible: "+ClassUtility.camelClassName(className));
	}
}
