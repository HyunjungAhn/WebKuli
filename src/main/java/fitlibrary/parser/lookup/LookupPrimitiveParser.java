/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.parser.lookup;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fitlibrary.exception.parse.CouldNotParseException;
import fitlibrary.parser.DelegateParser;
import fitlibrary.parser.Parser;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.Typed;

public class LookupPrimitiveParser {
	private static Map classToPropertyEditor = new HashMap();
	static Set nullables = new HashSet();
	
	public static ParserFactory parserFactory(final Typed typed) {
		final PropertyEditor editor = (PropertyEditor)classToPropertyEditor.get(typed.asClass());
		if (editor != null)
			return new ParserFactory() {
			  public Parser parser(Evaluator evaluator, Typed typed2) {
				return new PrimitiveParser(evaluator,typed2,editor,nullables.contains(typed.asClass()));
			}
		};
		// Include check for PropertyEditor in PropertyEditorManager.findEditor(type);
		return null;
	}
	static {
		Class[] classes = { byte.class, Byte.class,
				short.class, Short.class, int.class, long.class, Long.class,
				float.class, Float.class, double.class, Double.class, String.class};
		for (int i = 0; i < classes.length; i++)
			add(classes[i]);
		classToPropertyEditor.put(Integer.class,classToPropertyEditor.get(int.class));
		PropertyEditor charEditor = new CharPropertyEditor();
		classToPropertyEditor.put(char.class,charEditor);
		classToPropertyEditor.put(Character.class,charEditor);
		PropertyEditor boolEditor = new BooleanPropertyEditor();
		classToPropertyEditor.put(boolean.class,boolEditor);
		classToPropertyEditor.put(Boolean.class,boolEditor);
		classToPropertyEditor.put(Date.class,new DatePropertyEditor());
		classToPropertyEditor.put(Class.class,new ClassPropertyEditor());
		nullables.add(Byte.class);
		nullables.add(Short.class);
		nullables.add(Integer.class);
		nullables.add(Long.class);
		nullables.add(Float.class);
		nullables.add(Double.class);
		nullables.add(Date.class);
	}
	private static void add(Class type) {
		PropertyEditor findEditor = PropertyEditorManager.findEditor(type);
		if (findEditor != null)
			classToPropertyEditor.put(type,findEditor);
		else
			System.out.println("Unable to find a PropertyEditor for: "+type);
	}
	public static class ClassPropertyEditor extends PropertyEditorSupport {
		public void setAsText(String text) throws IllegalArgumentException {
			try {
				setValue(Class.forName(text));
			} catch (ClassNotFoundException e) {
				throw new CouldNotParseException(Class.class,text);
			}
		}
	}
	public static class CharPropertyEditor extends PropertyEditorSupport {
		public void setAsText(String text) throws IllegalArgumentException {
			if ("".equals(text))
				setValue(new Character(' '));
			else 
				setValue(new Character(text.charAt(0)));
		}
	}
	public static class BooleanPropertyEditor extends PropertyEditorSupport {
		public void setAsText(String text) throws IllegalArgumentException {
			text = text.toLowerCase();
			boolean bool = (text.equals("true") || text.equals("yes") || 
					text.equals("1") || text.equals("y") || text.equals("+"));
			setValue(new Boolean(bool));
		}
	}
	public static class DatePropertyEditor extends PropertyEditorSupport {
		private static final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
		public void setAsText(String text) throws IllegalArgumentException {
			try {
				setValue(dateFormatter.parse(text));
			} catch (ParseException e) {
				throw new IllegalArgumentException(text);
			}
		}
	}
	public static class ParseMethodDelegateParser extends DelegateParser {
		Method parseMethod;
		
		public ParseMethodDelegateParser(Class type) {
			super(type);
			try {
				parseMethod = type.getMethod("parse", new Class[]{ String.class });
			} catch (Exception e) {
				throw new RuntimeException("Unexpected exception: "+e);
			}
		}
		public Object parse(String text) throws Exception {
			return parseMethod.invoke(null, new Object[]{ text });
		}
	}
}
