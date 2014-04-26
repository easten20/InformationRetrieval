package de.hpi.krestel.mySearchEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.germanStemmer;

import com.google.common.collect.Iterators;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;
import de.hpi.krestel.mySearchEngine.parser.ParseHTMLToText;
import de.hpi.krestel.mySearchEngine.parser.ParseWikiToHTMLUtility;
import de.hpi.krestel.mySearchEngine.parser.ReadXMLFile;
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
	
	// Replace 'Y' with your search engine name
	public SearchEngineY() {
		// This should stay as is! Don't add anything here!
		super();	
	}

	@Override
	void index(String dir) {
		// First you need to pre-process the raw input. Decide on how to tokenize, whether to
		// use a stopword list and/or stemming. For these steps you can use existing code — you
		// don’t need to come up with a stopword list or implement a new stemmer!
		for (WikiPage wikiPage: listWikiPages(dir)) {						
			String cleanText = cleanUpWikiText(wikiPage);
			Iterable<String> tokens = tokenizeWikiText(cleanText);
			tokens = removeStopWords(tokens);
			tokens = stemText(tokens);
			index(wikiPage, tokens);
		}
	}
	
	Iterable<WikiPage> listWikiPages(String dir) {
		WikiXMLIterable test = new WikiXMLIterable(dir);		
		return test;
	}
	
	String cleanUpWikiText(WikiPage wikiPage) {
		ParseHTMLToText htmlParser = new ParseHTMLToText();
		String html = ParseWikiToHTMLUtility.parseMediaWiki(wikiPage.getText());
		return htmlParser.parseHTML(html).toString();
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
	
	void index(WikiPage wikiPage, Iterable<String> tokens) {
		System.out.println("page Id: " + wikiPage.getId());
		System.out.println(((List<String>) tokens).size());
	}

	@Override
	boolean loadIndex(String directory) {
		// TODO Auto-generated method stub
		return false;
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
