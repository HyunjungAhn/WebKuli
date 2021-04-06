/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.exception.method;

import fitlibrary.exception.FitLibraryExceptionWithHelp;

public class AmbiguousActionException extends FitLibraryExceptionWithHelp {

	public AmbiguousActionException(String first, String second) {
		super("Ambiguity between "+first+" and "+second,"AmbiguousMethod");
	}

}
