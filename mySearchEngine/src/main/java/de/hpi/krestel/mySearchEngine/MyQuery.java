package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.xml.stream.XMLStreamException;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.germanStemmer;

import de.hpi.krestel.mySearchEngine.domain.BooleanClause;
import de.hpi.krestel.mySearchEngine.domain.BooleanOp;
import de.hpi.krestel.mySearchEngine.domain.BooleanQuery;
import de.hpi.krestel.mySearchEngine.domain.Term;
import de.hpi.krestel.mySearchEngine.domain.WikiPage;

public class MyQuery {
		
	private BooleanQuery booleanQuery;
	private Index index;	
	private Set<Long> docPositionST;	
	
	public MyQuery(Index index){		
		this.index = index;
		this.docPositionST = new TreeSet<Long>(); 
	}										
	
	public void setQuery(String queryText){		
		String andOp = "and";
		String orOp = "or";
		String butnotOP = "butnot";		     
		//now stringList = [fish*, and, *tropical] or [fish*, butnot, *tropical]		         		
		//in case of "but not", [fish, but, not, tropical] -> [fish, butnot, tropical]		
		queryText = queryText.toLowerCase().replace("but not", butnotOP);
		
		ArrayList<String> stringList = this.tokenizeQuery(queryText);			         		         		    		
		this.booleanQuery = new BooleanQuery();
		for (int i = 0 ; i < stringList.size(); i++){
			if (i < (stringList.size()-1)) {
				if(stringList.get(i+1).equals(andOp)) { // try to get operator
					this.booleanQuery.addClause(new BooleanClause(new Term(stringList.get(i)), BooleanOp.MUST));// after found and add term before to it
					this.booleanQuery.addClause(new BooleanClause(new Term(stringList.get(i+2)), BooleanOp.MUST)); // after found and add term next to it
					i = i+2;
				}
				else if(stringList.get(i+1).equals(orOp)) { // try to get operator
					this.booleanQuery.addClause(new BooleanClause(new Term(stringList.get(i)), BooleanOp.SHOULD)); // after found and add term before to it
					this.booleanQuery.addClause(new BooleanClause(new Term(stringList.get(i+2)), BooleanOp.SHOULD)); // after found and add term next to it
					i = i+2;
				}
				else if(stringList.get(i+1).equals(butnotOP)) { // try to get operator
					this.booleanQuery.addClause(new BooleanClause(new Term(stringList.get(i)), BooleanOp.SHOULD)); // after found and add term next to it
					this.booleanQuery.addClause(new BooleanClause(new Term(stringList.get(i+2)), BooleanOp.MUSTNOT)); // after found and add term next to it
					i = i+2;
				}		        		
				else {
					this.booleanQuery.addClause(new BooleanClause(new Term(stringList.get(i)), BooleanOp.SHOULD));		        			 
				}
			}			
			else {
				this.booleanQuery.addClause(new BooleanClause(new Term(stringList.get(i)), BooleanOp.SHOULD));		        			 
			}
		}		         		         		         

	}	
	
	public List<WikiPage> wikiPagesMatchingQuery() throws IOException, XMLStreamException {
		List<WikiPage> wikiPages = new ArrayList<WikiPage>();			
		this.fillDocumentPositions(this.booleanQuery);
		for (Long docPosition : this.docPositionST){
			WikiPage wikiPage = WikiPage.from(this.index.getXMLFilePath(), docPosition);
			wikiPages.add(wikiPage);
		}		
		return wikiPages;
	}	
	
	private ArrayList<String> tokenizeQuery(String inputQuery) {
		ArrayList<String> stringList = new ArrayList<String>();
		StopWord stopWord = StopWord.StopWordFromFiles();
		StringTokenizer queryTokenizer = new StringTokenizer(inputQuery," ");
        SnowballStemmer stemmer = new germanStemmer(); 
         while (queryTokenizer.hasMoreTokens()){
             String token = queryTokenizer.nextToken().toLowerCase();
             if (!stopWord.GetHashSet().contains(token)){
            	 stemmer.setCurrent(token);
            	 stemmer.stem();
            	 stringList.add(stemmer.getCurrent());		             
             }
         }
         return stringList;
	}		
	
	private void fillDocumentPositions(BooleanQuery boolQuery) throws IOException, XMLStreamException{
		List<Long> docPosL;
		for(BooleanClause clause: boolQuery.getBooleanClauses()) {		
			switch (clause.getBoolOp()){
			case SHOULD:
				docPosL = this.index.fileIndex().findDocumentPositionsInXMLFile(clause.getTerm());
				this.docPositionST.addAll(docPosL);				
				break;
			case MUST:
				docPosL = this.index.fileIndex().findDocumentPositionsInXMLFile(clause.getTerm());
				this.docPositionST.addAll(docPosL);
				this.docPositionST.retainAll(docPosL);
				break;
			case MUSTNOT:
				docPosL = this.index.fileIndex().findDocumentPositionsInXMLFile(clause.getTerm());
				this.docPositionST.removeAll(docPosL);
				break;				
			}		
		}
	}
}
