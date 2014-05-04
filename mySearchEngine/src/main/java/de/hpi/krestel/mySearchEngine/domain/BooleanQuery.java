package de.hpi.krestel.mySearchEngine.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


//thanks to http://searchhub.org/2011/12/28/why-not-and-or-and-not/
public class BooleanQuery implements BooleanImpl {	
	private List<BooleanImpl> booleanImplL;	
	private BooleanOp boolOp;
	private Set<Long> docPostionL;
	
	public BooleanQuery() {		
		 this.booleanImplL = new ArrayList<BooleanImpl>();
		 this.docPostionL = new TreeSet<Long>();
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
	
}
