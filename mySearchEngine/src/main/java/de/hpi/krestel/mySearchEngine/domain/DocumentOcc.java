package de.hpi.krestel.mySearchEngine.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hpi.krestel.mySearchEngine.Occurence;
import de.hpi.krestel.mySearchEngine.score.BM25;

public class DocumentOcc{

	long pageId;
	List<Occurence> occurenceL;
	double score;
	
	public DocumentOcc(){	
		this.occurenceL = new ArrayList<Occurence>();
	}
	
	public List<Occurence> getOccurenceL(){
		return this.occurenceL;
	}
	
	public long getPositionOfDocumentInXMLFile(){
		return this.pageId;
	}
	
	public void setPageId(long pageId){
		this.pageId = pageId;
	}
	
	public void setScore(double score){
		this.score = score;
	}
	
	public double getScore(){		
		return this.score;
	}
	
	public int getTermsInOcc(Term term){
		int fi = 0;
		for (Occurence occurence: this.occurenceL){
			if (term.getStarOp() != StarOp.NOSTAR){				
				if (term.isRegexMatch(occurence.getWord()))
					fi+=1;
			}
			else
			{				
				if (occurence.getWord().compareTo(term.getText()) == 0)
					fi+=1;
			}
		}
		return fi;
	}
}
