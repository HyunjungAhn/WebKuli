/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package com.nhncorp.ntaf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.closure.LookupMethodTarget;
import fitlibrary.exception.method.VoidMethodException;
import fitlibrary.exception.table.RowWrongWidthException;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.traverse.Evaluator;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.TestResults;

/**
 * Used to be called SetUpTraverse
 * A traverse for entering data for setup (or anything else).
 * Serves a similar purpose to Michael Feather's RowEntryFixture
 * It operates the same as CalculateTraverse, except that there is no empty column
 * and thus no expected columns.
 * It calls setUp() before a call of the method for each row.
 * It calls tearDown() afterwards.
 */
public class NtafCollectionSetUpTraverse extends NtafDoTraverse {
	protected CalledMethodTarget calledTarget;
	protected int argCount = -1;
	protected boolean boundOK = false;
	protected Collection collection = new ArrayList();
	protected boolean embedded = false;

	public NtafCollectionSetUpTraverse() {
		//
	}

	public NtafCollectionSetUpTraverse(Collection collection) {
		this.collection = collection;
	}

	public NtafCollectionSetUpTraverse(Object sut, Collection collection, boolean embedded) {
		super(sut);
		this.collection = collection;
		this.embedded = embedded;
	}

	public NtafCollectionSetUpTraverse(Object sut) {
		super(sut);
	}

	public Object interpret(Table table, TestResults testResults) {
		bindFirstRowToTarget(table.row(1), testResults, this);

		for (int i = 2; i < table.size(); i++) {
			processRow(table.row(i), testResults);
		}

		return collection;
	}

	//@Overrides
	public Object interpretInFlow(Table table, TestResults testResults) {
		setUp(table, testResults);
		try {
			interpret(table, testResults);
		} catch (Exception e) {
			int rowNo = 0;

			if (embedded) {
				rowNo = 1;
			}

			table.row(rowNo).error(testResults, e);
		}
		tearDown(table, testResults);
		
		return collection;
	}

	public void bindFirstRowToTarget(Row row, TestResults testResults, Evaluator evaluator) {
		try {
			argCount = row.size();
			calledTarget = findMethodTarget(row, evaluator, embedded);
			boundOK = true;
		} catch (Exception e) {
			row.error(testResults, e);
		}
	}

	private static CalledMethodTarget findMethodTarget(Row row, Evaluator evaluator, boolean embedded) {
		List arguments = new ArrayList();
		String argNames = buildArguments(row, arguments);
		String methodName = ExtendedCamelCase.camel(argNames);
		CalledMethodTarget findMethod = LookupMethodTarget.findMethod(methodName, arguments, "ResultType", evaluator);

		if (findMethod.returnsVoid() && embedded) {
			throw new VoidMethodException(methodName, "SetUpTraverse");
		}

		return findMethod;
	}

	private static String buildArguments(Row row, List arguments) {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < row.size(); i++) {
			String name = row.text(i);
			buf.append(' ');
			buf.append(name);
			arguments.add(ExtendedCamelCase.camel(name));
		}
		
		return buf.toString();
	}

	public void processRow(Row row, TestResults testResults) {
		if (!boundOK) {
			row.ignore(testResults);
			return;
		}
		if (row.size() != argCount) {
			row.error(testResults, new RowWrongWidthException(argCount));
			return;
		}
		try {
			invokeMethod(row, testResults);
		} catch (Exception e) {
			row.error(testResults, e);
		}
	}

	public Object invokeMethod(Row row, TestResults testResults) throws Exception {
		Object result = target().invoke(row, testResults, true);
		collection.add(result);
		return result;
	}

	public CalledMethodTarget target() {
		return calledTarget;
	}

	public Collection getCollection() {
		return collection;
	}

	public static boolean hasObjectFactoryMethodFor(Table table, Evaluator evaluator) {
		try {
			findMethodTarget(table.row(0), evaluator, false);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
