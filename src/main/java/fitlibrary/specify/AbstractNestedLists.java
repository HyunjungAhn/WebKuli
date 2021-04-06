/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify;

import java.util.ArrayList;
import java.util.List;

import fitlibrary.DoFixture;

public abstract class AbstractNestedLists extends DoFixture {
	public static class Fruit {
		private String name;
		private List list = new ArrayList();
		
		public Fruit(String name, List list) {
			this.name = name;
			this.list = list;
		}
		public String getName() {
			return name;
		}
		public List getElements() {
			return list;
		}
		public String toString() {
			return "Fruit("+name+","+list+")";
		}
	}
	public static class Element {
		private String id;
		private int count;
		private List subElements = new ArrayList();
		private Element next;

		public Element() {
			//
		}
		public Element(String id, int count) {
			this.id = id;
			this.count = count;
		}
		public Element(String id, int count, List list) {
			this(id,count);
			this.subElements = list;
		}
		public int getCount() {
			return count;
		}
		public String getId() {
			return id;
		}
		public List getSubElements() {
			return subElements;
		}
		public String toString() {
			return "Element("+id+","+count+")";
		}
		public void setNext(Element next) {
			this.next = next;
		}
		public Element getNext() {
			return next;
		}
		public void setId(String id) {
			this.id = id;
		}
		public static Element parse(String s) {
			return new Element("newOne",99);
		}
	}
	public static class SubElement {
		private String a, b;

		public SubElement(String a, String b) {
			this.a = a;
			this.b = b;
		}
		public String getA() {
			return a;
		}
		public String getB() {
			return b;
		}
		public String toString() {
			return "SubElement("+a+","+b+")";
		}
	}
}
