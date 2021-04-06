/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.parser.graphic;

import fitlibrary.differences.LocalFile;
import fitlibrary.log.Logging;
import fitlibrary.traverse.Traverse;

/**
 * Used to check whether the name of the image is as expected.
 */
public class ImageNameGraphic implements GraphicInterface {
    private LocalFile expectedFile;

    public ImageNameGraphic(String expectedFileName) {
        this.expectedFile = Traverse.getLocalFile(expectedFileName);
    }
    public ImageNameGraphic(LocalFile expectedFile) {
        this.expectedFile = expectedFile;
    }
    public LocalFile toGraphic() {
        return expectedFile;
    }
    public boolean equals(Object object) {
        if (!(object instanceof ImageNameGraphic))
                return false;
        boolean equals = expectedFile.equals(
                ((ImageNameGraphic)object).expectedFile);
        Logging.log(this,"equals(): "+expectedFile+" and "+object+" equals="+equals);
        return equals;
    }
    public static GraphicInterface parseGraphic(LocalFile expectedFile) {
        Logging.log(GraphicInterface.class,"parseGraphic(): "+expectedFile);
        return new ImageNameGraphic(expectedFile);
    }
    public String toString() {
        return "ImageNameGraphic["+expectedFile+"]";
    }
}
