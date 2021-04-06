/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 23/09/2006
*/

package fitlibrary.typed;

import java.lang.reflect.InvocationTargetException;

import fitlibrary.parser.Parser;
import fitlibrary.parser.lookup.ParserSelector;
import fitlibrary.traverse.Evaluator;
import fitlibrary.utility.ClassUtility;

public class NonGenericTyped implements Typed {
	private Class classType;
	private boolean hasMethodOrField;
	private static ParserSelector parserSelector = new ParserSelector();

	public NonGenericTyped(Class classType) {
		this.classType = classType;
	}
	public NonGenericTyped(Class classType, boolean hasMethodOrField) {
		this(classType);
		this.hasMethodOrField = hasMethodOrField;
	}
	public Class asClass() {
		return classType;
	}
	public boolean hasMethodOrField() {
		return hasMethodOrField;
	}
	public Typed getComponentTyped() {
		return new NonGenericTyped(classType.getComponentType());
	}
	public boolean isPrimitive() {
		return classType.isPrimitive();
	}
	public boolean isArray() {
		return classType.isArray();
	}
	public boolean isGeneric() {
		return false;
	}
	public Object newInstance() throws InstantiationException, IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
		return ClassUtility.newInstance(asClass());
	}
	public String getClassName() {
		return asClass().getName();
	}
	public String simpleClassName() {
		return ClassUtility.simpleClassName(asClass());
	}
	public TypedObject typedObject(Object subject) {
		return new NonGenericTypedObject(subject);
	}
	public TypedObject newTypedInstance() throws InstantiationException, IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
		return typedObject(newInstance());
	}
	public Parser parser(Evaluator evaluator) {
		return parserSelector.parserFor(evaluator,this,false);
	}
	public Parser resultParser(Evaluator evaluator) {
		return parserSelector.parserFor(evaluator,this,true);
	}
	public Parser on(Evaluator evaluator, Typed typed, boolean isResult) {
		return parserSelector.parserFor(evaluator,typed,isResult);
	}
	public String toString() {
		return classType.toString();
	}
}
