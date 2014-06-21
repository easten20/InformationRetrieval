package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.domain.BooleanClause;
import de.hpi.krestel.mySearchEngine.domain.BooleanImpl;
import de.hpi.krestel.mySearchEngine.domain.BooleanOp;
import de.hpi.krestel.mySearchEngine.domain.BooleanQuery;
import de.hpi.krestel.mySearchEngine.domain.PhraseClause;
import de.hpi.krestel.mySearchEngine.domain.Term;
import de.hpi.krestel.mySearchEngine.domain.WikiPage;

public class MyQuery {
		
	private BooleanQuery booleanQuery;	
	private Index index;	
	private Set<Long> docPositionST;
	private List<String> queryTokens;	
	
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
		//thks to http://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
		Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
		Matcher regexMatcher = regex.matcher(queryText);		
		String operator = "";
		TokenStream token = new TokenStream();
		queryTokens = token.preprocessText(queryText);
		this.booleanQuery = new BooleanQuery();
		while (regexMatcher.find()) {
		    if (regexMatcher.group(1) != null || regexMatcher.group(2) != null) {
		        // Add double-quoted or single-quoted string without the quotes
		    	this.booleanQuery.addClause(new PhraseClause(token.preprocessText(regexMatcher.group())));		    			   
		    } else {
		        // Add unquoted word
		    	if (regexMatcher.group().equalsIgnoreCase(andOp) ||		    		
		    			regexMatcher.group().equalsIgnoreCase(orOp) ||
		    				regexMatcher.group().equalsIgnoreCase(butnotOP))
		    		operator = regexMatcher.group();
		    	else
		    		if (!token.isStopWord(regexMatcher.group()))
		    			this.booleanQuery.addClause(new BooleanClause(new Term(token.stemText(regexMatcher.group()))));
		    }
		} 		
							         		         		    	
		if (operator.equalsIgnoreCase(andOp)) {
			this.booleanQuery.getBooleanClauses().get(0).setBoolOp(BooleanOp.MUST);
			this.booleanQuery.getBooleanClauses().get(1).setBoolOp(BooleanOp.MUST);
		}
		else if (operator.equalsIgnoreCase(orOp)){
			this.booleanQuery.getBooleanClauses().get(0).setBoolOp(BooleanOp.SHOULD);
			this.booleanQuery.getBooleanClauses().get(1).setBoolOp(BooleanOp.SHOULD);
		}
		else if (operator.equalsIgnoreCase(butnotOP))
			this.booleanQuery.getBooleanClauses().get(1).setBoolOp(BooleanOp.MUSTNOT);		
	}	
	
	public List<Long> getDocumentPositions(PhraseClause phraseQuery) throws IOException, XMLStreamException{		
		List<Occurence> occurencesL = new ArrayList<Occurence>();				
		List<Long> docPosL = new ArrayList<Long>();
		for (Term term: phraseQuery.getTerms()) {			
			occurencesL.addAll(this.index.fileIndex().findDocuments(term.getText()));			
		}				
		Collections.sort(occurencesL);
		int termSize = phraseQuery.getTerms().size(); // combinated word's size
		String firstWord = phraseQuery.getTerms().get(0).getText(); //first word		
		String phraseText = phraseQuery.getPhraseText().replace(" ", "");
		for (int i = 0; i < occurencesL.size(); i++){
			if ((i + termSize) > occurencesL.size()) 
				break;
			else
				if (firstWord.compareTo(occurencesL.get(i).getWord()) == 0)
				{
					//just make sure the sequences in order
					if (termSize-1 != (occurencesL.get(i+termSize-1).getPositionOfWordInDocument() - occurencesL.get(i).getPositionOfWordInDocument()))
							continue;
					String concatenatedTerms = "";					
					for (int j = 0; j < termSize; j++)
					{						
						concatenatedTerms= concatenatedTerms.concat(occurencesL.get(i+j).getWord());						
					}
					if (phraseText.equalsIgnoreCase(concatenatedTerms))
						docPosL.add(occurencesL.get(i).getPositionOfDocumentInXMLFile());					
				}
		}		
		return docPosL;
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

	public List<WikiPage> wikiPagesMatchingQuery(int topN) throws IOException, XMLStreamException {
		List<WikiPage> wikiPages = wikiPagesMatchingQuery();
		// thanks to http://stackoverflow.com/questions/5805602/how-to-sort-list-of-objects-by-some-property
		final List<Term> queryTerms = new PhraseClause(queryTokens, BooleanOp.MUST).getTerms();
		Collections.sort(wikiPages, new Comparator<WikiPage>() {
	        public int compare(WikiPage o1, WikiPage o2) {
	            try {	            	
					return (new Double(new BM25(index, queryTerms, o2).compute()).compareTo(new BM25(index, queryTerms, o1).compute()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 0;
	        }
	    });
		List<WikiPage> wikiPagesTopN = new ArrayList<>();
		for (int i = 0; i < topN; i++)
			wikiPagesTopN.add(wikiPages.get(i));		
		return wikiPagesTopN;
	}
	
	private List<Long> getDocumentPositions(BooleanClause booleanClause) throws IOException, XMLStreamException{
		return this.index.fileIndex().findDocumentPositionsInXMLFile(booleanClause.getTerm()); 
	}
	
	private void fillDocumentPositions(BooleanQuery boolQuery) throws IOException, XMLStreamException{				
		for(BooleanImpl clause: boolQuery.getBooleanClauses()) {
			if (clause instanceof BooleanClause)
				clause.getDocPositionST().addAll((this.getDocumentPositions((BooleanClause)clause)));
			if (clause instanceof PhraseClause)
				clause.getDocPositionST().addAll(this.getDocumentPositions((PhraseClause)clause));
			this.docPositionST.addAll(clause.getDocPositionST());
		}		
		
		for(BooleanImpl clause: boolQuery.getBooleanClauses()) {			
			switch (clause.getBoolOp()){
			case SHOULD:						
				//do nothing
				break;
			case MUST:									
				this.docPositionST.retainAll(clause.getDocPositionST());											
				break;
			case MUSTNOT:				
				this.docPositionST.removeAll(clause.getDocPositionST());
				break;				
			}			
		}		
	}
		
}
