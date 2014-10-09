package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;

public class SearchResult {
	private String query;
	private int resultSize = 10;
	private ArrayList<String> titles;
	private List<WikiPage> resultPages;
	private SearchEngineIR searchEngineY;
	private int topK;

	public SearchResult(String query, int prf, SearchEngineIR test, List<WikiPage> pages, int topK) {
		this.query = query;
		this.searchEngineY = test;
		this.resultPages = pages;
		this.topK = topK;
	}
	
	public SearchResult() {
		this.resultPages = new ArrayList<WikiPage>();
	}
	
	public int getResultPagesSize() {		
		if ( resultPages != null ) {
			return resultPages.size();
		}
		return 0;
	}

	public ArrayList<String> makeSnippets() throws IOException, XMLStreamException {
		
		int numberOfSnippet = 1;
		ArrayList<String> snippetsList = new ArrayList<String>();

		for (WikiPage wikiPage : resultPages) {
			snippetsList.add(wikiPage.generateSnippet2(query, resultSize, numberOfSnippet));
			numberOfSnippet ++;
			//debug
			System.out.println(snippetsList.get(snippetsList.size()-1));
		}

		return snippetsList;
	}
	
	public double computeNDCG (){
		ArrayList <String> goldenList = this.searchEngineY.getGoldRanking(query);
		
		//print goldenList
		/*
		System.out.println("+++goldenList+++");
		for ( int i = 0 ; i < goldenList.size() ; i ++ ) {
			System.out.println( i + goldenList.get(i) );
		}
		*/
		return this.searchEngineY.computeNdcg(goldenList, getTitles(), this.topK);
	}
	
	ArrayList<String> getTitles() {
		ArrayList<String> titles = new ArrayList<String>();
		for (WikiPage wikiPage : resultPages) {
			titles.add(wikiPage.getTitle());
		}						
		return titles;
	}
	
	
}
