/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify;
import java.util.HashMap;
import fitlibrary.DoFixture;
import fitlibrary.SetFixture;
import fitlibrary.SubsetFixture;

public class MapFixture extends DoFixture {

    public SetFixture getMap() throws Exception {
        HashMap theMap = new HashMap();
        theMap.put("a", "b");     
        theMap.put("c", "d");     
        return new SetFixture(theMap);
    }
    public SubsetFixture getSubsetMap() throws Exception {
        HashMap theMap = new HashMap();
        theMap.put("a", "b");     
        theMap.put("c", "d");     
        return new SubsetFixture(theMap);
    }
}
