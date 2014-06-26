package de.hpi.krestel.mySearchEngine.domain;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.BM25;
import de.hpi.krestel.mySearchEngine.TokenStream;
import de.hpi.krestel.mySearchEngine.parser.ParseHTMLToText;
import de.hpi.krestel.mySearchEngine.parser.ParseWikiToHTMLUtility;
import de.hpi.krestel.mySearchEngine.parser.ReadXMLParser;
import de.hpi.krestel.mySearchEngine.parser.ReadXMLParser2;
import de.hpi.krestel.mySearchEngine.parser.WikiXMLIterable;


/**
 * @author easten
 * class to contain pages from xml file
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

	public WikiPage(){  
		text = "";
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
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public void addText(String data) {
		text += data;
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
		if (!positionInXMLFileSet) { throw new AssertionError();}
		return positionInXMLFile;
	}
	
	public static WikiPage from(String xmlFilePath, long positionInXMLFile) throws IOException, XMLStreamException {
		WikiXMLIterable wikiPages = new WikiXMLIterable(xmlFilePath);
		ReadXMLParser2 parser = wikiPages.iterator();
		parser.jumpToPosition(positionInXMLFile);		
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
		//ParseHTMLToText htmlParser = new ParseHTMLToText();
		String text = this.getText();
		//String html = ParseWikiToHTMLUtility.parseMediaWiki(text);
		String html = text;
		html = html.replaceFirst("<\\?[^>]*\\?>", "");
		//String parsedHTML = htmlParser.parseHTML(html);
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
	
	public String resultGenerate(String query, WikiPage wikiPage, int resultSize, int flag)
	{
		String returnString = "";
		returnString += "***** " + flag + "." + wikiPage.getTitle() + " *****" + "\n";
		
		String entireText = wikiPage.getText();
		String key = null;
		int tot = 0;
		int queryPosition = 0;
		StringTokenizer stringTokenizer = new StringTokenizer(entireText, " ");
		
		//find queryPosition
		while(stringTokenizer.hasMoreTokens())
		{
			tot = tot + 1;
			key = stringTokenizer.nextToken();
			if(key.toLowerCase().contains(query.toLowerCase()))
			{
				queryPosition = tot ;
				break;
			}	
		}
		
		tot = 0;
		StringTokenizer stringTokenizer2 = new StringTokenizer(entireText, " ");
		while(stringTokenizer2.hasMoreTokens())
		{
			tot = tot + 1;
			key = stringTokenizer2.nextToken();
			
			if(queryPosition - resultSize <= tot && tot <= queryPosition + resultSize)
			{
				returnString += key + " ";
				returnString = returnString.replaceAll("\\[", "");
				returnString = returnString.replaceAll("\\]", "");
				returnString = returnString.replaceAll("\\{", "");
				returnString = returnString.replaceAll("\\}", "");

				//debug
				//System.out.println("added string: " + returnString);
			}
			
		}
		returnString += "\n";
		
		return returnString;
	}
	
	public double getScore(){
		return this.score;
	}
	
	public void setScore(double result){
		this.score = result;
	}
}
