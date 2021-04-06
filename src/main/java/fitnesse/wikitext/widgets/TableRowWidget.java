// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableRowWidget extends ParentWidget {
  private static final Pattern pattern = Pattern.compile("\\|([^\\|\n\r]*)");
  private TableWidget parentTable;
  public static int currRow = 0;
  public static String currUrl = "";
  public static boolean isWebTestPageType = false;

  private boolean isLiteral;

  public TableRowWidget(TableWidget parentTable, String text, boolean isLiteral) throws Exception {
    super(parentTable);
    this.parentTable = parentTable;
    this.isLiteral = isLiteral;
    addCells(text);
  }

  public int getColumns() {
    return numberOfChildren();
  }

  public TableWidget getParentTable() {
    return parentTable;
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer("<tr>");
    currRow++;
    storeUrl();
    
    html.append(childHtml()).append("</tr>\n");
    return html.toString();
  }

  private void storeUrl() throws Exception {
	  final Pattern pattern = Pattern.compile("<td>open</td>\n<td>(http://.*)</td>");
	  final Matcher matcher = pattern.matcher(childHtml());
	  
	  if (matcher.find()) {
		  currUrl = matcher.group(1);
	  }
  }
  
  public void addCells(String text) throws Exception {
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      new TableCellWidget(this, match.group(1), isLiteral);
      addCells(text.substring(match.end()));
    }
  }
}

