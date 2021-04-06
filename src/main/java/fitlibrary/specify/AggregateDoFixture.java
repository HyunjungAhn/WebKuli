/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import fitlibrary.DoFixture;

public class AggregateDoFixture extends DoFixture {
	public Point sumAndGives(Point pt1, Point pt2) {
		return pt1.plus(pt2);
	}
	public Set setAndGives(Point pt1, Point pt2) {
		 HashSet set = new HashSet();
		 set.add(pt1);
		 set.add(pt2);
		 return set;
	 }
	public List addAndGives(List lists, Point pt2) {
		List results = new ArrayList();
		for (Iterator it = lists.iterator(); it.hasNext(); ) {
			Point pt1 = (Point)it.next();
			results.add(pt1.plus(pt2));
		}
		return results;
	}
	public Point xY(int x, int y) {
		return new Point(x,y);
	}
	
	public static class Point {
		private int x,y;

		public Point() {
			//
		}
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public int getX() {
			return x;
		}
		public void setX(int x) {
			this.x = x;
		}
		public int getY() {
			return y;
		}
		public void setY(int y) {
			this.y = y;
		}
		public Point plus(Point other) {
			return new Point(x+other.x,y+other.y);
		}
	}
}
