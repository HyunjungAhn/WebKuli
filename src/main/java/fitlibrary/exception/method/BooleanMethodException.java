/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.exception.method;

import fitlibrary.exception.FitLibraryExceptionWithHelp;

public class BooleanMethodException extends FitLibraryExceptionWithHelp {
    public BooleanMethodException(String name) {
        super("Method "+name+" does not return a boolean.","BooleanConstraintMethod");
    }
}
