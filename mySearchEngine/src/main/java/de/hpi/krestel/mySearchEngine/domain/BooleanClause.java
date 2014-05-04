package de.hpi.krestel.mySearchEngine.domain;

import java.util.Set;
import java.util.TreeSet;

public class BooleanClause implements BooleanImpl {
	private BooleanOp boolOp;
	private Term term;
	private Set<Long> docPositionST;
	
	public BooleanClause(Term term, BooleanOp boolOp){
		this.setBoolOp(boolOp);
		this.setTerm(term);	
		this.docPositionST = new TreeSet<Long>();
	}
	
	public BooleanClause(Term term){
		this(term, BooleanOp.SHOULD);		
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public BooleanOp getBoolOp() {
		return boolOp;
	}

	public void setBoolOp(BooleanOp boolOp) {
		this.boolOp = boolOp;
	}
	
	public Set<Long> getDocPositionST(){
		return this.docPositionST;
	}
}
