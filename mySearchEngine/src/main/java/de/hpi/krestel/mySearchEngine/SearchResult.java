package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.util.ArrayList;


import java.util.List;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;

public class SearchResult {
	private String query;
	private int resultSize = 10;
	private ArrayList<String> titles;
	private SearchEngineY searchEngineY;
	
	public SearchResult(String query, int prf, ArrayList<String> titles, SearchEngineY test){		
		this.query = query;
		this.titles = titles;
		this.searchEngineY = test;
	}
	
	public ArrayList<String> makeSnippets() throws IOException, XMLStreamException{
		int flag = 1;
		
		ArrayList<String> snippetsList = new ArrayList<String>();
		
		//debug
		System.out.println("titles: " + titles.toString());
	
		for(WikiPage wikiPage : searchEngineY.searchWikiPages(query))
		{
			//debug
			System.out.println("wikiPage.title: " + wikiPage.getTitle());
	
			if(titles.size() == 0) break;
			
			for( int i = 0 ; i < titles.size(); i++)
			{
				if(titles.get(i).equals(wikiPage.getTitle()))
				{
					//debug
					//System.out.println("titles.size: " + titles.size());
					
					snippetsList.add(wikiPage.resultGenerate(query, wikiPage, resultSize, flag));
					titles.remove(i);
					flag++;
					break;
				}//end if
			}//end for
		}//end for
		
		return snippetsList;
		
	}//end makeSnippets()
}


