package de.hpi.krestel.mySearchEngine;

import de.hpi.krestel.mySearchEngine.domain.BooleanOp;
import de.hpi.krestel.mySearchEngine.domain.Term;

public class MyQuery {
	
	private String query;
	private Iterable<BooleanOp> bolOps;
	private Iterable<Term> queryWords; //[Artikel, Smithee]	
	
	public MyQuery(String query){
		this.query = query;
	}
	
	public Iterable<BooleanOp> getOperators(){
		return this.bolOps;		
	}
	
	public Iterable<Term> getTerms(){
		return this.queryWords;
	}		
}
