// Copyright (C) 2009 by NHN Corporation. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class ChartWidget extends ParentWidget {
	public static final String REGEXP = "^!chart-(file|data|url) [^\r\n]*";
	private static final Pattern chartPattern = Pattern.compile("^!chart-(file|data|url) (.*)");

	private String ind;
	private String data;
	private String text;

	public ChartWidget(ParentWidget parent, String text) throws IOException {
		super(parent);
		this.text = text;
		Matcher match = chartPattern.matcher(text);

		if (match.find()) {
			this.ind = match.group(1);
			this.data = match.group(2);
		}
	}

	public String render() throws Exception {
		StringBuffer chartData = new StringBuffer();
		
		if (ind == null || data == null) {
			return "";
		}
		
		try{		
			if(ind.equals("file")) {
				if(data.contains("${FIT_ROOT}")) {
					data = data.replace("${FIT_ROOT}", System.getProperty("FIT_ROOT"));
				}
				else if(data.contains("${SYS_ROOT}")) {
					data = data.replace("${SYS_ROOT}", System.getProperty("user.dir"));
				}
								
				FileReader reader = new FileReader(data);
								
				LineNumberReader lineReader = new LineNumberReader(reader);
				String[] fileName = new String[]{};
				fileName = data.split("(\\\\)|(/)"); // c:\testdir\testsubdir\test.txt or /home/testdir/testsubdir/test.txt				
				
				chartData.append("<img src=\"/chart?file={" + fileName[fileName.length-1] + "}");
				
				while(true) {
					String str = lineReader.readLine();
					if (null == str) {
						break;
					}
				chartData.append("{" + str + "}");
				
				}
				chartData.append("\">");			
				data = chartData.toString();
	
				return chartData.toString();
			}
			else {
				return "<img src=\"/chart?" + ind + "=" + data + "\">";
			}
		}catch(FileNotFoundException e) {
			e.printStackTrace();
			return text;
		}
	}
}
