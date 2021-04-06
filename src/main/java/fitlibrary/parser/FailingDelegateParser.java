/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.parser;

import fitlibrary.exception.parse.CouldNotParseException;

public class FailingDelegateParser extends DelegateParser {
	public FailingDelegateParser(Class type) {
		super(type);
	}
	public Object parse(String s) throws Exception {
		throw new CouldNotParseException(type,s);
	}
}
