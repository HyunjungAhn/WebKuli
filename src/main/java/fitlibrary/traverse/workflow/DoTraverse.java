/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow;

import fit.Fixture;
import fit.FixtureLoader;
import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.closure.LookupMethodTarget;
import fitlibrary.closure.MethodTarget;
import fitlibrary.collection.CollectionSetUpTraverse;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.FitLibraryExceptionWithHelp;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.NotRejectedException;
import fitlibrary.exception.method.AmbiguousActionException;
import fitlibrary.exception.method.MissingMethodException;
import fitlibrary.exception.parse.ParseException;
import fitlibrary.exception.table.ExtraCellsException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.parser.Parser;
import fitlibrary.parser.graphic.GraphicParser;
import fitlibrary.parser.graphic.ObjectDotGraphic;
import fitlibrary.suite.FlowControl;
import fitlibrary.suite.InFlowPageRunner;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.CommentTraverse;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.function.CalculateTraverse;
import fitlibrary.traverse.function.ConstraintTraverse;
import fitlibrary.typed.NonGenericTyped;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class DoTraverse extends Traverse implements DoEvaluator {
	private boolean gatherExpectedForGeneration;
	private Object expectedResult = new Boolean(true); // Used for UI code generation
	private CollectionSetUpTraverse setUpTraverse = null; // delegate for setup phase
	private boolean settingUp = true;
	private FlowControl flowControl = new FlowControl() { // Default: do nothing
		public void abandon() {
		}
		public void setStopOnError(boolean stopOnError) {
		}
	};
	
	protected DoTraverse() {
		//
	}
	public DoTraverse(Object sut) {
		super(sut);
	}
	public DoTraverse(TypedObject typedObject) {
		super(typedObject);
	}
	public Object interpret(Table table, TestResults testResults) {
		Object result = null;
		setUp(table,testResults);
    	int size = table.size();
    	for (int rowNo = 1; rowNo < size; rowNo++) {
    		Row row = table.row(rowNo);
    		try {
				result = interpretRow(row,testResults);
				if (result instanceof DoEvaluator) {
					DoEvaluator resultingEvaluator = (DoEvaluator)result;
    				resultingEvaluator.interpretInFlow(new Table(row),testResults);
    				break;
    			} else if (result instanceof Evaluator) {
    				Evaluator resultingEvaluator = (Evaluator)result;
    				resultingEvaluator.interpret(new Table(row),testResults);
    				break;
    			} else if (getAlienTraverseHandler().isAlienTraverse(result)) {
    				getAlienTraverseHandler().doTable(result, new Table(row),testResults);
    				break;
    			}
    		} catch (Exception ex) {
    			row.error(testResults,ex);
    		}
    	}
    	tearDown(table,testResults);
    	return result;
	}
    public Object interpretWholeTable(Table table, TableListener tableListener) {
    	TestResults testResults = tableListener.getTestResults();
		try {
			Object result = interpretRow(table.row(0),testResults);
			if (result instanceof Evaluator) {
				Evaluator resultingEvaluator = (Evaluator)result;
				resultingEvaluator.interpret(table,testResults);
				return result;
			} else if (getAlienTraverseHandler().isAlienTraverse(result)) {
				getAlienTraverseHandler().doTable(result,table,testResults);
			} else // do the rest of the table with this traverse
				return interpretInFlow(table,testResults);
		} catch (Throwable e) {
            table.error(testResults,e);
		}
		return null;
	}
    // @Overridden
    public Object interpretInFlow(Table table, TestResults testResults) {
    	return interpret(table,testResults);
    }
	public Object interpretRow(Row row, TestResults testResults) {
        final Cell cell = row.cell(0);
		if (cell.hasEmbeddedTable()) {
		    setExpectedResult(null);
		    interpretInnerTables(cell.innerTables(),testResults);
		    return null;
		}
		setExpectedResult(new Boolean(true));
		String methodName = row.text(0);
		//Check for Fixture
		Fixture fixture = null;
		 try {
			fixture = FixtureLoader.instance().disgraceThenLoad(methodName);
			if (null != fixture) {
				return fixture;
			}
		} catch (Throwable e1) {
		}

		if (false == methodName.equals("") && 5 != Character.getType(methodName.charAt(0))) {
			methodName = ExtendedCamelCase.camel(methodName);
		}
		
		try {
			DoTraverse switchedSetUp = switchSetUp();
			CalledMethodTarget specialMethod = switchedSetUp.findSpecialMethod(row.text(0));
			checkForAmbiguity(methodName,specialMethod,null);
			try {
				CalledMethodTarget target = switchedSetUp.findMethodByActionName(row,row.size()-1);
				checkForAmbiguity(methodName,specialMethod,target);
				Object result = target.invokeAndWrap(row.rowFrom(1),testResults);
				if (result instanceof Boolean)
					target.color(row,((Boolean)result).booleanValue(),testResults);
				return result;
			} catch (MissingMethodException e) {
				if (specialMethod == null)
					throw e;
				return specialMethod.invoke(new Object[] { row, testResults });
			}
		} catch (IgnoredException ex) {
			//
		} catch (Exception ex) {
			cell.error(testResults, ex);
		}
		return null;
    }
	private void interpretInnerTables(Tables tables, TestResults testResults) {
		new InFlowPageRunner(this,false).run(tables,0,new TableListener(testResults));
	}
	/** Check that the result of the action in the rest of the row matches
	 *  the expected value in the last cell of the row.
	 * @param testResults 
	 * @param evaluator 
	 */
	public void check(final Row row, TestResults testResults) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("DoTraverseCheck");
		CalledMethodTarget target = findMethodFromRow(row,less);
		Cell expectedCell = row.last();
		if (gatherExpectedForGeneration)
			expectedResult = target.getResult(expectedCell,testResults);
		target.invokeAndCheck(row.rowFrom(2),expectedCell,testResults,false);
	}
	public void reject(Row row, TestResults testResults) throws Exception {
		not(row,testResults);
	}
    /** Same as reject()
     * @param testResults 
     */
	public void not(Row row, TestResults testResults) throws Exception {
		Cell notCell = row.cell(0);
		expectedResult = new Boolean(false);
		try {
			Object result = callMethodInRow(row,testResults,false);
		    if (!(result instanceof Boolean))
		        notCell.error(testResults,new NotRejectedException());
		    else if (((Boolean)result).booleanValue())
		        notCell.fail(testResults);
		    else
		        notCell.pass(testResults);
		} catch (IgnoredException e) {
			// No result, so ignore
		} catch (FitLibraryException e) {
			if (e instanceof ParseException)
				notCell.pass(testResults);
			else
				row.error(testResults,e);
		} catch (Exception e) {
		    notCell.pass(testResults);
		}
	}
	/** Add a cell containing the result of the rest of the row.
     *  HTML is not altered, so it can be viewed directly.
     */
	public void show(Row row, TestResults testResults) throws Exception {
		try {
			CalledMethodTarget target = findMethodFromRow(row,2);
		    Object result = target.invoke(row.rowFrom(2),testResults,true);
		    row.addCell(target.getResultString(result));
		} catch (IgnoredException e) {
			// No result, so ignore
		}
	}
	/** Add a cell containing the result of the rest of the row,
     *  shown as a Dot graphic.
	 * @param testResults 
     */
	public void showDot(Row row, TestResults testResults) throws Exception {
		Parser adapter = new GraphicParser(new NonGenericTyped(ObjectDotGraphic.class));
		try {
		    Object result = callMethodInRow(row,testResults, true);
		    row.addCell(adapter.show(new ObjectDotGraphic(result)));
		} catch (IgnoredException e) { // No result, so ignore
		}
	}
	/** Checks that the action in the rest of the row succeeds.
     *  o If a boolean is returned, it must be true.
     *  o For other result types, no exception should be thrown.
	 * @param testResults 
     */
	public void ensure(Row row, TestResults testResults) throws Exception {
		expectedResult = new Boolean(true);
		try {
		    Object result = callMethodInRow(row,testResults, true);
		    row.cell(0).passOrFail(testResults,((Boolean)result).booleanValue());
		} catch (IgnoredException e) {
			// No result, so ignore
		} catch (Exception e) {
		    row.cell(0).fail(testResults);
		}
	}
	/** The rest of the row is ignored. 
     */
	public void note(Row row, TestResults testResults) throws Exception {
		//		Nothing to do
	}
    /** The rest of the table is ignored (and not coloured)
     */
	public CommentTraverse comment(Row row, TestResults testResults) throws Exception {
		return new CommentTraverse();
	}
    /** The rest of the table is ignored (and is coloured as ignored)
     */
	public CommentTraverse ignored(Row row, TestResults testResults) throws Exception {
		return new CommentTraverse(true);
	}
    /** To allow for DoTraverse to be used without writing any fixturing code.
     */
	public void start(Row row, TestResults testResults) throws Exception {
		String className = row.text(1);
		if (row.size() != 2)
		    throw new ExtraCellsException("DoTraverseStart");
		try {
		    setSystemUnderTest(ClassUtility.newInstance(className));
		} catch (Exception e) {
		    throw new FitLibraryExceptionWithHelp("Unknown class: "+className,
		            "UnknownClass.DoTraverseStart");
		}
	}
	/** To allow for a CalculateTraverse to be used for the rest of the table.
     */
	public CalculateTraverse calculate(Row row, TestResults testResults) throws Exception {
		if (row.size() != 1)
		    throw new ExtraCellsException("DoTraverseCalculate");
		CalculateTraverse traverse;
		if (this.getClass() == DoTraverse.class)
			traverse =  new CalculateTraverse(getTypedSystemUnderTest());
		else
			traverse = new CalculateTraverse(this);
		traverse.theSetUpTearDownAlreadyHandled();
		return traverse;
	}
	/** To allow for a ConstraintTraverse to be used for the rest of the table.
     */
	public ConstraintTraverse constraint(Row row, TestResults testResults) throws Exception {
		if (row.size() != 1)
		    throw new ExtraCellsException("DoTraverseConstraint");
		ConstraintTraverse traverse = new ConstraintTraverse(this);
		traverse.theSetUpTearDownAlreadyHandled();
		return traverse;
	}
	/** To allow for a failing ConstraintTraverse to be used for the rest of the table.
     */
	public ConstraintTraverse failingConstraint(Row row, TestResults testResults) throws Exception {
		if (row.size() != 1)
		    throw new ExtraCellsException("DoTraverseConstraint");
		ConstraintTraverse traverse = new ConstraintTraverse(this,false);
		traverse.theSetUpTearDownAlreadyHandled();
		return traverse;
	}
	/** 
	 * To support templates
	 */
	public UseTemplateTraverse useTemplate(Row row, TestResults testResults) throws Exception {
		return new UseTemplateTraverse(row.text(1));
	}
	/** 
	 * To support templates
	 */
	public DefineTemplateTraverse template(Row row, TestResults testResults) throws Exception {
		return new DefineTemplateTraverse();
	}
	/** The rest of the storytest is ignored (but is not coloured as ignored)
	 */
	public void abandonStorytest(Row row, TestResults testResults) {
		flowControl.abandon();
	}
	/**
	 * if (stopOnError) then we don't continue intepreting a table
	 * if there's been a problem
	 */
	public void setStopOnError(boolean stopOnError) {
		flowControl.setStopOnError(stopOnError);
	}
	public void expectedTestResults(Row row, TestResults testResults) throws Exception {
		if (testResults.matches(row.text(1),row.text(3),row.text(5),row.text(7))) {
			testResults.clear();
			row.cell(0).pass(testResults);
		} else {
			String results = testResults.toString();
			testResults.clear();
			row.cell(0).fail(testResults,results);
		}
	}

	public CalledMethodTarget findMethodFromRow(final Row row, int less) throws Exception {
		return findMethodByActionName(row.rowFrom(1), row.size() - less);
	}
	/** Is overridden in subclass SequenceTraverse to process arguments differently
	 */
	public CalledMethodTarget findMethodByActionName(Row row, int allArgs) throws Exception {
		return LookupMethodTarget.findMethodInEverySecondCell(this, row, allArgs);
	}
	private Object callMethodInRow(Row row, TestResults testResults, boolean catchError) throws Exception {
		return findMethodFromRow(row,2).invoke(row.rowFrom(2),testResults,catchError);
	}
	private CalledMethodTarget findSpecialMethod(String name) {
		return LookupMethodTarget.findSpecialMethod(this, name);
	}
	public void setGatherExpectedForGeneration(boolean gatherExpectedForGeneration) {
		this.gatherExpectedForGeneration = gatherExpectedForGeneration;
	}
	public void setExpectedResult(Object expectedResult) {
		this.expectedResult = expectedResult;
	}
	public Object getExpectedResult() {
		return expectedResult;
	}
	public static void checkForAmbiguity(String methodName, CalledMethodTarget specialMethod, MethodTarget target) {
		String methodDetails = "method "+methodName+"()";
		String parseMethodDetails = "method "+methodName+"(Row)";
		if (target != null && specialMethod != null)
			throw new AmbiguousActionException(methodDetails,parseMethodDetails);
	}
	public void setSetUpTraverse(CollectionSetUpTraverse setUpTraverse) {
		this.setUpTraverse = setUpTraverse;
		setUpTraverse.setOuterContext(this);
	}
	public void setSetUpTraverse(Object object) {
		setSetUpTraverse(new CollectionSetUpTraverse(object));
	}
	public void setSettingUp(boolean settingUp) {
		this.settingUp = settingUp;
	}
	public DoTraverse switchSetUp() {
		if (settingUp && setUpTraverse != null)
			return setUpTraverse;
		return this;
	}
	public void finishSettingUp() {
		setSettingUp(false);
	}
	public void doNotTearDownAutomatically() {
		this.canTearDown  = false;
	}
	public void registerFlowControl(FlowControl flowControl) {
		this.flowControl = flowControl;
	}
}
