/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.exception;

/**
 * An exception that's ignored as the problem has already been signalled
 * locally in the table.
 */
public class IgnoredException extends RuntimeException {
	private Exception ignoredException = null;

	public IgnoredException(Exception ignoredException) {
		this.ignoredException = ignoredException;
	}
	public IgnoredException() {
		//
	}
	public String toString() {
		return "Ignored: "+ignoredException;
	}
}
