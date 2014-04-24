package de.hpi.krestel.mySearchEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.germanStemmer;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;
import de.hpi.krestel.mySearchEngine.parser.ParseHTMLToText;
import de.hpi.krestel.mySearchEngine.parser.ParseWikiToHTMLUtility;
import de.hpi.krestel.mySearchEngine.parser.ReadXMLFile;

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
		List<WikiPage> listPages = parseXML.getWikiPages();
		//get Stopwords
		StopWord stopWord = new StopWord();        
        stopWord.FillStopWord(new File("res/stop-words_german_1_de.txt").getAbsolutePath());
        stopWord.FillStopWord(new File("res/stop-words_german_2_de.txt").getAbsolutePath());        
        int minLength = 3;
		SnowballStemmer stemmer = new germanStemmer();			
		ParseHTMLToText htmlParser = new ParseHTMLToText();
		for (WikiPage page: listPages) {				
			List<String> listTerms = new ArrayList<String>();							       	        //		
			String html = ParseWikiToHTMLUtility.parseMediaWiki(page.getText());
			for (String strWord : htmlParser.parseHTML(html).toString().replaceAll("[^a-zA-Z ]", "").split("\\s+")) {
				//filter by stopword and length
				if (strWord.length() < minLength || stopWord.GetHashSet().contains(strWord))
					continue;
				stemmer.setCurrent(strWord); //stemword				
				stemmer.stem();										
				listTerms.add(stemmer.getCurrent());				
			}					
			System.out.println(listTerms.size());
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
