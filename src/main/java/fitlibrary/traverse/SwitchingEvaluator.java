/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse;

import fitlibrary.table.Table;
import fitlibrary.utility.TableListener;

public interface SwitchingEvaluator {
    void runTable(Table table, TableListener tableListener);
}
