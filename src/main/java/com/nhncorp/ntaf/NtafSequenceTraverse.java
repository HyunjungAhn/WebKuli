/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package com.nhncorp.ntaf;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.closure.LookupMethodTarget;
import fitlibrary.table.Row;

public class NtafSequenceTraverse extends NtafDoTraverse {
	public NtafSequenceTraverse(Object sut) {
		super(sut);
	}

	public CalledMethodTarget findMethodByActionName(Row row, int allArgs) throws Exception {
		return LookupMethodTarget.findTheMethodMapped(row.text(0), allArgs, this);
	}
}
