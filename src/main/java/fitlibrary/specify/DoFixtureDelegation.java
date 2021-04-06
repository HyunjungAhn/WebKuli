/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify;

import java.text.SimpleDateFormat;
import java.util.Date;

import fitlibrary.ArrayFixture;
import fitlibrary.DoFixture;

public class DoFixtureDelegation extends DoFixture {
	private static SimpleDateFormat DATE_FORMAT = 
		   new SimpleDateFormat("yyyy/MM/dd HH:mm");

	public DoFixtureDelegation() {
		registerParseDelegate(Date.class,DATE_FORMAT);
		registerParseDelegate(MyValue.class, new MyValueDelegate());
		registerParseDelegate(MyValueTwo.class, MyValueTwoClassDelegate.class);
	}
	public Object useToString() {
		return new ClassWithNoTypeAdapter();
	}
	public static class ClassWithNoTypeAdapter {
		public String toString() {
			return "77";
		}
	}
	public Date getDate() {
	    return new Date(2004-1900,2,3);
	}
	public MyClass myClass() {
	    return new MyClass(3);
	}
	public MyClass sameMyClass(MyClass value) {
		return value;
	}
	public MyClass myClassPlus(MyClass one, MyClass two) {
		return one.plus(two);
	}
	public static class MyClass {
        private int i;

        public MyClass(int i) {
            this.i = i;
        }
        public MyClass plus(MyClass two) {
			return new MyClass(i+two.i);
		}
		public static MyClass parse(String s) {
			if (s.startsWith("i "))
				return new MyClass(Integer.parseInt(s.substring(2)));
			throw new RuntimeException("Invalid value: must start with 'i '");
	    }
        public String toString() {
            return "i "+i;
        }
        public boolean equals(Object object) {
            if (!(object instanceof MyClass))
                return false;
            return ((MyClass)object).i == i;
        }
	}
	public ArrayFixture aValueSetWithAnObjectDelegate() {
		MyValueHolder[] values = {new MyValueHolder(new MyValue(1)),
				new MyValueHolder(new MyValue(2))};
		return new ArrayFixture(values);
	}
	public static class MyValue {
		private int i;

		public MyValue(int i) {
			this.i = i;
		}
		public String toString() {
			return ""+i;
		}
		public boolean equals(Object other) {
			if (!(other instanceof MyValue))
				return false;
			return i == ((MyValue)other).i;
		}
	}
	public static class MyValueHolder {
		private MyValue value;

		public MyValueHolder(MyValue value) {
			this.value = value;
		}
		public MyValue getValue() {
			return value;
		}
	}
	public static class MyValueDelegate {
		public MyValue parse(String s) {
			return new MyValue(Integer.parseInt(s));
	    }		
	}
	public ArrayFixture aValueSetWithAClassDelegate() {
		MyValueHolderTwo[] values = {new MyValueHolderTwo(new MyValueTwo(1)),
				new MyValueHolderTwo(new MyValueTwo(2))};
		return new ArrayFixture(values);
	}
	public static class MyValueTwo {
		private int i;

		public MyValueTwo(int i) {
			this.i = i;
		}
		public String toString() {
			return ""+i;
		}
		public boolean equals(Object other) {
			if (!(other instanceof MyValueTwo))
				return false;
			return i == ((MyValueTwo)other).i;
		}
	}
	public static class MyValueHolderTwo {
		public MyValueTwo value;

		public MyValueHolderTwo(MyValueTwo value) {
			this.value = value;
		}
		public MyValueTwo getValue() {
			return value;
		}
	}
	public static class MyValueTwoClassDelegate {
		public static MyValueTwo parse(String s) {
			return new MyValueTwo(Integer.parseInt(s));
	    }		
	}
	public ArrayFixture aValueSetWithItsOwnParseMethod() {
		MyValueHolderThree[] values = {new MyValueHolderThree(new MyValueThree(1)),
				new MyValueHolderThree(new MyValueThree(2))};
		return new ArrayFixture(values);
	}
	public static class MyValueThree {
		private int i;

		public MyValueThree(int i) {
			this.i = i;
		}
		public String toString() {
			return ""+i;
		}
		public boolean equals(Object other) {
			if (!(other instanceof MyValueThree))
				return false;
			return i == ((MyValueThree)other).i;
		}
		public static MyValueThree parse(String s) {
			return new MyValueThree(Integer.parseInt(s));
	    }		
	}
	public static class MyValueHolderThree {
		public MyValueThree value;

		public MyValueHolderThree(MyValueThree value) {
			this.value = value;
		}
		public MyValueThree getValue() {
			return value;
		}
	}
}
