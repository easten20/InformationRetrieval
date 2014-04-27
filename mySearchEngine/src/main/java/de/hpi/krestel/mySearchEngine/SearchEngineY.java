package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		for (WikiPage wikiPage: listWikiPages(wikipediaFilePath)) {						
			String cleanText = cleanUpWikiText(wikiPage);
			Iterable<String> tokens = tokenizeWikiText(cleanText);
			tokens = removeStopWords(tokens);
			tokens = stemText(tokens);
			index.add(wikiPage, tokens);
		}
		index.save();
	}
	
	Iterable<WikiPage> listWikiPages(String wikipediaFilePath) {
		return new WikiXMLIterable(wikipediaFilePath);
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
		String[] tokens = wikiText.replaceAll("[^a-zA-Z ]", "").split("\\s+");
		return Arrays.asList(tokens);
	}
	
	StopWord getStopWord() {
		// TODO: cache the result, do not create a new one every time. 
		return StopWord.StopWordFromFiles();
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
		// TODO: cache the stemmer for better performance
		return new germanStemmer();
	}
	
	Iterable<String> stemText(Iterable<String> tokens) {
		SnowballStemmer stemmer  = this.getStemmer();
		List<String> stemmedTokens = new ArrayList<String>();
		for (String token : tokens) {
			stemmer.setCurrent(token); 		
			stemmer.stem();
			stemmedTokens.add(stemmer.getCurrent());
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
	
	@Override
	Double computeNdcg(String query, ArrayList<String> ranking, int ndcgAt) {
	
		// TODO Auto-generated method stub
		return null;
	}
}
