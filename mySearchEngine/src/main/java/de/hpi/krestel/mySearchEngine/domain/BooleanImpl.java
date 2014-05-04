package de.hpi.krestel.mySearchEngine.domain;

import java.util.Set;

public interface BooleanImpl {
	public BooleanOp getBoolOp();
	public void setBoolOp(BooleanOp boolOp);
	Set<Long> getDocPositionST();
}
