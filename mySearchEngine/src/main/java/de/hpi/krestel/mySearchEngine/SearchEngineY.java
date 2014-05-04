package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

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
	TokenStream token;
	
	// Replace 'Y' with your search engine name
	public SearchEngineY() {
		// This should stay as is! Don't add anything here!
		super();
		token = new TokenStream();
	}

	@Override
	void index(String wikipediaFilePath) throws IOException {
		// First you need to pre-process the raw input. Decide on how to tokenize, whether to
		// use a stopword list and/or stemming. For these steps you can use existing code — you
		// don’t need to come up with a stopword list or implement a new stemmer!
		Index index = new Index(wikipediaFilePath);
		for (WikiPage wikiPage: listWikiPages(wikipediaFilePath, index)) {						
			String cleanText = cleanUpWikiText(wikiPage);
			Iterable<String> tokens = this.token.preprocessText(cleanText);
			index.add(wikiPage, tokens);
		}
		index.save();
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
		MyQuery queryResult = new MyQuery(this.index);		
		queryResult.setQuery(query);		
		return queryResult.wikiPagesMatchingQuery();		
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
