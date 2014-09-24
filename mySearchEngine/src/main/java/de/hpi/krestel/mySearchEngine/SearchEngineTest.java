package de.hpi.krestel.mySearchEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;


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
//		basicPath = "res/wiki.xml"; // comment this out/in
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.10.xml";
//		basicPath = "res/dewiki-20140216-pages-articles-multistream.100.xml";
//		basicPath = "res/dewiki-20140216-pages-articles-multistream.1000.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.10000.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.100000.xml";
		
		String filePath = new File(basicPath).getAbsolutePath();
		SearchEngineY test = new SearchEngineY();
		if (!test.loadIndex(filePath)) {
			System.out.println("Creating index...");
			test.index(filePath);
			System.out.println("Created index!");
		}
		if (!test.loadIndex(filePath)) {
			throw new AssertionError("Index should be loaded.");
		};
		
		System.out.println("Searching Terms...");

		searchTitles("08/15", test);
		/*
		 * test queries:
					"ein trauriges Arschloch"
					Toskana AND Wein 
					sülz* AND staatlich
					öffentlicher nahverkehr stadtpiraten
					schnitzel AND kaffe BUT NOT schwein*
					Dr. No
					ICE BUT NOT T
					Bierzelt Oktoberfest
					Los Angeles sport
					08/15
		 */

		System.out.println("Searched Terms!");
	}

	private static void searchTitles(String query, SearchEngineY test) throws IOException, XMLStreamException {
		System.out.println("---------------------- " + query + " ----------------------");

		SearchResult searchResult = test.searchWikiPages(query, prf, topK);
		
		double ndcg = searchResult.computeNDCG();
		System.out.println("ndcg@"+topK + " : " + ndcg);
	
		ArrayList<String> snippetsList = searchResult.makeSnippets();
		for(int i = 0;i<snippetsList.size();i++)
		{
			System.out.println(snippetsList.get(i));
		}
	
	}

	@SuppressWarnings("unused")
	private static void evaluate(SearchEngine se) throws IOException {
		se.indexWrapper();
		for(int i=0;i<SearchEngineTest.queries.length;i++){
			// Search and store results
			se.searchWrapper(queries[i], topK, prf);
		}		
	}

}
