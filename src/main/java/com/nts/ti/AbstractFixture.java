package com.nts.ti;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.nhncorp.ntaf.FlowFixture;

import fit.FitServer;
import fit.Parse;
import fit.exception.FitParseException;
import fitlibrary.table.Row;
import fitlibrary.traverse.CommentTraverse;
import fitlibrary.utility.TestResults;

public class AbstractFixture extends FlowFixture{	
	
	final static String BLUE = "#0000ff";
	final static String RED = "#ff0000";
	final static String ORANGE = "#ffffcf";	
//	private final static String[] keywords = { "click", "exists", "getText", "sendKey", "send key", "get text", "clearText", "clear text", "flick", "singleTap", "single tap", "longTap", "long tap", "scroll", "check"};
	private final static String[] keywords = { "exists", "getText", "get text", "hasText", "has text"};
	
	static List<String> keywordList = new ArrayList<String>();
	static {
		for (String k : keywords) {
			keywordList.add(k);
		}
	}
	
	public void doTable(Parse p) {
		Parse table = p;
		StringBuilder sb = new StringBuilder();
		while (null != table) {
			Parse tr = table.parts;
			String firstCellValue = tr.parts.body;
			String secondCellValue = "";
			if(tr.parts.more!=null){
				secondCellValue = tr.parts.more.body;
			}
			if (null != table.leader && !table.leader.isEmpty())
				sb.append(table.leader);

			if(firstCellValue.startsWith("start_") && tr.size()>1){
				for(int i=0, trCnt=tr.size();i<trCnt;i++){
					sb.append("<br/>");
					sb.append(table.tag);
					sb.append(tr.tag);
					Parse td = tr.at(i).parts;
					while (td != null) {
						sb.append(td.tag + td.body + td.end);
						td = td.more;
					}
					sb.append(tr.end);
					sb.append(table.end);
				}
			}else{
				
				if (tr.size() < 2) {
					sb.append(table.tag);
	
					sb.append(tr.tag);
					Parse td = tr.parts;
					while (td != null) {
						sb.append(td.tag + td.body + td.end);
						td = td.more;
					}
					
					sb.append(tr.end);
					
					if (keywordList.contains(firstCellValue) || keywordList.contains(secondCellValue)) {
						sb.append(addElapsedRow(countColspan(table)));
					}
					sb.append(table.end);
				} else {
					String showTag = "";
					sb.append(table.tag);
					while (tr != null) {
						firstCellValue = tr.parts.body;
						if(tr.parts.more!=null){
							secondCellValue = tr.parts.more.body;
						}
						Parse td = tr.parts;
						if(!firstCellValue.equals("show")){
							sb.append(tr.tag);
							while (td != null) {
								sb.append(td.tag + td.body + td.end);
								td = td.more;
							}
							sb.append(tr.end);
							if (keywordList.contains(firstCellValue) || keywordList.contains(secondCellValue)) {
								sb.append(addElapsedRow(countColspan(table)));
							}
						}else{
							showTag += "<br/><table border=\"1\" cellspacing=\"0\"><tr>";
							while (td != null) {
								showTag += (td.tag + td.body + td.end);
								td = td.more;
							}
							showTag += "</tr></table>";
						}
						
						tr = tr.more;
					}
					sb.append(table.end);
					if(!showTag.isEmpty())	sb.append(showTag);
				}
			}
			
			if (null != table.trailer && !table.trailer.isEmpty())	sb.append(table.trailer);
			
			table = table.more;
		}
		try {
			interpretTables(new Parse(sb.toString()));
		} catch (FitParseException e) {
			FitServer.setSymbol("$elapsed$", null);
			e.printStackTrace();
		}
	}
	
	private int countColspan(Parse table){
		Parse tr = table.parts;
		int colSpan = 0;
		while(tr!=null){
			int span = 0;
			Parse td = tr.parts;
			span = td.size();
			if(span>colSpan)	colSpan = span;
			tr = tr.more;
		}
		return colSpan;
	}
	
	private String addElapsedRow(int colSpan){
		if(colSpan>0){
			return "<tr><td colspan="+colSpan+">elapsed</td></tr>";
		}
		return "<tr><td>elapsed</td></tr>";
	}

	/**
	 * @param row
	 * @param testResults
	 * @return
	 * @throws Exception
	 */
	public CommentTraverse elapsed(Row row, TestResults testResults)
			throws Exception {
		if(null!=FitServer.getSymbol("$elapsed$")){
			long duration = (Long) FitServer.getSymbol("$elapsed$");
			NumberFormat nf = NumberFormat.getNumberInstance();
			if(duration>3000){
				String elapsedBody = "&nbsp;<strong><span class='fail'>" + nf.format(duration) + "&nbsp;ms</span></strong>";
				row.lastCell().parse.addToBody(elapsedBody);
			}else if(duration>2000){
				String elapsedBody = "&nbsp;<span class='error'>" + nf.format(duration) + "&nbsp;ms</span>";
				row.lastCell().parse.addToBody(elapsedBody);
			}else{
				String elapsedBody = "&nbsp;<span class='pass'>" + nf.format(duration) + "&nbsp;ms</span>";
				row.lastCell().parse.addToBody(elapsedBody);
			}
		}
		return new CommentTraverse();
	}
	
	protected void elapsedTime(long t1) {
		FitServer.setSymbol("$elapsed$", (System.currentTimeMillis() - t1));
	}
}
