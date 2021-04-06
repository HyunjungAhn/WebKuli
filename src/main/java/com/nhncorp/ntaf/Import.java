package com.nhncorp.ntaf;

import fit.Fixture;
import fit.FixtureLoader;
import fit.Parse;
import fitlibrary.DoFixture;

public class Import extends Fixture {
	public void doRow(Parse row) {
		String packageName = row.parts.text();
		FixtureLoader.instance().addPackageToPath(packageName);
	}

	public void interpretFollowingTables(Parse tables) {
		listener.tableFinished(tables);

		Parse followingTables = tables.more;

		while (followingTables != null) {
			Parse heading = followingTables.at(0, 0, 0);
			Fixture fixture = null;

			if (getForcedAbort()) {
				ignore(heading); //Semaphores: ignore on failed lock
			} else if (heading != null) {
				try {
					fixture = getLinkedFixtureWithArgs(followingTables);
					fixture.listener = listener;
					fixture.doTable(followingTables);

				} catch (Throwable e) {
					exception(heading, e);

					for (int i = 0; i < followingTables.size(); ++i) {
						listener.tableFinished(followingTables.at(i));
					}

					break;
				}
			}

			if (null != fixture && (fixture instanceof NtafDoFixture || fixture instanceof DoFixture)) {
				break;
			}

			listener.tableFinished(followingTables);
			followingTables = followingTables.more;
		}
	}
}
