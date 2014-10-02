package de.hpi.krestel.mySearchEngine;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
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
	static String[] queries = {"'ein trauriges Arschloch'",
							"Toskana AND Wein", 
							"sülz* AND staatlich",
							"öffentlicher nahverkehr stadtpiraten",
							"schnitzel AND kaffee BUT NOT schwein*",
							"Dr. No",
							"ICE BUT NOT T",
							"Bierzelt Oktoberfest",
							"Los Angeles sport",
							"08/15"};
	//static String[] queries = { "adolf hitler", "Bierzelt Oktoberfest" };
	static String basicPath = "res/dewiki-20140216-pages-articles-multistream.xml";
	
	// some variables (will be explained when needed, ignore for now!)
	static int topK = 10;
	static int prf = 5;

	public static void main(String[] args) throws IOException, XMLStreamException {		
		//String basicPath = "res/dewiki-20140216-pages-articles-multistream.xml";
		//basicPath = "res/wiki.xml"; // comment this out/in
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.10.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.100.xml";
		basicPath = "res/dewiki-20140216-pages-articles-multistream.1000.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.10000.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.100000.xml";
		
		String filePath = new File(basicPath).getAbsolutePath();
		SearchEngineIR mySearchEngine = new SearchEngineIR();
		if (!mySearchEngine.loadIndex(filePath)) {
			final long startTime = System.currentTimeMillis();
			System.out.println("Creating index...");
			mySearchEngine.index(filePath);
			System.out.println("Created index!");			
			final long endTime = System.currentTimeMillis();
			System.out.println("Total execution time: " + (endTime - startTime) );
		}
		if (!mySearchEngine.loadIndex(filePath)) {
			throw new AssertionError("Index should be loaded.");
		};
				
		System.out.println("Searching Terms...");
		//MySecondClass window = new MySecondClass();
		for (String query : queries){
			searchTitles(query, mySearchEngine);
		}
		//searchTitles("ICE BUT NOT TTT", test);
		System.out.println("Searched Terms!");
	}

	private static void searchTitles(String query, SearchEngineIR test) throws IOException, XMLStreamException {
		//SearchResult searchResult = null;
		//try {
		
			System.out.println("---------------------- " + query + " ----------------------");
			//printInWindow(window, "---------------------- " + query + " ----------------------");

			SearchResult searchResult = test.searchWikiPages(query, prf, topK);		
			
			for (String title: searchResult.getTitles()){
				System.out.println(title);
				//printInWindow(window, title);
			}
		
		
		//double ndcg = searchResult.computeNDCG();
		//System.out.println("ndcg@"+topK + " : " + ndcg);
		//printInWindow(window, "ndcg@"+topK + " : " + ndcg);
//		}
//		catch (Exception ex) {
//			System.out.println(ex.getMessage());
//		}	
		
		if ( searchResult.getResultPagesSize() != 0 ) {
		
			ArrayList<String> snippetsList = searchResult.makeSnippets();
			for(int i = 0;i<snippetsList.size();i++)
			{
				System.out.println(snippetsList.get(i));
				//colorQueryTerm(window, snippetsList.get(i), query);
			}
		}
		
	}
	
	private static void printInWindow(MySecondClass window, String string){
		window.appendToPane(string, Color.BLACK);
		window.appendToPane("\n", Color.BLACK);
	}
	
	private static void colorQueryTerm(MySecondClass window, String string, String query){
		System.out.println("string: " + string + ", query: " + query);
		if( string.toLowerCase().contains(query.toLowerCase())){
			System.out.println("string contains query");
			int index = string.toLowerCase().indexOf(query.toLowerCase());
			window.appendToPane( string.substring(0, index), Color.BLACK);
			window.appendToPane( string.substring(index, index+query.length()), Color.RED);
			window.appendToPane( string.substring(index+query.length(), string.length()-1), Color.BLACK);
			window.appendToPane( "\n\n\n", Color.BLACK);
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
