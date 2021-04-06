package fitnesse.responders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.components.SaveRecorder;
import fitnesse.html.HtmlTag;

public class LayerMenuGenerator {
	private static final String NTAF_HOME = "http://dev.naver.com/projects/ntaf";
	private static final String WEBKIT_VERSION = "WebKit 2.0.0";
	private StringBuffer content = new StringBuffer();
	private Set<String> keywordSet = new TreeSet<String>();

	private String addSeperator() {
		HtmlTag hrTag = new HtmlTag("hr");
		hrTag.addAttribute("class", "hr_style");

		return "<br/>" + hrTag.html();
	}

	private String addFirstRowTag() {
		HtmlTag staticMenuTableRowTag = new HtmlTag("tr");
		HtmlTag staticMenuTableCellTag = new HtmlTag("td");
		staticMenuTableCellTag.addAttribute("class", "common");
		HtmlTag staticMenuHrefTag = new HtmlTag("a");
		staticMenuHrefTag.addAttribute("href", NTAF_HOME);
		staticMenuHrefTag.add(WEBKIT_VERSION);
		staticMenuTableCellTag.add(staticMenuHrefTag);
		staticMenuTableCellTag.add(addSeperator());

		HtmlTag staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "button");
		staticMenuInputTag.addAttribute("name", "save");
		staticMenuInputTag.addAttribute("id", "save999999");
		staticMenuInputTag.addAttribute("title", "해당페이지의 마지막에 webKit 키워드를 추가 합니다.");
		staticMenuInputTag.addAttribute("value", "Save");
		staticMenuInputTag.addAttribute("onclick", "addKeyword(this, 'false', 'webKit');submit();");
		staticMenuTableCellTag.add(staticMenuInputTag);

		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "button");
		staticMenuInputTag.addAttribute("name", "save");
		staticMenuInputTag.addAttribute("id", "test999999");
		staticMenuInputTag.addAttribute("title", "해당페이지의 마지막에 webKit 키워드를 추가 후 테스트를 수행 합니다.");
		staticMenuInputTag.addAttribute("value", "Test");
		staticMenuInputTag.addAttribute("onclick", "addKeyword(this, 'true', 'webKit');submit();");
		staticMenuTableCellTag.add(staticMenuInputTag);

		staticMenuTableRowTag.add(staticMenuTableCellTag);

		return staticMenuTableRowTag.html();
	}

	private HtmlTag addSecondRowTag() {
		HtmlTag staticMenuTableRowTag = new HtmlTag("tr");

		HtmlTag staticMenuTableCellTag = new HtmlTag("td");
		staticMenuTableCellTag.addAttribute("class", "common");
		staticMenuTableCellTag.add(getSelectElement(keywordSet));
		staticMenuTableCellTag.add(addSeperator());

		HtmlTag staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "button");
		staticMenuInputTag.addAttribute("id", "save999999");
		staticMenuInputTag.addAttribute("name", "save");
		staticMenuInputTag.addAttribute("title", "해당 위키 페이지의 마지막에 선택한 키워드를 추가 합니다.");
		staticMenuInputTag.addAttribute("value", "Save");
		staticMenuInputTag.addAttribute("onclick", "addFavorite(this, 'false');submit();");
		staticMenuTableCellTag.add(staticMenuInputTag);

		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "button");
		staticMenuInputTag.addAttribute("id", "test999999");
		staticMenuInputTag.addAttribute("name", "save");
		staticMenuInputTag.addAttribute("title", "해당 위키 페이지의 마지막에 선택한 키워드를 추가 후 테스트를 수행 합니다.");
		staticMenuInputTag.addAttribute("value", "Test");
		staticMenuInputTag.addAttribute("onclick", "addFavorite(this, 'true');submit();");
		staticMenuTableCellTag.add(staticMenuInputTag);
		staticMenuTableRowTag.add(staticMenuTableCellTag);

		return staticMenuTableRowTag;
	}

	private HtmlTag addFifthRowTag() {
		HtmlTag staticMenuTableRowTag = new HtmlTag("tr");
		HtmlTag staticMenuTableCellTag = new HtmlTag("td");
		staticMenuTableCellTag.addAttribute("class", "common");

		HtmlTag staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "text");
		staticMenuInputTag.addAttribute("id", "word");
		staticMenuInputTag.addAttribute("name", "word");
		staticMenuInputTag.addAttribute("title", "추가하고자 하는 명령을 입력 합니다.");
		staticMenuInputTag.addAttribute("style", "width:150px");
		staticMenuInputTag.addAttribute("value", "");
		staticMenuTableCellTag.add(staticMenuInputTag);
		staticMenuTableCellTag.add(addSeperator());

		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "button");
		staticMenuInputTag.addAttribute("name", "save");
		staticMenuInputTag.addAttribute("onclick", "savePage(word.value);");
		staticMenuInputTag.addAttribute("title", "해당 명령어를 추가 합니다.");
		staticMenuInputTag.addAttribute("value", "save");
		staticMenuTableCellTag.add(staticMenuInputTag);
		staticMenuTableRowTag.add(staticMenuTableCellTag);
		return staticMenuTableRowTag;
	}

	private HtmlTag addSixthRowTag() {
		HtmlTag staticMenuTableRowTag = new HtmlTag("tr");
		HtmlTag staticMenuTableCellTag = new HtmlTag("td");
		staticMenuTableCellTag.addAttribute("class", "common");

		HtmlTag staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "checkbox");
		staticMenuInputTag.addAttribute("id", "firefox");
		staticMenuInputTag.addAttribute("name", "firefox");
		staticMenuInputTag.addAttribute("checked", "true");
		staticMenuInputTag.add("firefox");
		staticMenuTableCellTag.add(staticMenuInputTag);

		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "checkbox");
		staticMenuInputTag.addAttribute("id", "safari");
		staticMenuInputTag.addAttribute("name", "safari");
		staticMenuInputTag.add("safari");
		staticMenuTableCellTag.add(staticMenuInputTag);

		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "checkbox");
		staticMenuInputTag.addAttribute("id", "ie");
		staticMenuInputTag.addAttribute("name", "ie");
		staticMenuInputTag.add("ie");
		staticMenuTableCellTag.add(staticMenuInputTag);

		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "checkbox");
		staticMenuInputTag.addAttribute("id", "htmlUnit");
		staticMenuInputTag.addAttribute("name", "htmlUnit");
		staticMenuInputTag.add("htmlUnit");
		staticMenuTableCellTag.add(staticMenuInputTag);

		staticMenuTableCellTag.add(addSeperator());

		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "button");
		staticMenuInputTag.addAttribute("name", "save");
		staticMenuInputTag.addAttribute("onclick", "insertIterate();");
		staticMenuInputTag.addAttribute("title", "선택된 브라우저 별로 반복 테스트 구문을 추가 합니다.");
		staticMenuInputTag.addAttribute("value", "insert");
		staticMenuTableCellTag.add(staticMenuInputTag);

		staticMenuTableRowTag.add(staticMenuTableCellTag);
		return staticMenuTableRowTag;
	}

	public LayerMenuGenerator() throws Exception {
		getFavoriteKeywrodList();

		HtmlTag staticMenuDivTag = addStaticMenuDivTag();
		HtmlTag staticMenuFormTag = addStaticMenuFormTag();
		HtmlTag staticMenuTableTag = addStaticMenuTableTag();

		staticMenuTableTag.add(addFirstRowTag());

		if (true == (keywordSet.size() > 0)) {
			staticMenuTableTag.add(addSecondRowTag());
		}

		staticMenuTableTag.add(addFifthRowTag());
		staticMenuTableTag.add(addSixthRowTag());
		staticMenuFormTag.add(staticMenuTableTag);

		staticMenuFormTag.add(addHiddenInputTag(staticMenuFormTag));
		staticMenuDivTag.add(staticMenuFormTag);
		content.append(staticMenuDivTag.html());
	}

	private HtmlTag addHiddenInputTag(HtmlTag staticMenuFormTag) {
		HtmlTag staticMenuInputTag;
		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "hidden");
		staticMenuInputTag.addAttribute("id", "responder999999");
		staticMenuInputTag.addAttribute("name", "responder");
		staticMenuInputTag.addAttribute("value", "saveData");
		staticMenuFormTag.add(staticMenuInputTag);

		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "hidden");
		staticMenuInputTag.addAttribute("id", "isTest999999");
		staticMenuInputTag.addAttribute("name", "isTest");
		staticMenuInputTag.addAttribute("value", "saveData");
		staticMenuFormTag.add(staticMenuInputTag);

		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "hidden");
		staticMenuInputTag.addAttribute("name", "saveTime");
		staticMenuInputTag.addAttribute("value", String.valueOf(SaveRecorder.timeStamp()));
		staticMenuFormTag.add(staticMenuInputTag);

		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "hidden");
		staticMenuInputTag.addAttribute("name", "ticketId");
		staticMenuInputTag.addAttribute("value", String.valueOf(SaveRecorder.newTicket()));
		staticMenuFormTag.add(staticMenuInputTag);

		staticMenuInputTag = new HtmlTag("input");
		staticMenuInputTag.addAttribute("type", "hidden");
		staticMenuInputTag.addAttribute("id", "pageContent999999");
		staticMenuInputTag.addAttribute("name", "pageContent");
		staticMenuInputTag.addAttribute("value", "");
		return staticMenuInputTag;
	}

	private HtmlTag addStaticMenuTableTag() {
		HtmlTag staticMenuTableTag = new HtmlTag("table");
		staticMenuTableTag.addAttribute("cellspacing", "0");
		staticMenuTableTag.addAttribute("cellpadding", "0");
		staticMenuTableTag.addAttribute("width", "100%");
		staticMenuTableTag.addAttribute("height", "100%");
		staticMenuTableTag.addAttribute("class", "test_table");
		return staticMenuTableTag;
	}

	private HtmlTag addStaticMenuFormTag() {
		HtmlTag staticMenuFormTag = new HtmlTag("form");
		staticMenuFormTag.addAttribute("id", "f999999");
		staticMenuFormTag.addAttribute("name", "f999999");
		staticMenuFormTag.addAttribute("method", "post");
		return staticMenuFormTag;
	}

	private HtmlTag addStaticMenuDivTag() {
		HtmlTag staticMenuDivTag = new HtmlTag("div");
		staticMenuDivTag.addAttribute("id", "STATICMENU");
		staticMenuDivTag.addAttribute("name", "STATICMENU");
		staticMenuDivTag.addAttribute("style", "position:absolute;left:700px;top:41px;width:120px;z-index:9999;visibility:hidden;");
		return staticMenuDivTag;
	}

	private String getSelectElement(Set<String> keywordSet) {
		StringBuffer select = new StringBuffer("<select id='favorites' title='키워드 즐겨찾기'>");

		for (String keyword : keywordSet) {
			select.append("<option value='" + keyword + "'>" + keyword + "</option>");
		}

		select.append("</select>");

		return select.toString();
	}

	private Set<String> getFavoriteKeywrodList() throws Exception {
		keywordSet.clear();
		String rootPath = System.getProperty("FIT_ROOT");
		String detailPath;

		if (rootPath != null) {
			detailPath = rootPath + "/KeywordList/content.txt";
		} else {
			rootPath = System.getProperty("user.dir");
			detailPath = rootPath + "/FitNesseRoot/KeywordList/content.txt";
		}

		BufferedReader reader = new BufferedReader(new FileReader(detailPath));
		String line;

		while (null != (line = reader.readLine())) {
			Pattern pattern = Pattern.compile("!\\|([\\w]*)\\|");
			Matcher matcher = pattern.matcher(line);

			if (true == matcher.find()) {
				keywordSet.add(matcher.group(1));
			}
		}

		return keywordSet;
	}

	public String toString() {
		return content.toString();
	}
}
