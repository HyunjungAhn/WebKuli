package com.nhncorp.ntaf;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import fit.FixtureBridge;
import fit.Parse;

import javax.xml.parsers.*;


/**
 * @author nhn
 * @version 1.0
 * @created 04-8-2008 PM 5:40:12
 */
public class NtafFixture extends FixtureBridge {
	private static final String HELP_XML = "NtafFixtureHelpXML.xml";
	private static final String FIXTURE_COMMENT = "fixturecomment";
	private static final String FIXTURE = "fixture";
	private static final String SEPARATOR = " : ";
	private static final String ATTRIBUTE = "attribute";
	private static final String NO_COMMENT = "nocomment";
	private static final String KEYWORD_COMMENT = "keywordcomment";
	private static final String HTML_BR = "<br>";
	private static final String NAME = "name";
	private static final String KEYWORD = "keyword";

	private static Element eRoot;

	static {
		try {
			InputStream inputStream = NtafFixture.class.getClassLoader().getResourceAsStream(HELP_XML);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();

			Document document = builder.parse(inputStream);
			eRoot = document.getDocumentElement();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	protected void addHelpComment(Parse table) throws Exception {
		Parse fixtureCell = table.parts.parts;
		ignore(fixtureCell);
	}

	public void doTable(Parse table) {
		try {
			Parse sourceCell = getSourceCell(table);

			if (sourceCell == null) {
				addHelpComment(table);
			} else {
				doRows(table.parts.more);
			}
		} catch (Exception e) {
			exception(table.parts.parts, e);
		}
	}

	protected static HashMap<String, String> getKeywordSet(String fixtureName) {
		Text tData;
		HashMap<String, String> keywordSet = new HashMap<String, String>();

		NodeList nFixtureName = eRoot.getElementsByTagName(KEYWORD);
		int items = nFixtureName.getLength();

		for (int i = 0; i < items; i++) {
			Element eFixture = (Element)nFixtureName.item(i);

			if (((Element)eFixture.getParentNode().getParentNode()).getAttribute(NAME).equals(fixtureName)) {
				try {
					String keywordName = eFixture.getAttribute(NAME);
					NodeList nFixturecomment = eFixture.getElementsByTagName(KEYWORD_COMMENT);
					int comments = nFixturecomment.getLength();
					StringBuffer comment = new StringBuffer();

					for (int j = 0; j < comments; j++) {
						Element eFixturecomment = (Element)nFixturecomment.item(j);
						tData = (Text)eFixturecomment.getFirstChild();
						comment.append(tData.getData());
						comment.append(' ');
					}

					keywordSet.put(keywordName, comment.toString());

				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		}

		return keywordSet;
	}

	protected static StringBuffer getFixtureInfo() {
		Text tData;
		StringBuffer targetName = new StringBuffer();

		NodeList nFixtureName = eRoot.getElementsByTagName(FIXTURE);
		int items = nFixtureName.getLength();

		for (int i = 0; i < items; i++) {
			Element eFixture = (Element)nFixtureName.item(i);
			targetName.append(HTML_BR);
			targetName.append(eFixture.getAttribute(NAME));
			targetName.append(SEPARATOR);
			NodeList nFixturecomment = eFixture.getElementsByTagName(FIXTURE_COMMENT);
			int comments = nFixturecomment.getLength();

			for (int j = 0; j < comments; j++) {
				Element eFixturecomment = (Element)nFixturecomment.item(j);
				tData = (Text)eFixturecomment.getFirstChild();
				targetName.append(tData.getData());
				targetName.append(' ');
			}
		}

		return targetName;
	}

	protected static StringBuffer getKeywordInfo(String fixtureName, String keywordName) {
		Text tData;
		StringBuffer targetName = new StringBuffer();
		NodeList nFixtureName = eRoot.getElementsByTagName(KEYWORD);
		int items = nFixtureName.getLength();

		for (int i = 0; i < items; i++) {
			Element eFixture = (Element)nFixtureName.item(i);

			if (((Element)eFixture.getParentNode().getParentNode()).getAttribute(NAME).equals(fixtureName)) {
				if (keywordName.equals("")) {
					targetName.append(HTML_BR);
					targetName.append(eFixture.getAttribute(NAME));
					targetName.append(SEPARATOR);
					NodeList nKeywordcomment = eFixture.getElementsByTagName(KEYWORD_COMMENT);
					int length = nKeywordcomment.getLength();

					for (int j = 0; j < length; j++) {
						Element eFixturecomment = (Element)nKeywordcomment.item(j);
						tData = (Text)eFixturecomment.getFirstChild();
						targetName.append(tData.getData());
						targetName.append(' ');
					}
				} else if (keywordName.toLowerCase(Locale.US).equals(NO_COMMENT)) {
					targetName.append(HTML_BR);
					targetName.append(eFixture.getAttribute(NAME));
				} else if (eFixture.getAttribute(NAME).equals(keywordName)) {
					NodeList nKeywordAttribute = eFixture.getElementsByTagName(ATTRIBUTE);
					int length = nKeywordAttribute.getLength();

					for (int j = 0; j < length; j++) {
						Element eFixturecomment = (Element)nKeywordAttribute.item(j);
						targetName.append(eFixture.getAttribute(NAME));
						targetName.append(SEPARATOR);
						tData = (Text)eFixturecomment.getFirstChild();
						targetName.append(tData.getData());
						targetName.append(HTML_BR);
					}
				}
			}
		}

		return targetName;
	}

	protected static String parsingXML(String fixtureName, String keywordName) throws Exception {
		StringBuffer targetName;

		if (fixtureName.equals("")) {
			targetName = getFixtureInfo();
		} else {
			targetName = getKeywordInfo(fixtureName, keywordName);
		}

		return targetName.toString();
	}

	protected Parse getSourceCell(Parse table) {
		Parse sourceRow = table.parts.more;

		if (sourceRow == null) {
			return null;
		}

		return sourceRow.parts;
	}
}