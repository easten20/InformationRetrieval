package de.hpi.krestel.mySearchEngine.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class PhraseClause implements BooleanImpl {
		
	private BooleanOp boolOp;
	private List<Term> termsL;
	private Set<Long> docPositionST;
	
	public PhraseClause(Iterable<String> iterableString){
		this(iterableString, BooleanOp.SHOULD);		
	}
	
	public PhraseClause(Iterable<String> phraseTextL, BooleanOp boolOp){
		termsL = new ArrayList<Term>();
		for (String string: phraseTextL){
			termsL.add(new Term(string));
		}
		this.boolOp = boolOp;
		this.docPositionST = new TreeSet<Long>();
	}					
	
	public String getPhraseText(){
		StringBuilder strBuilder = new StringBuilder();		
		for (Term term: this.termsL)
			strBuilder.append(term.getText() + " ");		
		return strBuilder.toString().trim();
	}
	
	public List<Term> getTerms(){
		return this.termsL;
	}

	@Override
	public BooleanOp getBoolOp() { 
		return this.boolOp;
	}

	@Override
	public void setBoolOp(BooleanOp boolOp) {
		this.boolOp = boolOp;		
	}
	
	public Set<Long> getDocPositionST(){
		return this.docPositionST;
	}
}