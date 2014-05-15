package de.hpi.krestel.mySearchEngine.domain;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.TokenStream;
import de.hpi.krestel.mySearchEngine.parser.ParseHTMLToText;
import de.hpi.krestel.mySearchEngine.parser.ParseWikiToHTMLUtility;
import de.hpi.krestel.mySearchEngine.parser.ReadXMLParser;
import de.hpi.krestel.mySearchEngine.parser.WikiXMLIterable;


/**
 * @author easten
 * class to contain pages from xml file
 */
public class WikiPage {
	private String title;
	private String id;   
	private String text;
	private long positionInXMLFile;
	private boolean positionInXMLFileSet; 
	private long stopPositionInXMLFile;
	private boolean stopPositionInXMLFileSet;
	private List<String> tokens;

	public WikiPage(){  
		text = "";
		stopPositionInXMLFileSet = false;
		positionInXMLFileSet = false;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void addText(String data) {
		text += data;
	}

	public void setPositionInXMLFile(long position) {
		positionInXMLFileSet = true;
		positionInXMLFile = position;
	}	
	
	public long getPositionInXMLFile() {
		if (!positionInXMLFileSet) { throw new AssertionError();}
		return positionInXMLFile;
	}
	
	public static WikiPage from(String xmlFilePath, long positionInXMLFile) throws IOException, XMLStreamException {
		WikiXMLIterable wikiPages = new WikiXMLIterable(xmlFilePath);
		ReadXMLParser parser = wikiPages.iterator();
		parser.jumpToPosition(positionInXMLFile);
		boolean thereIsAWikiPage = parser.hasNext(); 
		assert thereIsAWikiPage;
		WikiPage wikiPage =  parser.next();
		assert wikiPage.getPositionInXMLFile() == positionInXMLFile;
		wikiPage.setPositionInXMLFile(positionInXMLFile);
		return wikiPage;
	}

	public void setStopPositionInXMLFile(long lastPageLocation) {
		stopPositionInXMLFile = lastPageLocation;
		stopPositionInXMLFileSet = true;
	}
	
	public long getStopPositionInXMLFile() {
		if (!stopPositionInXMLFileSet) { throw new AssertionError();}
		return stopPositionInXMLFile;
	}
	
	String cleanUpWikiText() {
		ParseHTMLToText htmlParser = new ParseHTMLToText();
		String text = this.getText();
		String html = ParseWikiToHTMLUtility.parseMediaWiki(text);
		html = html.replaceFirst("<\\?[^>]*\\?>", "");
		String parsedHTML = htmlParser.parseHTML(html);
		return parsedHTML.toString();
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
	
	public String mostFrequentWord (){
		List<String> tokens = this.asTokens();
		TreeMap<String, Integer> frequency = new TreeMap<String,Integer>();
		String mostUsedToken = "";
		int freq = 0;
		for (String token:tokens){
			if (frequency.containsKey(token)){
				frequency.put(token, frequency.get(token)+1);
			}
			else{
				frequency.put(token, 1);
			}
			if (frequency.get(token) > freq){
				freq=frequency.get(token);
				mostUsedToken=token;
			}
		}
		return mostUsedToken;
		
	}
}
