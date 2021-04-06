package fitlibrary.debug;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class GrabPage {
	private static final String openDiv = "<div";
	private static final String closeDiv = "</div>";
	private String host;

	public GrabPage(String host) {
		this.host = host;
	}
	public String grabPage(String page) throws IOException {
		String s = grabBarePage(page);
		s = removeDivClasses("hidden",s);
		s = removeDivClasses("sidebar",s);
		s = removeDivClasses("nav_break",s);
		s = removeDivClasses("header",s);
		s = removeDivClasses("collapse_rim",s);
		s = removeDivStyles("float",s);
		s = removeA("/files",s);
		s = removeA("?searchForm",s);
		s = removeA("javascript:",s);
		s = removeSpan("meta",s);
		s = removeFooter(s);
		s = changePageUrls(s);
		return s;
	}
	public String changePageUrls(String s) {
		String openALink = "<a href=\"";
		String endQuote = "\">";
		String endALink = "</a>";
		int searchFrom = 0;
		while (true) {
			int open = s.indexOf(openALink,searchFrom);
			if (open < 0)
				break;
			int startOfUrl = open+openALink.length();
			int endOfUrl = s.indexOf(endQuote,startOfUrl);
			String url = s.substring(startOfUrl,endOfUrl);
			int endOfALink = s.indexOf(endALink,endOfUrl+endQuote.length());
			String link = s.substring(endOfUrl+endQuote.length(),endOfALink);
			s = s.substring(0,startOfUrl)+url+endQuote+link+s.substring(endOfALink);
			searchFrom = endOfUrl+url.length()-url.length()+link.length()-link.length();
		}
		return s;
	}
	public String grabBarePage(String page) throws MalformedURLException, IOException {
		URL url = new URL(extendedName(page));
		BufferedInputStream reader = new BufferedInputStream(url.openStream());
		String s = "";
		while (true) {
			int in = reader.read();
			if (in < 0)
				break;
			s += (char)in;
		}
		reader.close();
		return s;
	}
	private String extendedName(String pageName) {
		if (host.endsWith("/"))
			return host+pageName;
		return host+"."+pageName;
	}
	private String removeFooter(String s) {
		String openTag = "<hr>(<a href=\"FitLib";
		String closeTag = ".RecentChanges</a>)";
		int open = s.indexOf(openTag);
		if (open < 0)
			return s;
		int end = s.indexOf(closeTag,open+openTag.length());
		if (end < 0)
			return s;
		return s.substring(0,open)+s.substring(end+closeTag.length());
	}
	public static String removeA(String link, String s) {
		return removeTag(s, "<a href=\""+link, "</a>");
	}
	private String removeSpan(String type, String s) {
		return removeTag(s, "<span class=\""+type, "</span>");
	}
	private static String removeTag(String s, String openTag, String endTag) {
		while (true) {
			int open = s.indexOf(openTag);
			if (open < 0)
				break;
			int end = s.indexOf(endTag,open);
			s = s.substring(0,open)+s.substring(end+endTag.length());
		}
		return s;
	}
	private static String removeDivClasses(String type, String s) {
		return removeDiv("class", type, s);
	}
	private static String removeDivStyles(String type, String s) {
		return removeDiv("style", type, s);
	}
	private static String removeDiv(String attribute, String type, String s) {
		String openTag = "<div "+attribute+"=\""+type;
		while (true) {
			int open = s.indexOf(openTag); //show(s, open,"|{");
			if (open < 0)
				break;
			int afterClose = afterMatchedDiv(s,open+openTag.length()); //show(s, afterClose,"|}");
			s = s.substring(0,open)+s.substring(afterClose);
		}
		return s;
	}
	private static int afterMatchedDiv(String s, int start) {
		while (true) {
			//show(s, start,"|+");
			int nextOpen = s.indexOf(openDiv,start); //show(s, nextOpen,"|{");
			int nextClose = s.indexOf(closeDiv,start); //show(s, nextClose,"|}");
			if (nextClose >= 0 && (nextOpen < 0 || nextClose < nextOpen))
				return nextClose + closeDiv.length();
			if (nextOpen < 0)
				return -1;
			start = afterMatchedDiv(s,nextOpen+openDiv.length());
		}
	}
	public String toString() {
		return "GrabPage["+host+"]";
	}
}
