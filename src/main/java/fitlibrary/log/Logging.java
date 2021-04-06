/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.log;

import java.lang.reflect.Method;

import fitlibrary.utility.ClassUtility;

public class Logging {
	private static boolean LOGGING = false;
	private static String tab = "";
	private static final String TABBING = "  ";

	public static void setLogging(boolean logging) {
		LOGGING = logging;
	}
    public static void startLog(Object object, String s) {
        log(object,"Start: "+s);
        tab += TABBING;
    }
	public static void startLog(String s) {
		log("Start: "+s);
		tab += TABBING;
	}
    public static void endLog(Object object,String s) {
        log(object,"End: "+s);
        tab = tab.substring(TABBING.length());
    }
	public static void endLog(String s) {
		log("End: "+s);
		tab = tab.substring(TABBING.length());
	}
	public static void log(String s) {
		if (LOGGING)
			System.out.println(tab+s);
	}
	public static void logCall(Object object, Method method, Object[] args) {
		log("Call "+method.getName()+"("+argList(args)+") in object of class "+object.getClass().getName());
	}
	private static String argList(Object[] args) {
		String result = "";
		for (int i = 0; i < args.length; i++) {
			if (i > 0)
				result += ", ";
            Object arg = args[i];
            if (arg == null)
                result += "null";
            else
                result += arg.toString();
		}
		return result;
	}
    public static void log(Class type, String s) {
        log(ClassUtility.simpleClassName(type)+": "+s);
    }
	public static void log(Object object, String s) {
		log(ClassUtility.simpleClassName(object.getClass())+": "+s);
	}
}
