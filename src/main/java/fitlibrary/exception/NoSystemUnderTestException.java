/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.exception;

public class NoSystemUnderTestException extends FitLibraryException {
    public NoSystemUnderTestException() {
        super("SystemUnderTest needs to be defined.");
    }
}
