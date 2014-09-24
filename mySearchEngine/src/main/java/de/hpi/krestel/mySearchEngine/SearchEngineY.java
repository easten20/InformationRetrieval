package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
				index.add(wikiPage);
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
	
	public List<WikiPage> searchWikiPages(String query, int numberOfWikiPages) throws IOException, XMLStreamException {
		assert index.isValid();				
		MyQuery queryResult = new MyQuery(this.index);		
		queryResult.setQuery(query);		
		return queryResult.wikiPagesMatchingQuery(numberOfWikiPages);	// TODO: prf	
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
	
	public String pseudoRelevaceFeedback (String query, int prf) throws IOException, XMLStreamException{
		String newQuery = query;
		for (WikiPage wikiPage : searchWikiPages(query, prf)) {
			newQuery+= " " + wikiPage.mostFrequentWord();
		}
		return newQuery;
		
	}
	
	public SearchResult searchWikiPages (String query, int prf, int topK)throws IOException, XMLStreamException {
		String newQuery = pseudoRelevaceFeedback(query, prf);
		
		return new SearchResult(query, prf, this, searchWikiPages(newQuery, topK), topK) ;
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
	Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int at) {

		double dcg = 0.0;
		double idcg = 0.0;
		int rank=1;
		Iterator<String> iter = ranking.iterator();
		while(rank<=at){
			if(rank==1) idcg += 1+Math.floor(10 * Math.pow(0.5,0.1*rank));
			else idcg += 1+Math.floor(10 * Math.pow(0.5,0.1*rank))/Math.log(rank);
			if(iter.hasNext()){
				//change to get the titles of your ranking
				String title = iter.next().trim();
				int origRank = goldRanking.indexOf(title)+1;
				if(origRank<1){
					rank++;
					continue;
				}
				if(rank==1){
					dcg += 1+Math.floor(10 * Math.pow(0.5,0.1*origRank));
					rank++;
					continue;
				} 
				dcg += 1+Math.floor(10 * Math.pow(0.5,0.1*origRank))/Math.log(rank);
			}
			rank++;
		}
		return dcg/idcg;
	}	
	

}
