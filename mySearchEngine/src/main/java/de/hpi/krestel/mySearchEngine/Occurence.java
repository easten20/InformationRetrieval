package de.hpi.krestel.mySearchEngine;

public class Occurence {

	String id;
	int position;
	private String word;
	
	public Occurence(String item, String word) {
		
		id = item.split(":")[0];
		position = Integer.parseInt(item.split(":")[1]);
		this.word =word;
		
		// TODO Auto-generated constructor stub
	}
	
	public String getDocumentId (){
		return id;
	}
	
	public String getWord(){
		return word;
	}
	
	public int getPositionOfWordInDocument (){
		return position;
	}
	
	public String toString (){
		
		return ("This word: " + word + " occurs in document " + id + " at position" + position);
	}


}
