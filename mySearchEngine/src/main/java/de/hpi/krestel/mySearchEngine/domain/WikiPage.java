package de.hpi.krestel.mySearchEngine.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.BM25;
import de.hpi.krestel.mySearchEngine.TokenStream;
import de.hpi.krestel.mySearchEngine.parser.ParseHTMLToText;
import de.hpi.krestel.mySearchEngine.parser.ParseWikiToHTMLUtility;
import de.hpi.krestel.mySearchEngine.parser.ReadXMLParser;
import de.hpi.krestel.mySearchEngine.parser.ReadXMLParser2;
import de.hpi.krestel.mySearchEngine.parser.WikiXMLIterable;

/**
 * @author easten class to contain pages from xml file
 */
public class WikiPage {
	private String title;
	private String id;
	private String text;
	private double score;
	private long positionInXMLFile;
	private boolean positionInXMLFileSet;
	private long stopPositionInXMLFile;
	private long positionInTitleListFile;
	private boolean stopPositionInXMLFileSet;
	private List<String> tokens;
	private StringBuilder strBuilderText;
	private static final Pattern LINKS = Pattern.compile("\\[\\[\\s*([^\\]\\|#]*)\\s*([\\|#][^\\]]*\\s*)?\\]\\]");	

	public WikiPage() {
		text = "";
		this.strBuilderText = new StringBuilder();
		stopPositionInXMLFileSet = false;
		positionInXMLFileSet = false;		
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title.trim();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		//return this.text;
		return strBuilderText.toString();
	}

	public void setText(String text) {
		//this.text = text;
		this.strBuilderText = new StringBuilder();
		this.strBuilderText.append(text);
	}

	public void addText(String data) {
		//text += data;
		//text = text.concat(data);		
		this.strBuilderText.append(data);
	}

	public void setPositionInXMLFile(long position) {
		positionInXMLFileSet = true;
		positionInXMLFile = position;
	}

	public void setPositionInTitleListFile(long position) {
		this.positionInTitleListFile = position;
	}

	public long getPositionInTitleListFile(long position) {
		return this.positionInTitleListFile;
	}

	public long getPositionInXMLFile() {
		if (!positionInXMLFileSet) {
			throw new AssertionError();
		}
		return positionInXMLFile;
	}

	public static WikiPage from(String xmlFilePath, long positionInXMLFile)
			throws IOException, XMLStreamException {
		WikiXMLIterable wikiPages = new WikiXMLIterable(xmlFilePath);
		ReadXMLParser parser = wikiPages.iterator();
		parser.jumpToPosition(positionInXMLFile);
		WikiPage wikiPage = parser.next();
		assert wikiPage.getPositionInXMLFile() == positionInXMLFile;
		wikiPage.setPositionInXMLFile(positionInXMLFile);		
		return wikiPage;
	}

	public void setStopPositionInXMLFile(long lastPageLocation) {
		stopPositionInXMLFile = lastPageLocation;
		stopPositionInXMLFileSet = true;
	}

	public long getStopPositionInXMLFile() {
		if (!stopPositionInXMLFileSet) {
			throw new AssertionError();
		}
		return stopPositionInXMLFile;
	}

	String cleanUpWikiText() {
		// ParseHTMLToText htmlParser = new ParseHTMLToText();
		String text = this.getText();
		// String html = ParseWikiToHTMLUtility.parseMediaWiki(text);
		String html = text;
		html = html.replaceFirst("<\\?[^>]*\\?>", "");
		// String parsedHTML = htmlParser.parseHTML(html);
		return html.toString();
	}

	public long numberOfTerms() {
		return asTokens().size();
	}

	public List<String> asTokens() {
		if (tokens == null) {
			TokenStream tokenStream = new TokenStream();
			tokens = tokenStream.preprocessText(cleanUpWikiText());
		}
		return tokens;
	}

	public int countOfTerm(Term term) {
		int number = 0;
		for (String token : asTokens()) {
			if (term.matches(token)) {
				number += 1;
			}
		}
		return number;
	}

	public String mostFrequentWord() {
		List<String> tokens = this.asTokens();
		TreeMap<String, Integer> frequency = new TreeMap<String, Integer>();
		String mostUsedToken = "";
		int freq = 0;
		for (String token : tokens) {
			if (frequency.containsKey(token)) {
				frequency.put(token, frequency.get(token) + 1);
			} else {
				frequency.put(token, 1);
			}
			if (frequency.get(token) > freq) {
				freq = frequency.get(token);
				mostUsedToken = token;
			}
		}
		return mostUsedToken;
	}

	public String generateSnippet(String query, int resultSize,
			int snippetNumber) {
		String returnString = "";
		returnString += "***** " + snippetNumber + "." + this.getTitle()
				+ " *****" + "\n";

		String entireText = this.getText();
		String key = null;
		int position = 0;
		int queryPosition = -1;
		StringTokenizer stringTokenizer = new StringTokenizer(entireText, " ");

		// find queryPosition
		while (stringTokenizer.hasMoreTokens()) {
			position = position + 1;
			key = stringTokenizer.nextToken();
			if (key.toLowerCase().contains(query.toLowerCase())) {
				queryPosition = position;
				break;
			}
		}
		if (queryPosition == -1) {
			throw new AssertionError("Query: " + query
					+ " not found in WikiPage " + this.getTitle());
		}
		position = 0;
		StringTokenizer stringTokenizer2 = new StringTokenizer(entireText, " ");
		while (stringTokenizer2.hasMoreTokens()) {
			position = position + 1;
			key = stringTokenizer2.nextToken();

			if (queryPosition - resultSize <= position
					&& position <= queryPosition + resultSize) {
				returnString += key + " ";
				returnString = returnString.replaceAll("\\[", "");
				returnString = returnString.replaceAll("\\]", "");
				returnString = returnString.replaceAll("\\{", "");
				returnString = returnString.replaceAll("\\}", "");

				// debug
				// System.out.println("added string: " + returnString);
			}

		}
		returnString += "\n";

		return returnString;
	}

	public double getScore() {
		return this.score;
	}

	public void setScore(double result) {
		System.out.println("title: " + this.title);
		System.out.println("score :" + result);
		this.score = result;
	}

	public List<String> getLinks() {
		// synchronize with MyQuery.setQuery
		List<String> links = new ArrayList<String>();
		// from
		// http://stackoverflow.com/questions/6020384/create-array-of-regex-matches
		//Matcher m = Pattern.compile(
		//		"\\[\\[\\s*([^\\]\\|#]*)\\s*([\\|#][^\\]]*\\s*)?\\]\\]")
		//		.matcher(getText());
		Matcher m = LINKS.matcher(getText());
		while (m.find()) {
			String link;
			link = m.group(1);
			link = link.replaceAll(" ", "]"); // we shall have no spaces there
												// because they destroy the
												// index
			link.toLowerCase();
			links.add("[[" + link + "]]");
		}
		return links;
	}

}
