package fitlibrary.closure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fitlibrary.exception.method.WrongTypeForMethodException;
import fitlibrary.parser.Parser;
import fitlibrary.parser.lookup.ResultParser;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.typed.TypedObject;

public class MethodClosure implements Closure {
	private TypedObject typedObject;
	private Method method;

	public MethodClosure(TypedObject typedObject, Method method) {
		if (typedObject == null || typedObject.getSubject() == null || method == null)
			throw new RuntimeException("MethodClosure requires non-null args");
		this.typedObject = typedObject;
		this.method = method;
	}
	public TypedObject invokeTyped(Object[] args) throws IllegalAccessException, InvocationTargetException {
		return typedObject.asReturnTypedObject(invoke(args),method);
	}
    public Object invoke() throws IllegalAccessException, InvocationTargetException {
		return invoke(new Object[]{});
	}
    public Object invoke(Object[] args) throws IllegalAccessException, InvocationTargetException {
        try {
            return method.invoke(typedObject.getSubject(),args);
        } catch(IllegalArgumentException e) {
            Class[] parameterTypes = method.getParameterTypes();
            String expectedType = "(";
            for (int i = 0; i < parameterTypes.length; i++)
                expectedType += parameterTypes[i].getName()+", ";
            expectedType = expectedType.substring(0,expectedType.length()-2);
            expectedType += ")";
            String actualType = "(";
            for (int i = 0; i < args.length; i++) {
            	if (args[i] == null)
            		actualType += "null, ";
            	else
            		actualType += args[i].getClass().getName()+", ";
            }
            if (args.length > 0)
            	actualType = actualType.substring(0,actualType.length()-2);
            actualType += ")";
            throw new WrongTypeForMethodException(method,expectedType,actualType);
        }
    }
    public void setSubject(Object subject) {
    	this.typedObject = Traverse.asTypedObject(subject);
    }
    public void setTypedSubject(TypedObject typedObject) {
    	this.typedObject = typedObject;
    }
	public String toString() {
		return "MethodClosure["+typedObject+","+method+"]";
	}
	public ResultParser resultParser(Evaluator evaluator) {
		return typedObject.resultParser(evaluator,method);
	}
	public Parser[] parameterParsers(Evaluator evaluator) {
		return typedObject.parameterParsers(evaluator,method);
	}
	public ResultParser specialisedResultParser(ResultParser resultParser, Object result, Evaluator evaluator) {
		if (result == null || result.getClass() == method.getReturnType())
			return resultParser;
		return typedObject.resultParser(evaluator,method,result.getClass());
	}
	public Class getReturnType() {
		return method.getReturnType();
	}
	public Class[] getParameterTypes() {
		return method.getParameterTypes();
	}
	public Object getObject() {
		return method;
	}

}
