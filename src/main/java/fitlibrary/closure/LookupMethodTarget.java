/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.closure;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import fitlibrary.exception.NoSystemUnderTestException;
import fitlibrary.exception.method.MissingMethodException;
import fitlibrary.table.Row;
import fitlibrary.traverse.DomainAdapter;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.TestResults;

public class LookupMethodTarget {
	public static CalledMethodTarget findSpecialMethod(Evaluator evaluator, String name) {
		if (name.equals(""))
			return null;
		name = ExtendedCamelCase.camel(name);
		Closure findEntityMethod = findFixturingMethod(evaluator, name, new Class[] {Row.class, TestResults.class});
		if (findEntityMethod == null)
			return null;
		return new CalledMethodTarget(findEntityMethod, evaluator);
	}

	public static Closure findFixturingMethod(Evaluator evaluator, String name, Class[] argTypes) {
		Closure method = asTypedObject(evaluator).findPublicMethodClosureForTypedObject(name, argTypes);
		if (method == null && evaluator.getSystemUnderTest() instanceof Evaluator)
			method = findFixturingMethod((Evaluator)evaluator.getSystemUnderTest(), name, argTypes);
		if (method == null && evaluator.getSystemUnderTest() instanceof DomainAdapter)
			method = evaluator.getTypedSystemUnderTest().findPublicMethodClosureForTypedObject(name, argTypes);
		if (method == null && evaluator.getNextOuterContext() != null)
			method = findFixturingMethod(evaluator.getNextOuterContext(), name, argTypes);
		return method;
	}

	private static TypedObject asTypedObject(Object subject) {
		return Traverse.asTypedObject(subject);
	}

	public static CalledMethodTarget findMethodInEverySecondCell(Evaluator evaluator, Row row, int allArgs) {
		int parms = allArgs / 2 + 1;
		int argCount = (allArgs + 1) / 2;
		String name = row.text(0);
		for (int i = 1; i < parms; i++) {
			if (5 != Character.getType(name.charAt(0))) {
				name += " " + row.text(i * 2);
			} else {
				name += "" + row.text(i * 2);
			}
		}

		CalledMethodTarget target = findTheMethodMapped(name, argCount, evaluator);
		target.setEverySecond(true);
		return target;
	}

	public static CalledMethodTarget findTheMethodMapped(String name, int argCount, Evaluator evaluator) {
		//TODO 확인필요 나종채_20100416 : 모바일 프레임워크 연동을 위해 switch 명령어를 사용시에 문제가 될 수 있어 Camel case에 대해 변환 기능을 제거
		if (5 != Character.getType(name.charAt(0))) {
			name = ExtendedCamelCase.camel(name);
		}
		
		return findTheMethod(name, unknownParameterNames(argCount), "TypeOfResult", evaluator);
	}

	private static List unknownParameterNames(int argCount) {
		List methodArgs = new ArrayList();
		for (int i = 0; i < argCount; i++)
			methodArgs.add("arg" + (i + 1));
		return methodArgs;
	}

	public static CalledMethodTarget findTheMethod(String name, List methodArgs, String returnType, Evaluator evaluator) {
		String signature = ClassUtility.methodSignature(name, methodArgs, returnType);
		TypedObject typedObject = asTypedObject(evaluator);
		return typedObject.findSpecificMethodOrPropertyGetter(name, methodArgs.size(), evaluator, signature);
	}

	public static CalledMethodTarget findMethod(String name, List methodArgs, String returnType, Evaluator evaluator) {
		Closure result = asTypedObject(evaluator).findMethodForTypedObject(name, methodArgs.size());
		if (result != null)
			return new CalledMethodTarget(result, evaluator);
		String signature = ClassUtility.methodSignature(name, methodArgs, returnType);
		throw new MissingMethodException(signature, identifiedClassesInOutermostContext(evaluator, true), "");
	}

	public static CalledMethodTarget findSetter(String propertyName, Evaluator evaluator) {
		String methodName = ExtendedCamelCase.camel("set " + propertyName);
		String arg = ExtendedCamelCase.camel(propertyName);
		TypedObject typedSubject = evaluator.getTypedSystemUnderTest();
		if (typedSubject == null)
			throw new NoSystemUnderTestException();
		CalledMethodTarget target = typedSubject.optionallyFindMethodOnTypedObject(methodName, 1, evaluator, true);
		if (target != null)
			return target;
		throw new MissingMethodException("public void " + methodName + "(ArgType " + arg + ") { }", identifiedClassesInSUTChain(typedSubject.getSubject()), "");
	}

	public static CalledMethodTarget findGetterUpContextsToo(TypedObject typedObject, Evaluator evaluator, String propertyName, boolean considerContext) {
		CalledMethodTarget target = typedObject.optionallyFindGetterOnTypedObject(propertyName, evaluator);
		if (considerContext && target == null)
			target = searchForMethodTargetUpOuterContext(propertyName, evaluator.getNextOuterContext(), evaluator);
		if (target != null)
			return target;
		String getMethodName = ExtendedCamelCase.camel("get " + propertyName);
		String signature = "public ResultType " + getMethodName + "() { }";
		throw new MissingMethodException(signature, identifiedClassesInSUTChain(typedObject.getSubject()), "DomainObject");
	}

	private static CalledMethodTarget searchForMethodTargetUpOuterContext(String name, Evaluator outerContext, Evaluator evaluator) {
		if (outerContext == null)
			return null;
		CalledMethodTarget target = null;
		if (outerContext.getSystemUnderTest() != null) {
			TypedObject typedObject = outerContext.getTypedSystemUnderTest();
			target = typedObject.optionallyFindGetterOnTypedObject(name, evaluator);
		}
		if (target == null)
			return searchForMethodTargetUpOuterContext(name, outerContext.getNextOuterContext(), evaluator);
		return target;
	}

	public static String identifiedClassesInSUTChain(Object firstObject) {
		List classes = new ArrayList();
		identifiedClassListInSutChain(firstObject, classes, true);
		return ClassUtility.classList(firstObject.getClass(), classes);
	}

	private static void identifiedClassListInSutChain(Object firstObject, List classes, boolean includeSut) {
		Object object = firstObject;
		while (object instanceof DomainAdapter) {
			object = ((DomainAdapter)object).getSystemUnderTest();
			if (object != null && (includeSut || object instanceof DomainAdapter) && !ClassUtility.aFitLibraryClass(object.getClass()) && !classes.contains(object.getClass()))
				classes.add(object.getClass());
		}
	}

	public static String identifiedClassesInOutermostContext(Object firstObject, boolean includeSut) {
		Object object = firstObject;
		if (firstObject instanceof Evaluator)
			object = ((Evaluator)firstObject).getOutermostContext();
		List classes = new ArrayList();
		identifiedClassListInSutChain(object, classes, includeSut);
		return ClassUtility.classList(firstObject.getClass(), classes);
	}

	public static Class findClassFromFactoryMethod(Evaluator evaluator, Class type, String typeName) throws IllegalAccessException, InvocationTargetException {
		String methodName = "concreteClassOf" + ClassUtility.simpleClassName(type);
		Closure method = findFixturingMethod(evaluator, methodName, new Class[] {String.class});
		if (method == null)
			throw new MissingMethodException("public Class " + methodName + "(String typeName) { }", identifiedClassesInOutermostContext(evaluator, true), "");
		return (Class)method.invoke(new Object[] {typeName});
	}
}
