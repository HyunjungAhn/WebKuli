/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.parser.lookup;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import fitlibrary.parser.DelegateParser;
import fitlibrary.parser.DelegatingParser;
import fitlibrary.parser.Parser;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.Typed;

public class ParseDelegation {
	// This map is cleared at the end of each storytest, allowing for different Parse Delegates
	// in different storytests.
	private static Map PARSE_DELEGATES = new HashMap(); // <Class,DelegateParser>
	
	/** Registers a delegate, a class that will handle parsing of other types of values.
	 */
    public static void registerParseDelegate(Class type, Class parseDelegate) {
    	DelegateClassParser delegate = DelegateClassParser.findDelegate(parseDelegate);
    	if (delegate != null) {
    		PARSE_DELEGATES.put(type,delegate);
    	}
    	else
    		throw new RuntimeException("Parse delegate class "+parseDelegate.getName()+
    		" does not have a suitable static parse() method.");
    }
	/** Registers a delegate object that will handle parsing of other types of values.
	 */
	public static void registerParseDelegate(Class type, Object parseDelegate) {
	    try {
	        DelegateObjectParser delegate = new DelegateObjectParser(parseDelegate);
			PARSE_DELEGATES.put(type,delegate);
	    } catch (Exception ex) {
	        throw new RuntimeException("Parse delegate object of class "+
	                parseDelegate.getClass().getName()+
	                " does not have a suitable parse() method.");
	    }
	}
    public static void clearDelegatesForNextTest() {
        PARSE_DELEGATES.clear();
    }
	public static boolean hasParseMethod(Class type)  {
		return findParseMethod(type) != null;
	}
	public static Method findParseMethod(Class type)  {
		try {
			Method method = type.getMethod("parse", new Class[]{ String.class });
	        int modifiers = method.getModifiers();
            Class returnType = method.getReturnType();
			if (Modifier.isStatic(modifiers) &&
	        	   Modifier.isPublic(modifiers) &&
	        	   returnType != Void.class &&
	        	   !returnType.isPrimitive())
            	return method;
		} catch (NoSuchMethodException e) {
			//
		}
		return null;
	}
	static class DelegateClassParser extends DelegateParser implements Cloneable {
	    private Method parseMethod;
	    
	    public static DelegateClassParser findDelegate(Class type) {
	        Method parseMethod = findParseMethod(type);
	        if (parseMethod == null)
	        	return null;
	        return new DelegateClassParser(parseMethod);
	    }
	    public DelegateClassParser(Method parseMethod) {
			this.parseMethod = parseMethod;
		}
		public Object parse(String s) throws Exception {
		    return parseMethod.invoke(null, new Object[] { s });
		}
	}
	static class DelegateObjectParser extends DelegateParser implements Cloneable {
	    private Object delegate;
	    private Method parseMethod;
	    
	    public DelegateObjectParser(Object delegate) throws SecurityException, NoSuchMethodException {
	        super(null);
	        this.delegate = delegate;
	        this. parseMethod = delegate.getClass().getMethod("parse",
	                new Class[] { String.class });
	    }
		public Object parse(String s) throws Exception {
		    return parseMethod.invoke(delegate, new Object[] { s });
		}
	}
	public static DelegateParser getDelegate(Class type) {
		return (DelegateParser) PARSE_DELEGATES.get(type);
	}
	public static ParserFactory delegateClassFactory(final Typed typed) {
		final DelegateParser classParser = DelegateClassParser.findDelegate(typed.asClass());
		if (classParser != null)
			return new ParserFactory() {
			public Parser parser(Evaluator evaluator, Typed typed2) {
				return new DelegatingParser(classParser,evaluator,typed2);
			}
		};
		return null;
	}
}
