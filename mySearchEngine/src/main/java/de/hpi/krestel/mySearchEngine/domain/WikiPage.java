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
import de.hpi.krestel.mySearchEngine.Index;
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
	private Double BM25;
	private Index BM25Index;
	private List<Term> BM25QueryTerms;

	public WikiPage() {
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
		if (!positionInXMLFileSet) {
			throw new AssertionError();
		}
		return positionInXMLFile;
	}

	public static WikiPage from(String xmlFilePath, long positionInXMLFile)
			throws IOException, XMLStreamException {
		WikiXMLIterable wikiPages = new WikiXMLIterable(xmlFilePath);
		ReadXMLParser2 parser = wikiPages.iterator();
		parser.jumpToPosition(positionInXMLFile);
		WikiPage wikiPage = parser.next();
		assert wikiPage.getPositionInXMLFile() == positionInXMLFile;
		wikiPage.setPositionInXMLFile(positionInXMLFile);
		parser.close();
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

	public String generateSnippet(String query, int snippetLengthInCharacters,
			int snippetNumber) {
		String readableWikiPage = getReadableText();
		String searchText = readableWikiPage.toLowerCase();
		String[] queryWords = query.toLowerCase().split("\\s+");
		
		int bestSnippetPosition = 0;
		int bestScore = -1;
		
		for (int positionOfSnippet = 0; 
			 positionOfSnippet < searchText.length() - snippetLengthInCharacters; 
			 positionOfSnippet ++) {
			int score = 0;
			String snippetText = searchText.substring(positionOfSnippet, positionOfSnippet + snippetLengthInCharacters);
			for (String word : queryWords) {
				// Find occurrences of substring in string thanks to 
				// http://stackoverflow.com/questions/767759/occurrences-of-substring-in-a-string
				int lastIndex = 0;
				while(lastIndex != -1){
					lastIndex = snippetText.indexOf(word, lastIndex);
					if( lastIndex != -1){
						score ++;
						lastIndex += word.length();
					}
				}
				if (score > bestScore) {
					bestSnippetPosition = positionOfSnippet;
				}
			}
		}
		String snippet = readableWikiPage.substring(bestSnippetPosition, 
				bestSnippetPosition + snippetLengthInCharacters);
		return "***** " + snippetNumber + "." + this.getTitle() + " *****\n" + snippet;
	}

	private String getReadableText() {
		String text = cleanUpWikiText();
		//    in den [[Exposition (Literatur)|Expositionen]] vieler
		// => m in den Expositionen vieler
		text = text.replaceAll("\\[\\[[^\\[\\]]*\\||\\]\\]", "");
		//    en [[Bernard Herrmann zus
		// => en Bernard Herrmann zus
		text = text.replaceAll("\\[\\[\\{\\{", "");
		//    {{Zitat|Bei der üblichen Form von Suspense ist es unerlässlich, 
		// => Bei der üblichen Form von Suspense ist es unerlässlich, 
		text = text.replaceAll("\\{\\{[^\\{\\}]*\\||\\}\\}", "");
		//    ===== Tricktechnik =====
		//    Tricktechnik
		text = text.replaceAll("=+", "");
		//    &lt;
		// => 
		text = text.replaceAll("&[^;]{1,4};", "");
		
		//    replace multiple whitespaces by a whitespace
		text = text.replaceAll("\\s+", " ");
		return text;
	}

	public double getScore() {
		return this.score;
	}

	public void setScore(double result) {
		this.score = result;
	}

	public List<String> getLinks() {
		// synchronize with MyQuery.setQuery
		List<String> links = new ArrayList<String>();
		// from
		// http://stackoverflow.com/questions/6020384/create-array-of-regex-matches
		Matcher m = Pattern.compile(
				"\\[\\[\\s*([^\\]\\|#]*)\\s*([\\|#][^\\]]*\\s*)?\\]\\]")
				.matcher(getText());
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

	public Double BM25(Index index, List<Term> queryTerms) throws IOException {
		if (BM25Index == index && BM25QueryTerms == queryTerms) {
			return BM25;
		}
		BM25Index = index;
		BM25QueryTerms = queryTerms;
		BM25 = new Double(new BM25(index, queryTerms, this).compute());
		return BM25;
	}

}
