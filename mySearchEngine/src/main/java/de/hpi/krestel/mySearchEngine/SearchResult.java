package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;

public class SearchResult {
	private String query;
	private int resultSize = 100;
	private ArrayList<String> titles;
	private List<WikiPage> resultPages;
	private SearchEngineY searchEngineY;
	private int topK;

	public SearchResult(String query, int prf, SearchEngineY test, List<WikiPage> pages, int topK) {
		this.query = query;
		this.searchEngineY = test;
		this.resultPages = pages;
		this.topK = topK;
	}

	public ArrayList<String> makeSnippets() throws IOException, XMLStreamException {
		
		int numberOfSnippet = 1;
		ArrayList<String> snippetsList = new ArrayList<String>();

		for (WikiPage wikiPage : resultPages) {
			snippetsList.add(wikiPage.generateSnippet(query, resultSize, numberOfSnippet));
			numberOfSnippet ++;
		}

		return snippetsList;
	}
	
	public double computeNDCG (){
		ArrayList <String> goldenList = this.searchEngineY.getGoldRanking(query);
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
