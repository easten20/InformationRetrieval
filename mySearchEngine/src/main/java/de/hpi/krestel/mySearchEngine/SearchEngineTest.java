package de.hpi.krestel.mySearchEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;


// This file will be used to evaluate your search engine!
// You can use/change this file for development. But
// any changes you make here will be ignored for the final test!

// You can use and are encouraged to use multi-threading, map-reduce, etc for
// indexing and/or searching
// The final evaluation will be done with 2GB RAM (java -Xmx2g) !!!!!!!!!!

public class SearchEngineTest {

	// Some test queries for development. The real test queries will be more difficult ;) 
	static String[] queries = {"artikel", "deutsch"};
	
	// some variables (will be explained when needed, ignore for now!)
	static int topK = 10;
	static int prf = 5;

	public static void main(String[] args) throws IOException, XMLStreamException {
		String basicPath = "res/dewiki-20140216-pages-articles-multistream.xml";
		basicPath = "res/wiki.xml"; // comment this out/in
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.10.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.100.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.1000.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.10000.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.100000.xml";
		
		String filePath = new File(basicPath).getAbsolutePath();
		SearchEngineY test = new SearchEngineY();
		if (!test.loadIndex(filePath)) {
			System.out.println("Creating index...");
			test.index(filePath);
		}
		assert test.loadIndex(filePath);
					
		System.out.println("Created index!");
		System.out.println("Searching Terms...");
		searchTitles("artikel", test);
		searchTitles("deutsch", test);
		System.out.println("Searched Terms!");
	}

	private static void searchTitles(String query, SearchEngineY test) throws IOException, XMLStreamException {
		System.out.println("---------------------- " + query + " ----------------------");
		System.out.println(test.searchTitles(query, prf, topK));
		
		SearchResult searchResult = new SearchResult(query, prf, test.searchTitles(query, prf, topK), test);
		
		ArrayList<String> snippetsList = new ArrayList<String>();
		snippetsList = searchResult.makeSnippets();
		
		for(int i = 0;i<snippetsList.size();i++)
		{
			System.out.println(snippetsList.get(i));
		}
	
	}

	@SuppressWarnings("unused")
	private static void evaluate(SearchEngine se) throws IOException {
		
		// Load or generate the index
		se.indexWrapper();

		for(int i=0;i<SearchEngineTest.queries.length;i++){
			// Search and store results
			se.searchWrapper(queries[i], topK, prf);
		}		
	}

}
