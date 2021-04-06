package com.nhncorp.ntaf;

import java.util.Map;

import fit.Binding;
import fit.Parse;
import fit.TypeAdapter;

/**
 * @author nhn
 * @version 1.0
 * @created 04-8-2008 PM 5:40:12
 */
public class NtafColumnFixture extends NtafFixture {
	protected Binding columnBindings[];
	protected boolean hasExecuted = false;

	
	
	public NtafColumnFixture() {

	}

	/**
	 * Utility
	 * 
	 * @param heads
	 */
	protected void bind(Parse heads) {
		try {
			Parse bindHead = heads;
			columnBindings = new Binding[heads.size()];

			for (int i = 0; bindHead != null; i++, bindHead = bindHead.more) {
				columnBindings[i] = createBinding(i, bindHead);
			}
		} catch (Throwable throwable) {
			exception(heads, throwable);
		}
	}

	/**
	 * 
	 * @param cell
	 * @param typeAdapter
	 */
	public void check(Parse cell, TypeAdapter typeAdapter) {
		try {
			executeIfNeeded();
		} catch (Exception e) {
			exception(cell, e);
		}
		super.check(cell, typeAdapter);
	}

	/**
	 * 
	 * @param column
	 * @param heads
	 * @exception Throwable
	 */
	protected Binding createBinding(int column, Parse heads) throws Throwable {
		return Binding.create(this, heads.text());
	}

	/**
	 * 
	 * @param cell
	 * @param column
	 */
	public void doCell(Parse cell, int column) {
		try {
			columnBindings[column].doCell(this, cell);
		} catch (Throwable e) {
			exception(cell, e);
		}
	}

	/**
	 * 
	 * @param row
	 */
	public void doRow(Parse row) {
		hasExecuted = false;
		try {
			reset();
			super.doRow(row);

			if (!hasExecuted) {
				execute();
			}
		} catch (Exception e) {
			exception(row.leaf(), e);
		}
	}

	/**
	 * Traversal
	 * 
	 * @param rows
	 */
	public void doRows(Parse rows) {
		bind(rows.parts);
		super.doRows(rows.more);
	}

	/**
	 * 
	 * @exception Exception
	 */
	public void execute() throws Exception {
	}

	/**
	 * 
	 * @exception Exception
	 */
	protected void executeIfNeeded() throws Exception {
		if (!hasExecuted) {
			hasExecuted = true;
			execute();
		}
	}

	/**
	 * 
	 * @exception Exception
	 */
	public void reset() throws Exception {

	}

}