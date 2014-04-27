package de.hpi.krestel.mySearchEngine;

public class Occurence {

	private long positionOfDocumentInXMLFile;
	private int positionOfWordInDocument;
	private String word;
	
	public Occurence(String item, String word) {
		
		String[] splitItem = item.split(":");
		this.positionOfDocumentInXMLFile = Long.parseLong(splitItem[0]);
		this.positionOfWordInDocument = Integer.parseInt(splitItem[1]);
		this.word = word;
	}
	
	public String getWord(){
		return word;
	}
	
	public int getPositionOfWordInDocument (){
		return positionOfWordInDocument;
	}
	
	public String toString (){
		
		return ("This word: " + word + " occurs in document " + positionOfDocumentInXMLFile + " at position" + positionOfWordInDocument + ".");
	}

	public long getPositionOfDocumentInXMLFile() {
		return positionOfDocumentInXMLFile;
	}


}
