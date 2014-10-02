package de.hpi.krestel.mySearchEngine.domain;

import java.util.List;
import java.util.Set;

import de.hpi.krestel.mySearchEngine.Occurence;

public interface BooleanImpl {
	public BooleanOp getBoolOp();
	public void setBoolOp(BooleanOp boolOp);
	public List<Term> getTerms();
	Set<Long> getDocPositionST();
	List<DocumentOcc> getDocumentOccL();
}
