/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.exception.table;

import fitlibrary.exception.FitLibraryExceptionWithHelp;

public class MissingCellsException extends FitLibraryExceptionWithHelp {
  public MissingCellsException(String details) {
    super("Missing table cells","MissingCells."+details);
  }
}
