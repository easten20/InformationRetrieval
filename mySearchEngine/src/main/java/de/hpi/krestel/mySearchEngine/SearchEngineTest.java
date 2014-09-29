package de.hpi.krestel.mySearchEngine;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
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
	static String[] queries = {"artikel", "deutsch"};
	
	// some variables (will be explained when needed, ignore for now!)
	static int topK = 20;
	static int prf = 5;

	public static void main(String[] args) throws IOException, XMLStreamException {		
		String basicPath = "res/dewiki-20140216-pages-articles-multistream.xml";
//		basicPath = "res/wiki.xml"; // comment this out/in
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.10.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.100.xml";
		basicPath = "res/dewiki-20140216-pages-articles-multistream.1000.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.10000.xml";
		//basicPath = "res/dewiki-20140216-pages-articles-multistream.100000.xml";
		
		String filePath = new File(basicPath).getAbsolutePath();
		SearchEngineY test = new SearchEngineY();
		if (!test.loadIndex(filePath)) {
			final long startTime = System.currentTimeMillis();
			System.out.println("Creating index...");
			test.index(filePath);
			System.out.println("Created index!");			
			final long endTime = System.currentTimeMillis();
			System.out.println("Total execution time: " + (endTime - startTime) );
		}
		if (!test.loadIndex(filePath)) {
			throw new AssertionError("Index should be loaded.");
		};
		
		System.out.println("Searching Terms...");
//		searchTitles("LINKTO Kulturapfel", test); // should be Bodensee
//		searchTitles("LINKTO schnitzelmitkartoffelsalat", test);
//		searchTitles("Art* BUT NOT Artikel", test);
		//searchTitles("Soziologie", test);
		searchTitles("München", test);
		//searchTitles("München", test);
		//”Art* BUT NOT Artikel”
		//”Artikel OR Reaktion”
		//”Artikel AND Smithee”
		//”’Filmfestspiele in Venedig’”
		System.out.println("Searched Terms!");
		
		//debug
//		MySecondClass window = new MySecondClass();
//		window.appendToPane("test", Color.BLACK);
//        SwingUtilities.invokeLater(new Runnable()
//        {
//            public void run()
//            {
//            	MySecondClass window = new MySecondClass();
//            	window.appendToPane("flow", Color.ORANGE);
//            }
//        });
		//debug
	}

	private static void searchTitles(String query, SearchEngineY test) throws IOException, XMLStreamException {
		
		System.out.println("---------------------- " + query + " ----------------------");

		SearchResult searchResult = test.searchWikiPages(query, prf, topK);
		
		for (String title: searchResult.getTitles()){
			System.out.println(title);
		}

			
//		double ndcg = searchResult.computeNDCG();
//		System.out.println("ndcg@"+topK + " : " + ndcg);
//>>>>>>> .merge_file_d1BGvg
		

        MySecondClass window = new MySecondClass();
  
		
		ArrayList<String> snippetsList = searchResult.makeSnippets();
		
		for(int i = 0;i<snippetsList.size();i++)
		{
			System.out.println(snippetsList.get(i));
			//window.appendToPane(snippetsList.get(i), Color.BLACK);
			colorQueryTerm(window, snippetsList.get(i), query);
			
		}
			
	}
	
	private static void colorQueryTerm(MySecondClass window, String string, String query){
		int index = string.indexOf(query);
		window.appendToPane( string.substring(0, index), Color.BLACK);
		window.appendToPane( string.substring(index, index+query.length()), Color.RED);
		window.appendToPane( string.substring(index+query.length(), string.length()-1), Color.BLACK);
		window.appendToPane( "\n\n\n", Color.BLACK);
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
