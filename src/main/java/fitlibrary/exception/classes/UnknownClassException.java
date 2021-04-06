/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.exception.classes;

import fitlibrary.exception.FitLibraryException;

public class UnknownClassException extends FitLibraryException {
	public UnknownClassException(String className) {
		super("Unknown class: "+className);
	}

}
