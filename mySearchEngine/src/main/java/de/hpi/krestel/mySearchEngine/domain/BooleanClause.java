package de.hpi.krestel.mySearchEngine.domain;

public class BooleanClause {
	private BooleanOp boolOp;
	private Term term;
	
	public BooleanClause(Term term, BooleanOp boolOp){
		this.setBoolOp(boolOp);
		this.setTerm(term);
	}
	
	public BooleanClause(Term term){
		this.setBoolOp(BooleanOp.SHOULD);
		this.setTerm(term);		
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
}
