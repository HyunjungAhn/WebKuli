package fitlibrary.specify.collection;

import java.util.ArrayList;
import java.util.List;

public class NullInCollection {
	public List getList() {
		ArrayList list = new ArrayList();
		list.add(null);
		list.add("fitlibrary");
		return list;
	}

}
