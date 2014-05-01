package de.hpi.krestel.mySearchEngine.domain;

public class Term {
	
	private String text;
	private StarOp starOp;
	
	public Term(String text)
	{
		this.text = text;
	}
			
	public StarOp getStarOp()
	{
		return this.starOp;
	}
	
}
