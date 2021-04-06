/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.exception.table;

import fitlibrary.exception.FitLibraryException;

public class RowWrongWidthException extends FitLibraryException {
    public RowWrongWidthException(int cellCount) {
        super("Row should be "+cellCount+" cells wide");
    }
}
