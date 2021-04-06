package fitlibrary.closure;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import fitlibrary.exception.method.AmbiguousNameException;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ClassUtility;

public class LookupClosure {
	private static Map mapMethodSignatureToMethod = new HashMap(5000);
	private static final Object NOT_FOUND = "";

	public static Closure findMethodClosure(TypedObject typedObject, String methodName, int argCount) {
		Method chosenMethod = findMethod(typedObject.getSubject().getClass(), methodName, argCount);
		if (chosenMethod == null && aGetter(methodName, argCount))
			return findField(typedObject,extractFieldName(methodName));
		if (chosenMethod == null)
			return null;
		return new MethodClosure(typedObject,chosenMethod);
	}
	private static Closure findField(TypedObject typedObject, String fieldName) {
		try {
			Class type = typedObject.getSubject().getClass();
			return new FieldClosure(typedObject,type.getField(fieldName));
		} catch (Exception e) {
			return findPrivateField(typedObject,fieldName);
		}
	}
	private static Closure findPrivateField(TypedObject typedObject, String fieldName) {
		Class type = typedObject.getSubject().getClass();
		Field[] declaredFields = type.getDeclaredFields();
		for (int i = 0; i < declaredFields.length; i++) {
			Field field = declaredFields[i];
			if (fieldName.equals(field.getName())) {
				field.setAccessible(true);
				return new FieldClosure(typedObject,field);
			}
		}
		return null;
	}
	public static Closure findPublicMethodClosure(TypedObject typedObject, String name, Class[] argTypes) {
		Object subject = typedObject.getSubject();
		if (subject == null)
			return null;
		try {
			return new MethodClosure(typedObject,subject.getClass().getMethod(name,argTypes));
        } catch (Exception e) {
            return null;
        }
	}
	private static Method findMethod(Class type, String name, int argCount) {
		MethodSignature methodSignature = new MethodSignature(type,name,argCount);
		Object result = mapMethodSignatureToMethod.get(methodSignature);
		if (result != null) {
			if (result == NOT_FOUND)
				return null;
			return (Method) result;
		}
		Method[] methods = type.getMethods();
		Method chosenMethod = null;
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (name.equals(method.getName()) &&
		            method.getParameterTypes().length == argCount && 
		            !ClassUtility.fitLibrarySystemMethod(method))
				if (chosenMethod == null)
					chosenMethod = method;
				else
					throw new AmbiguousNameException(name);
		}
		if (chosenMethod == null && (aGetter(name, argCount) || aSetter(name, argCount)))
			chosenMethod = findPrivateMethod(type,name,argCount);
		if (chosenMethod == null)
			mapMethodSignatureToMethod.put(methodSignature, NOT_FOUND);
		else
			mapMethodSignatureToMethod.put(methodSignature, chosenMethod);
		return chosenMethod;
	}
	private static String extractFieldName(String methodName) {
		String fieldName = "";
		if (methodName.startsWith("is"))
			fieldName = methodName.substring(2);
		else
			fieldName = methodName.substring(3);
		fieldName = Character.toLowerCase(fieldName.charAt(0))+fieldName.substring(1);
		return fieldName;
	}
	private static boolean aGetter(String name, int argCount) {
		boolean getter = name.startsWith("get") && name.length() > 3 && isUpper(name.charAt(3));
		boolean isa = name.startsWith("is") && name.length() > 2 && isUpper(name.charAt(2));
		return argCount == 0 && (getter || isa);
	}
	private static boolean isUpper(char ch) {
		return Character.isUpperCase(ch);
	}
	private static boolean aSetter(String name, int argCount) {
		return argCount == 1 && name.startsWith("set");
	}
	private static Method findPrivateMethod(Class type, String name, int args) {
		Method chosenMethod = findMethod(type.getDeclaredMethods(), name, args);
		if (chosenMethod != null) {
			chosenMethod.setAccessible(true);
			return chosenMethod;
		}
		if (type.getSuperclass() != null)
			return findPrivateMethod(type.getSuperclass(), name, args);
		return null;
	}
	private static Method findMethod(Method[] methods, String name, int args) {
		Method chosenMethod = null;
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (name.equals(method.getName()) &&
		            method.getParameterTypes().length == args && 
		            !ClassUtility.fitLibrarySystemMethod(method))
				if (chosenMethod == null)
					chosenMethod = method;
				else
					throw new AmbiguousNameException(name);
		}
		return chosenMethod;
	}
	private static class MethodSignature {
		private Class type;
		private String name;
		private int args;

		public MethodSignature(Class type, String name, int args) {
			this.type = type;
			this.name = name;
			this.args = args;
		}
		public boolean equals(Object object) {
			if (!(object instanceof MethodSignature))
				return false;
			MethodSignature signature = (MethodSignature) object;
			return type == signature.type && name.equals(signature.name)
				&& args == signature.args;
		}
		public int hashCode() {
			return type.hashCode()+name.hashCode()+args;
		}
		public String toString() {
			return type+"."+name+"("+args+")";
		}
	}
}
