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
	private ArrayList<WikiPage> resultPages;
	private SearchEngineY searchEngineY;
	
	public SearchResult(String query, int prf, ArrayList<String> titles, SearchEngineY test, ArrayList<WikiPage> pages){		
		this.query = query;
		this.titles = titles;
		this.searchEngineY = test;
		this.resultPages = pages;
	}
	
	public ArrayList<String> makeSnippets() throws IOException, XMLStreamException{
		int flag = 1;
		
		ArrayList<String> snippetsList = new ArrayList<String>();
		
	
		for(WikiPage wikiPage : resultPages)
		{
//			//debug
//			System.out.println("wikiPage.title: " + wikiPage.getTitle());
	
			if(titles.size() == 0) break;
			
			for( int i = 0 ; i < titles.size(); i++)
			{
//				System.out.println(titles.get(i));
//				System.out.println((wikiPage.getTitle()));
				if(titles.get(i).equals(wikiPage.getTitle()))
				{
					//debug
					//System.out.println("titles.size: " + titles.size());
					
					snippetsList.add(wikiPage.generateSnippet(query, resultSize, flag));
					titles.remove(i);
					flag++;
					break;
				}//end if
			}//end for
		}//end for
		
		return snippetsList;
		
	}//end makeSnippets()
}


