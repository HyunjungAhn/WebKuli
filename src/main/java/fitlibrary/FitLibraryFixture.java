/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary;

import fit.FixtureBridge;
import fit.Parse;
import fitlibrary.parser.lookup.ParseDelegation;
import fitlibrary.table.Table;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;

/**
 * An abstract superclass of all the flow-style fixtures.
 * It manages differences between the Fit and FitNesse versions of the FitLibrary,
 * by delegating to an object of class fitlibrary.FitNesseDifference.
 * This class is different in the builds between the two versions, but is
 * needed here to avoid compiletime conflicts. It also has to be created 
 * reflectively, because we can't mention its name here, except in a String.
 */
public abstract class FitLibraryFixture extends FixtureBridge implements Evaluator {
	private Traverse traverse;
	private TypedObject typedObjectUnderTest = Traverse.asTypedObject(null);

	/** Registers a delegate, a class that will
	 * handle parsing of other types of values.
	 */
	protected void registerParseDelegate(Class type, Class parseDelegate) {
		ParseDelegation.registerParseDelegate(type,parseDelegate);
	}
	/** Registers a delegate object that will
	 * handle parsing of other types of values.
	 */
	protected void registerParseDelegate(Class type, Object parseDelegate) {
		ParseDelegation.registerParseDelegate(type,parseDelegate);
	}
    /** Set the systemUnderTest. 
	 *  If an action can't be satisfied by the fixture, the systemUnderTest
	 *  is tried instead. Thus the fixture is an adapter with methods just
	 *  when they're needed.
	 */
	public void setSystemUnderTest(Object sut) {
		typedObjectUnderTest = Traverse.asTypedObject(sut);
	}
	public Object getSystemUnderTest() {
		return typedObjectUnderTest.getSubject();
	}
	public TypedObject getTypedSystemUnderTest() {
		return typedObjectUnderTest;
	}
	public void setOuterContext(Evaluator outerContext) {
		traverse().setOuterContext(outerContext);
	}
	public Evaluator getNextOuterContext() {
		return traverse().getNextOuterContext();
	}
	public Object getOutermostContext() {
		return traverse().getOutermostContext();
	}
    public final Traverse traverse() {
    	return traverse;
    }
    protected void setTraverse(Traverse traverse) {
    	this.traverse = traverse;
    }
    public void doTable(Parse table) {
        try {
            interpret(new Table(table), testResults());
        } catch (Exception e) {
            new Table(table).error(testResults(),e);
        }
    }
    public void doWithin(Table table, Evaluator evaluator, TestResults testResults) {
        setOuterContext(evaluator);
        counts = testResults.getCounts();
        interpret(table,testResults);
    }
	public boolean doEmbeddedTablePasses(Table table, Evaluator evaluator, TestResults testResults) {
		return traverse().doesInnerTablePass(table,evaluator,testResults);
	}
	public TestResults testResults() {
		return new TestResults(counts);
	}
    public Object interpret(Table table, TestResults testResults) {
    	return traverse().interpret(table,testResults);
    }
}
