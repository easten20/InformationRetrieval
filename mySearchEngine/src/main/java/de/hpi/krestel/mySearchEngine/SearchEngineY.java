package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	void index(String wikipediaFilePath) {
		try {
			// First you need to pre-process the raw input. Decide on how to tokenize, whether to
			// use a stopword list and/or stemming. For these steps you can use existing code — you
			// don’t need to come up with a stopword list or implement a new stemmer!
			Index index = new Index(wikipediaFilePath);
			for (WikiPage wikiPage: listWikiPages(wikipediaFilePath, index)) {						
				Iterable<String> tokens = wikiPage.asTokens();
				index.add(wikiPage, tokens);
			}		
				index.save();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
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
	
	public ArrayList<String> searchTitles(String query) throws IOException, XMLStreamException {
		ArrayList<String> titles = new ArrayList<String>();
		for (WikiPage wikiPage : searchWikiPages(query)) {
			titles.add(wikiPage.getTitle());
		}
		return titles;
	}
	
	public int checkRelevance (String title, ArrayList <String> goldenStandard){
		int i = 0;
		for (String goldenTitle : goldenStandard){
			i++;
			if (title.equals(goldenTitle))
				return i;
		}
		return -1;
	}
	
	public double calculateRelevance (int rankNumber ){
		double relevance = 0;
		
			relevance= 1+ Math.floor(10*Math.pow(0.5, 0.1*rankNumber));
			return relevance;
		
	}
	
	public ArrayList<String> searchTitles (String query, int prf, int topK, ArrayList<WikiPage> pages)throws IOException, XMLStreamException {
		ArrayList<String> titles = new ArrayList<String>();
		String newQuery = query;
		int flag=0;
		for (WikiPage wikiPage : searchWikiPages(query)) {
			if (flag>= prf){
				break;
			}
			String frequentWord = wikiPage.mostFrequentWord();
			newQuery+= " " + frequentWord;
			flag++;
			pages.add(wikiPage);
		}
		
		titles = searchTitles(newQuery);
		return titles;

	}

	ArrayList<Double> computeDG (ArrayList<Double> gains){
		ArrayList<Double> dg = new ArrayList<Double>();
		int size = gains.size();
		
		for (int i=0; i<size; i++){
			
			
			if (i==0){
				dg.add(gains.get(i));
			}else{
				
				double temp = Math.log(i+1)/ Math.log (2);
				double value = gains.get(i)/temp;
				
				dg.add(value);
			}
			
		}
		return dg;
	}
	
	ArrayList<Double> computeDCG (ArrayList<Double> dg){
		ArrayList<Double> dcg = new ArrayList<Double>(); 
		double tmp =0;
		for (double gain :dg ){
			tmp = tmp +gain;
			dcg.add(tmp);
				
		}
		return dcg;
	}
	
	ArrayList<Double> computeNDCG (ArrayList<Double> dcg, ArrayList<Double> dcgNorm){
		
		int j = dcgNorm.size();
		ArrayList<Double> ndcg = new ArrayList<Double>();
		for (int i= 0; i<j; i++){
			if (dcgNorm.get(i) == 0.0 || Double.isNaN(dcgNorm.get(i))){
				double ndcgValue = 0;
				ndcg.add(ndcgValue);
			}else{
				
				double ndcgValue = dcg.get(i)/dcgNorm.get(i);
				ndcg.add(ndcgValue);
			}
			
		}
		
		return ndcg;
	}
	
	@Override
	Double computeNdcg(ArrayList<String> goldRanking,ArrayList<String> myRanking, int at) {
		ArrayList<Double> myRankingRelevance = new ArrayList<Double> ();
		
		//add relevance values to myRanking
		int size = goldRanking.size();
		boolean flag = false;
		for (String title : myRanking){
			
			for (int i=0; i<size; i++){
				if (goldRanking.get(i).equals(title)){
					
					//debug
					//System.out.println("hit!!");
					
					double gain = calculateRelevance(i);
					myRankingRelevance.add(gain);
					flag = true;
					break;
				}
			}
			if (!flag) myRankingRelevance.add((double) 0);	
			flag=false;
		}
		int rankSize = myRankingRelevance.size();
		if (rankSize < at){
			int i = at -rankSize;
			for (int j = 0; j< i; j++)
				myRankingRelevance.add((double) 0);
		}
				
		ArrayList<Double> dg = computeDG(myRankingRelevance);
		ArrayList<Double> dcg = computeDCG(dg); 

//		
//		Collections.sort(myRankingRelevance);
//		Collections.reverse(myRankingRelevance);
		
		
//		ArrayList<Double> dgNorm = computeDG(myRankingRelevance);
//		
//		ArrayList<Double> dcgNorm = computeDCG(dgNorm); 
		ArrayList<Double> ndcg = computeNDCG(dcg, myRankingRelevance);
		
		// TODO Auto-generated method stub
		return ndcg.get(at-1);
	}			
	

}
