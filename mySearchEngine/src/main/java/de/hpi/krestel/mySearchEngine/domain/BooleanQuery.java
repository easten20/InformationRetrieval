package de.hpi.krestel.mySearchEngine.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.hpi.krestel.mySearchEngine.Occurence;


//thanks to http://searchhub.org/2011/12/28/why-not-and-or-and-not/
public class BooleanQuery implements BooleanImpl {	
	private List<BooleanImpl> booleanImplL;	
	private BooleanOp boolOp;
	private Set<Long> docPostionL;
	private List<DocumentOcc> occurencesL;
	private List<Term> getTerms;
	
	public BooleanQuery() {		
		 this.booleanImplL = new ArrayList<BooleanImpl>();
		 this.docPostionL = new TreeSet<Long>();
		 this.occurencesL = new ArrayList<DocumentOcc>();
	}
	
	public BooleanQuery(BooleanOp boolOp) {		
		 this();
		 this.boolOp = boolOp;
	}

	public void addClause(BooleanImpl booleanClause){
		this.booleanImplL.add(booleanClause);
	}		
	
	public List<BooleanImpl> getBooleanClauses() {
		return booleanImplL;
	}

	public BooleanOp getBoolOp() {
		return boolOp;
	}

	public void setBoolOp(BooleanOp boolOp) {
		this.boolOp = boolOp;
	}

	public Set<Long> getDocPositionST() {
		return docPostionL;
	}
	
	public List<Term> getTerms() {
		List<Term> terms = new ArrayList<Term>();
		for (BooleanImpl booleanImpl : this.booleanImplL)
			terms.addAll(booleanImpl.getTerms());
		return terms;
	}

	@Override
	public List<DocumentOcc> getDocumentOccL() {
		return occurencesL;
	}		
	
}
