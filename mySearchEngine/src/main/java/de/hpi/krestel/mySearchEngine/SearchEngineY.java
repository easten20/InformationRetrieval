package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;
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
	int resultSize = 10; // Nicco and Elina, we have to display different results. Change this size. Timur and I will use 10. 
	
	
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
			Iterable<String> tokens = wikiPage.asTokens();
			index.add(wikiPage, tokens);
		}
		index.save();
	}		
	
	Iterable<WikiPage> listWikiPages(String wikipediaFilePath, Index index) {
		WikiXMLIterable parser = new WikiXMLIterable(wikipediaFilePath);
		parser.setPosition(index.getlastPositionInXMLFile());
		return parser;
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
		return queryResult.wikiPagesMatchingQuery(5);		
	}		
	
	public List<String> searchTitles(String query) throws IOException, XMLStreamException {
		List<String> titles = new ArrayList<String>();
		for (WikiPage wikiPage : searchWikiPages(query)) {
			titles.add(wikiPage.getTitle());
		}
		return titles;
	}
	
//	public List<String> searchTitles (String query, int prf, int topK)throws IOException, XMLStreamException {
//		List<String> titles = new ArrayList<String>();
//		int flag=0;
//		for (WikiPage wikiPage : searchWikiPages(query)) {
//			if (flag>= prf){
//				break;
//			}
//			String frequentWord = wikiPage.mostFrequentWord();
//			query+= " " + frequentWord;
//			flag++;
//			//System.out.print("title: ");
//			//System.out.println(wikiPage.getTitle());
//			//System.out.print("text");
//			//System.out.println(wikiPage.getText());
//		}
//		
//		titles = searchTitles(query);
//		return titles;
//	}
	
	public void searchTitles (String query, int prf, int topK)throws IOException, XMLStreamException {
		List<String> titles = new ArrayList<String>();
		String newQuery = query;
		int flag=0;
		for (WikiPage wikiPage : searchWikiPages(query)) {
			if (flag>= prf){
				break;
			}
			String frequentWord = wikiPage.mostFrequentWord();
			newQuery+= " " + frequentWord;
			flag++;
			
			System.out.println("***** " + flag + "." + wikiPage.getTitle() + " *****");
			resultGenerate(query, wikiPage);
		}
		
		titles = searchTitles(newQuery);
		System.out.println(titles);

	}
	
	public void resultGenerate(String query, WikiPage wikiPage)
	{
		String entireText = wikiPage.getText();
		String key = null;
		int tot = 0;
		int queryPosition = 0;
		StringTokenizer stringTokenizer = new StringTokenizer(entireText, " ");
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
				System.out.print(key + " ");
			}
			
		}
		System.out.print("\n");
	}
	
	@Override
	Double computeNdcg(String query, ArrayList<String> ranking, int ndcgAt) {
	
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
