package de.hpi.krestel.mySearchEngine.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.hpi.krestel.mySearchEngine.Occurence;

public class BooleanClause implements BooleanImpl {
	private BooleanOp boolOp;
	private Term term;
	private Set<Long> docPositionST;
	private List<DocumentOcc> occurencesL;
	
	public BooleanClause(Term term, BooleanOp boolOp){
		this.setBoolOp(boolOp);
		this.setTerm(term);	
		this.docPositionST = new TreeSet<Long>();
		this.occurencesL = new ArrayList<DocumentOcc>();
	}
	
	public BooleanClause(Term term){
		this(term, BooleanOp.SHOULD);		
	}

	public Term getTerm() {
		return term;
	}
	
	public List<Term> getTerms() {
		List<Term> terms = new ArrayList<>();
		terms.add(this.term);
		return terms;
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

	@Override
	public List<DocumentOcc> getDocumentOccL() {
		return occurencesL;
	}
}
