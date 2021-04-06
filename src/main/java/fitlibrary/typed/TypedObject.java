/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 21/10/2006
*/

package fitlibrary.typed;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.closure.Closure;
import fitlibrary.parser.Parser;
import fitlibrary.parser.lookup.ResultParser;
import fitlibrary.traverse.Evaluator;

public interface TypedObject {
	public Object getSubject();
	public Closure findPublicMethodClosureForTypedObject(String name, Class[] args);
	public Closure findMethodForTypedObject(String name, int argCount);
	public CalledMethodTarget optionallyFindMethodOnTypedObject(String name, int argCount, Evaluator evaluator, boolean includeSut);
	public CalledMethodTarget optionallyFindGetterOnTypedObject(String propertyName, Evaluator evaluator);
	public CalledMethodTarget findGetterOnTypedObject(String propertyName, Evaluator evaluator);
	public CalledMethodTarget findSpecificMethodOrPropertyGetter(String name, int argCount, Evaluator evaluator, String signature);
	public Class getClassType();
	public Closure findMethodClosure(String name, int argCount, boolean includeSut);
	public Parser[] parameterParsers(Evaluator evaluator, Method method);
	public ResultParser resultParser(Evaluator evaluator, Method method);
	public ResultParser resultParser(Evaluator evaluator, Field field);
	public ResultParser resultParser(Evaluator evaluator, Method method, Class actualResultType);
	public ResultParser resultParser(Evaluator evaluator, Field field, Class class1);
	public TypedObject asReturnTypedObject(Object object, Method method);
	public TypedObject asReturnTypedObject(Object object, Field field);
	public Evaluator traverse(Evaluator evaluator);
}
