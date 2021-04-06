package fitlibrary.closure;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import fitlibrary.parser.Parser;
import fitlibrary.parser.lookup.ResultParser;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.TypedObject;

public class FieldClosure implements Closure {
	private Field field;
	private TypedObject typedObject;

	public FieldClosure(TypedObject typedObject, Field field) {
		this.typedObject = typedObject;
		this.field = field;
	}
	public Class[] getParameterTypes() {
		return new Class[0];
	}
	public Class getReturnType() {
		return field.getType();
	}
	public Object invoke() throws IllegalAccessException, InvocationTargetException {
		return field.get(typedObject.getSubject());
	}
	public Object invoke(Object[] arguments) throws IllegalAccessException, InvocationTargetException {
		return invoke();
	}
	public TypedObject invokeTyped(Object[] arguments) throws IllegalAccessException, InvocationTargetException {
		return typedObject.asReturnTypedObject(invoke(),field);
	}
	public Parser[] parameterParsers(Evaluator evaluator) {
		return new Parser[0];
	}
	public ResultParser resultParser(Evaluator evaluator) {
		return typedObject.resultParser(evaluator,field);
	}
	public ResultParser specialisedResultParser(ResultParser resultParser, Object result, Evaluator evaluator) {
		if (result == null || result.getClass() == field.getType())
			return resultParser;
		return typedObject.resultParser(evaluator,field,result.getClass());
	}
	public void setTypedSubject(TypedObject typedObject) {
		this.typedObject = typedObject;
	}
	public String toString() {
		return "FieldClosure["+typedObject+","+field+"]";
	}
	public Object getObject() {
		return field;
	}
}
