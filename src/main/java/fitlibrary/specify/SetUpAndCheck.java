/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify;

import java.util.ArrayList;
import java.util.List;

import fitlibrary.DoFixture;
import fitlibrary.SetUpFixture;

public class SetUpAndCheck extends DoFixture {
	private SetUpFixture setUp = new MySetUpFixture(this);
	private List colours = new ArrayList();

	public void addColor(Colour colour) {
		colours.add(colour);
	}
	public List colours() {
		return colours;
	}
	public static class MySetUpFixture extends SetUpFixture {
		private SetUpAndCheck switcher;
		
		public MySetUpFixture(SetUpAndCheck switcher) {
			this.switcher = switcher;
			switcher.setSetUpFixture(this);
		}
		public SetUpFixture colours() {
			return this;
		}
		public void colourName(String name) {
			switcher.addColor(new Colour(name));
		}
		public boolean startAction() {
			setUpFinished();
			return true;
		}
	}
	public static class Colour {
		private String colourName;

		public Colour(String name) {
			this.colourName = name;
		}
		public String getColourName() {
			return colourName;
		}
	}
}
