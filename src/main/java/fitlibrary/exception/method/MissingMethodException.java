/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.exception.method;

import fitlibrary.exception.FitLibraryExceptionWithHelp;

public class MissingMethodException extends FitLibraryExceptionWithHelp {
	public MissingMethodException(String signature, String classes, String link) {
		super("Missing method: "+signature+" in "+classes,link);
	}
}
