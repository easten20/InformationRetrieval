package de.hpi.krestel.mySearchEngine;

import java.util.ArrayList;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.germanStemmer;

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
		ReadXMLFile parseXML = new ReadXMLFile(dir);	
		List<WikiPage> listPages = parseXML.pageL;
		SnowballStemmer stemmer = new germanStemmer();			
		for (WikiPage page: listPages) {			
			for (String strWord : page.text.split("\\s+")) {
				stemmer.setCurrent(strWord);			
				stemmer.stem();				
				System.out.println("stemmer: " + stemmer.getCurrent());				
			}												
		}		
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
