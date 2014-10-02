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
import de.hpi.krestel.mySearchEngine.domain.DocumentOcc;
import de.hpi.krestel.mySearchEngine.domain.PhraseClause;
import de.hpi.krestel.mySearchEngine.domain.Term;
import de.hpi.krestel.mySearchEngine.domain.WikiPage;
import de.hpi.krestel.mySearchEngine.score.BM25_New;

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
		String LINKTO = "LINKTO";
		if (queryText.startsWith(LINKTO)) {
			// synchronize with WikiPage.getLinks
			queryText = queryText.substring(LINKTO.length());
			queryText = queryText.trim();
			queryText = queryText.replace(" ", "]");
			queryText = queryText.toLowerCase();
			queryText = "[[" + queryText + "]]";
			// queryText is now the word we need to look for.
			// I do not understand what is going on behind this point.
			// I will not spend hours to complete the task.
			// TODO: May somebody do it who knows about the BooleanQuery.
		}
		//now stringList = [fish*, and, *tropical] or [fish*, butnot, *tropical]		         		
		//in case of "but not", [fish, but, not, tropical] -> [fish, butnot, tropical]		
		queryText = queryText.toLowerCase().replace("but not", butnotOP);			
		//thanks to http://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
		Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
		Matcher regexMatcher = regex.matcher(queryText);				
		TokenStream token = new TokenStream();
		queryTokens = token.preprocessText(queryText);
		this.booleanQuery = new BooleanQuery();
		List<String> operator = new ArrayList<String>();
		while (regexMatcher.find()) {
		    if (regexMatcher.group(1) != null || regexMatcher.group(2) != null) {
		        // Add double-quoted or single-quoted string without the quotes
		    	this.booleanQuery.addClause(new PhraseClause(token.preprocessText(regexMatcher.group())));		    			   
		    } else {
		        // Add unquoted word
		    	if (regexMatcher.group().equalsIgnoreCase(andOp) ||		    		
		    			regexMatcher.group().equalsIgnoreCase(orOp) ||
		    				regexMatcher.group().equalsIgnoreCase(butnotOP))
		    		operator.add(regexMatcher.group());
		    	else
		    		if (!token.isStopWord(regexMatcher.group()))
		    			this.booleanQuery.addClause(new BooleanClause(new Term(token.stemText(regexMatcher.group()))));
		    }		    		    
		} 	
		for (int i = 0; i < operator.size(); i++){
			if (operator.get(i).equalsIgnoreCase(andOp)) {
				this.booleanQuery.getBooleanClauses().get(i).setBoolOp(BooleanOp.MUST);
				this.booleanQuery.getBooleanClauses().get(i+1).setBoolOp(BooleanOp.MUST);
			}
			else if (operator.get(i).equalsIgnoreCase(orOp)){
				this.booleanQuery.getBooleanClauses().get(i).setBoolOp(BooleanOp.SHOULD);
				this.booleanQuery.getBooleanClauses().get(i+1).setBoolOp(BooleanOp.SHOULD);
			}
			else if (operator.get(i).equalsIgnoreCase(butnotOP))
				this.booleanQuery.getBooleanClauses().get(i+1).setBoolOp(BooleanOp.MUSTNOT);
		}
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
		//String phraseText = phraseQuery.getPhraseText().replace(" ", "");
		for (int i = 0; i < occurencesL.size(); i++){
			if ((i + termSize) > occurencesL.size()) 
				break;
			else
				if (firstWord.compareTo(occurencesL.get(i).getWord()) == 0)
				{
					boolean docSimilar = true;
					Long docId = occurencesL.get(i).getPositionOfDocumentInXMLFile();
					for (int j = 1; j < termSize; j++){
						if (docId != occurencesL.get(i + j).getPositionOfDocumentInXMLFile()){
							docSimilar = false;
							break;
						}
					}
					if (!docSimilar)
						continue;
					else {						
						boolean phraseSimilar = true;
						for (int j = 0; j < termSize; j++){							
							if (phraseQuery.getTerms().get(j).getText().compareTo(occurencesL.get(i+j).getWord()) != 0)
								phraseSimilar = false;
						}						
						if (phraseSimilar == true) {							
							if (termSize-1 == (occurencesL.get(i+termSize-1).getPositionOfWordInDocument() - occurencesL.get(i).getPositionOfWordInDocument()))
								docPosL.add(occurencesL.get(i).getPositionOfDocumentInXMLFile());
						}
					}
				}				
		}		
		return docPosL;
	}
	
	public List<DocumentOcc> getDocumentOccurences(PhraseClause phraseQuery) throws IOException, XMLStreamException{		
		List<Occurence> occurencesL = new ArrayList<Occurence>();
		List<Occurence> occurencesNewL = new ArrayList<Occurence>();
		for (Term term: phraseQuery.getTerms()) {			
			occurencesL.addAll(this.index.fileIndex().findDocuments(term.getText()));			
		}				
		Collections.sort(occurencesL);
		int termSize = phraseQuery.getTerms().size(); // combinated word's size
		String firstWord = phraseQuery.getTerms().get(0).getText(); //first word		
		//String phraseText = phraseQuery.getPhraseText().replace(" ", "");
		for (int i = 0; i < occurencesL.size(); i++){
			if ((i + termSize) > occurencesL.size()) 
				break;
			else
				if (firstWord.compareTo(occurencesL.get(i).getWord()) == 0)
				{
					boolean docSimilar = true;
					Long docId = occurencesL.get(i).getPositionOfDocumentInXMLFile();
					for (int j = 1; j < termSize; j++){
						if (docId != occurencesL.get(i + j).getPositionOfDocumentInXMLFile()){
							docSimilar = false;
							break;
						}
					}
					if (!docSimilar)
						continue;
					else {						
						boolean phraseSimilar = true;
						for (int j = 0; j < termSize; j++){							
							if (phraseQuery.getTerms().get(j).getText().compareTo(occurencesL.get(i+j).getWord()) != 0)
								phraseSimilar = false;
						}						
						if (phraseSimilar == true) {							
							if (termSize-1 == (occurencesL.get(i+termSize-1).getPositionOfWordInDocument() - occurencesL.get(i).getPositionOfWordInDocument()))
								occurencesNewL.add(occurencesL.get(i));
						}
					}
				}				
		}		
		return this.groupOccurencesToDocumentOcc(occurencesNewL);
	}
		
	private List<Long> getDocumentPositions(BooleanClause booleanClause) throws IOException, XMLStreamException{
		return this.index.fileIndex().findDocumentPositionsInXMLFile(booleanClause.getTerm()); 
	}
	
	private List<DocumentOcc> getDocumentOccurences(BooleanClause booleanClause) throws IOException, XMLStreamException{
		return this.groupOccurencesToDocumentOcc(this.index.fileIndex().findDocuments(booleanClause.getTerm()));				
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
	
	/***
	 * Instead of parse all of wikipages every time we retrieve it 
	 * we only return the occurences 
	 */
	public List<WikiPage> wikiPagesMatchingQuery_New(int topN) throws IOException, XMLStreamException {
		List<DocumentOcc> documentOccs = new ArrayList<DocumentOcc>();
		documentOccs = this.fillDocumentPositions(this.booleanQuery);									
		this.calculateScoreDocumentOccs(documentOccs);		
		List<WikiPage> wikiPages = new ArrayList<WikiPage>();
		if (topN > documentOccs.size())
			topN = documentOccs.size();
		for (int i = 0; i < topN; i++){			
			WikiPage wikiPage = WikiPage.from(this.index.getXMLFilePath(), documentOccs.get(i).getPositionOfDocumentInXMLFile());			
			wikiPages.add(wikiPage);
			wikiPage.setScore(documentOccs.get(i).getScore());			
		}		
		return wikiPages;							
	}
		
	public void calculateScoreDocumentOccs(List<DocumentOcc> docOccs){
		//calculate document Score
		System.out.println("running calculation...");
		final List<Term> queryTerms = new ArrayList<Term>(); 
		for (BooleanImpl booleanImpl: this.booleanQuery.getBooleanClauses())
			queryTerms.addAll(booleanImpl.getTerms());				
		Collections.sort(
				docOccs, new Comparator<DocumentOcc>() {
		    public int compare(DocumentOcc o1, DocumentOcc o2) {
		    	try {	            	
					return (new Double(new BM25_New(index, queryTerms, o2).compute()).compareTo(new BM25_New(index, queryTerms, o1).compute()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	return 0;
				        }
					}
				);		
	}
	
	public List<WikiPage> wikiPagesMatchingQuery(int topN) throws IOException, XMLStreamException {
		List<WikiPage> wikiPages = wikiPagesMatchingQuery();
		// thanks to http://stackoverflow.com/questions/5805602/how-to-sort-list-of-objects-by-some-property
		final List<Term> queryTerms = new PhraseClause(queryTokens, BooleanOp.MUST).getTerms();
		Collections.sort(
			wikiPages, new Comparator<WikiPage>() {
		        public int compare(WikiPage o1, WikiPage o2) {
		            try {	            	
						return (new Double(new BM25(index, queryTerms, o2).compute()).compareTo(new BM25(index, queryTerms, o1).compute()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return 0;
		        }
			}
		);		
		if (topN > wikiPages.size())
			topN = wikiPages.size();
		return wikiPages.subList(0, topN);					
	}		
	
	private List<DocumentOcc> groupOccurencesToDocumentOcc(List<Occurence> occurencesL){
		long docId = -1; //set initial page Id
		Collections.sort(occurencesL);
		List<DocumentOcc> documentOccs = new ArrayList<DocumentOcc>();
		DocumentOcc docOcc = new DocumentOcc();
		for (Occurence occurence : occurencesL){
			if (docId != occurence.getPositionOfDocumentInXMLFile()){
				docOcc = new DocumentOcc();
				docId = occurence.getPositionOfDocumentInXMLFile();
				docOcc.setPageId(docId);
				documentOccs.add(docOcc);
			}
			docOcc.getOccurenceL().add(occurence);			
		}
		return documentOccs;
	}
	
	private List<DocumentOcc> fillDocumentPositions(BooleanQuery boolQuery) throws IOException, XMLStreamException{				
		for(BooleanImpl clause: boolQuery.getBooleanClauses()) {
			if (clause instanceof BooleanClause){
				clause.getDocumentOccL().addAll((this.getDocumentOccurences((BooleanClause)clause)));
			}
			if (clause instanceof PhraseClause){		
				clause.getDocumentOccL().addAll(this.getDocumentOccurences((PhraseClause)clause));		
			}
		}						
		boolean isEmpty = false;		
		boolean isExist;
		boolean isSaved;
		int[] pointer = new int[boolQuery.getBooleanClauses().size()];	
		Long[] documentIDs = new Long[boolQuery.getBooleanClauses().size()]; 
		long currentDocumentId = 0;
		List<DocumentOcc> documentOccsL = new ArrayList<DocumentOcc>();		
		DocumentOcc documentOcc;
		while (!isEmpty){
			//check lowest document Id in global pointer
			currentDocumentId = 0;
			for(int i = 0; i < pointer.length; i++) {
				if (pointer[i] != -1){
					documentIDs[i] = boolQuery.getBooleanClauses().get(i).getDocumentOccL().get(pointer[i]).getPositionOfDocumentInXMLFile();
					if (currentDocumentId == 0)
						currentDocumentId = documentIDs[i];
					if (currentDocumentId > documentIDs[i]) 
						currentDocumentId = documentIDs[i];					
				}
			}			
			//check saved process
			isSaved = true;
			for(int i = 0; i < pointer.length; i++) {
				isExist = (currentDocumentId == documentIDs[i]);									
				switch (boolQuery.getBooleanClauses().get(i).getBoolOp()){			
				case SHOULD:					
					//do nothing
					break;
				case MUST:					
					isSaved = isExist;
					break;
				case MUSTNOT:
					if (isExist)
						isSaved = false;
					break;
				}
				if (!isSaved)
					break;
			}									
			if (isSaved) {
				documentOcc = new DocumentOcc();
				documentOcc.setPageId(currentDocumentId);
				documentOccsL.add(documentOcc);
				for(int i = 0; i < pointer.length; i++) {
					if (currentDocumentId == documentIDs[i] && pointer[i] != -1) {							
							documentOcc.getOccurenceL().addAll(boolQuery.getBooleanClauses().get(i).getDocumentOccL().get(pointer[i]).getOccurenceL());						
					}
				}			
			}
			//add pointer			
			for(int i = 0; i < pointer.length; i++) {
				if (currentDocumentId == documentIDs[i] && pointer[i] != -1) {								
					pointer[i] += 1;
				}
				if (pointer[i] >= boolQuery.getBooleanClauses().get(i).getDocumentOccL().size())
					pointer[i] = -1;																		
			}				
			
			isEmpty = true;
			for (int i = 0; i < pointer.length; i++){
				if (pointer[i] == -1){					
					//stop is MUST has no more elements					
					if (boolQuery.getBooleanClauses().get(i).getBoolOp() == BooleanOp.MUST) {
						isEmpty = true;
						break;					
					}
				}
				else
					isEmpty = false;
			}
		}				
		return documentOccsL;
	}		
}
