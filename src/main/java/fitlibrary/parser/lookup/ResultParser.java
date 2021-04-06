package fitlibrary.parser.lookup;

import fitlibrary.parser.Parser;

public interface ResultParser extends Parser {
	boolean isShowAsHtml();
	void setTarget(Object target);
	Object getResult() throws Exception;
}
