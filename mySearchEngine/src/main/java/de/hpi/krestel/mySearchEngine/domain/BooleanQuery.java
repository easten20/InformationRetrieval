package de.hpi.krestel.mySearchEngine.domain;

import java.util.ArrayList;
import java.util.List;


//thanks to http://searchhub.org/2011/12/28/why-not-and-or-and-not/
public class BooleanQuery {	
	private List<BooleanClause> booleanClausesL;	
	
	public BooleanQuery() {		
		 this.booleanClausesL = new ArrayList<BooleanClause>();
	}				

	public void addClause(BooleanClause booleanClause){
		this.booleanClausesL.add(booleanClause);
	}
	
	public List<BooleanClause> getBooleanClauses() {
		return booleanClausesL;
	} 	
}
