package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.germanStemmer;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;
import de.hpi.krestel.mySearchEngine.parser.ParseHTMLToText;
import de.hpi.krestel.mySearchEngine.parser.ParseWikiToHTMLUtility;
import de.hpi.krestel.mySearchEngine.parser.WikiXMLIterable;

/* This is your file! implement your search engine here!
 * 
 * Describe your search engine briefly:
 *  - multi-threaded?
 *  - stemming?
 *  - stopword removal?
 *  - index algorithm?
 *  - etc.  
 * 
 */

// Replace 'Y' with your search engine name
public class SearchEngineY extends SearchEngine {
	
	Index index;
	private StopWord cachedStopWord;
	private SnowballStemmer cachedStemmer;
	
	// Replace 'Y' with your search engine name
	public SearchEngineY() {
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
	void index(String wikipediaFilePath) throws IOException {
		// First you need to pre-process the raw input. Decide on how to tokenize, whether to
		// use a stopword list and/or stemming. For these steps you can use existing code — you
		// don’t need to come up with a stopword list or implement a new stemmer!
		Index index = new Index(wikipediaFilePath);
		for (WikiPage wikiPage: listWikiPages(wikipediaFilePath, index)) {						
			String cleanText = cleanUpWikiText(wikiPage);
			Iterable<String> tokens = preprocessText(cleanText);
			index.add(wikiPage, tokens);
		}
		index.save();
	}
	
	Iterable<String> preprocessText(String text) {
		Iterable<String> tokens = tokenizeWikiText(text);
		tokens = removeStopWords(tokens);
		tokens = stemText(tokens);
		return tokens;
	}
	
	Iterable<WikiPage> listWikiPages(String wikipediaFilePath, Index index) {
		WikiXMLIterable parser = new WikiXMLIterable(wikipediaFilePath);
		parser.setPosition(index.getlastPositionInXMLFile());
		return parser;
	}
	
	String cleanUpWikiText(WikiPage wikiPage) {
		ParseHTMLToText htmlParser = new ParseHTMLToText();
		String text = wikiPage.getText();
		String html = ParseWikiToHTMLUtility.parseMediaWiki(text);
		html = html.replaceFirst("<\\?[^>]*\\?>", "");
		String parsedHTML = htmlParser.parseHTML(html);
		return parsedHTML.toString();
	}
	
	Iterable<String> tokenizeWikiText(String wikiText) {
		// TODO: most unicode characters that are higher than 128 are just usual characters
		//       maybe we can split with this in  mind
		String[] tokens = wikiText.replaceAll("[^a-zA-Z ]", " ").split("\\s+");
		return Arrays.asList(tokens);
	}
	
	StopWord getStopWord() {
		if (cachedStopWord == null) {
			cachedStopWord = StopWord.StopWordFromFiles(); 
		}
		return cachedStopWord;
	}
	
	Iterable<String> removeStopWords(Iterable<String> tokens) {
		StopWord stopWord = getStopWord();
		List<String> stopWordFreeTokens = new ArrayList<String>();
		for (String token : tokens) {
			if (token.length() < 3 || stopWord.GetHashSet().contains(token))
				continue;
			stopWordFreeTokens.add(token);
		}
		return stopWordFreeTokens;
	}
	
	
	SnowballStemmer getStemmer() {
		if (cachedStemmer == null) {
			cachedStemmer = new germanStemmer();
		}
		return cachedStemmer;
	}
	
	Iterable<String> stemText(Iterable<String> tokens) {
		SnowballStemmer stemmer  = this.getStemmer();
		List<String> stemmedTokens = new ArrayList<String>();
		for (String token : tokens) {
			stemmer.setCurrent(token); 		
			stemmer.stem();
			token = stemmer.getCurrent();
			// remove the s at the end
			token = token.replaceAll("s+$", "");
			stemmedTokens.add(token);
		}
		return stemmedTokens;
	}


	@Override
	boolean loadIndex(String wikipediaFilePath) {
		index = new Index(wikipediaFilePath);
		return index.isValid();
	}

	@Override
	ArrayList<String> search(String query, int topK, int prf) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<WikiPage> searchWikiPages(String query) throws IOException, XMLStreamException {
		assert index.isValid();
		
		//check if query contains "and"
		Iterable<String> queryTokens = preprocessText(query);
		
		//queryTokens.add("and");
		
		return index.wikiPagesMatchingQuery(queryTokens);
	}
	
	public List<String> searchTitles(String query) throws IOException, XMLStreamException {
		List<String> titles = new ArrayList<String>();
		for (WikiPage wikiPage : searchWikiPages(query)) {
			titles.add(wikiPage.getTitle());
		}
		return titles;
	}
	
	@Override
	Double computeNdcg(String query, ArrayList<String> ranking, int ndcgAt) {
	
		// TODO Auto-generated method stub
		return null;
	}

}
