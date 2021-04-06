package fit;

import fitlibrary.exception.classes.ConstructorNotVisible;
import fitlibrary.exception.classes.NoNullaryConstructor;
import fitlibrary.exception.classes.UnknownClassException;
import fitlibrary.table.Cell;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.TestResults;

/** Needed to get at the fixture of the first table of the storytest page.
 */
public class FixtureBridge extends Fixture {
	public Object firstObject(Parse tables, TestResults results) {
		if (tables != null) {
			Parse heading = tables.at(0, 0, 0);
			if (heading != null) {
				Cell headingCell = new Cell(heading);
				try {
					String className = headingCell.text().replaceAll(" ","");
					try {
						return getLinkedFixtureWithArgs(tables);
					} catch (Exception e) {
						try {
							return ClassUtility.newInstance(className);
						} catch (NoSuchMethodException ex) {
							throw new NoNullaryConstructor(className);
						} catch (ClassNotFoundException ex) {
							throw new UnknownClassException(className);
						} catch (InstantiationException ex) {
							throw new NoNullaryConstructor(className);
						} catch (IllegalAccessException ex) {
							throw new ConstructorNotVisible(className);
						}
					}
				}
				catch (Throwable e) {
					headingCell.error(results, e);
				}
			}
		}
		return null;
	}
}
