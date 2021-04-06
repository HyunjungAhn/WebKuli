/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package com.nhncorp.ntaf;

/**
 * Exactly the same as NtafDoFixture, except that actions don't have keywords
 * in every second cell.
 */
public class NtafSequenceFixture extends NtafDoFixture {
	private NtafSequenceTraverse sequenceTraverse = new NtafSequenceTraverse(this);

	public NtafSequenceFixture() {
		setTraverse(sequenceTraverse);
	}

	public NtafSequenceFixture(Object sut) {
		this();
		setSystemUnderTest(sut);
	}
}
