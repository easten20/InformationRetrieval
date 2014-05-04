package de.hpi.krestel.mySearchEngine;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;

public class Occurence implements Comparable<Occurence>{

	private long positionOfDocumentInXMLFile;
	private int positionOfWordInDocument;
	private String word;
	private String xmlFilePath;
	
	public Occurence(String item, String word, String xmlFilePath) {
		
		String[] splitItem = item.split(":");
		this.positionOfDocumentInXMLFile = Long.parseLong(splitItem[0]);
		this.positionOfWordInDocument = Integer.parseInt(splitItem[1]);
		this.word = word;
		this.xmlFilePath = xmlFilePath;
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
	
	public WikiPage getWikiPage() throws IOException, XMLStreamException {
		return WikiPage.from(xmlFilePath, getPositionOfDocumentInXMLFile());
	}

	@Override
	public int compareTo(Occurence comparedObj) {
		if (this.positionOfDocumentInXMLFile != comparedObj.positionOfDocumentInXMLFile)
			return (int) (this.positionOfDocumentInXMLFile - comparedObj.positionOfDocumentInXMLFile);
		else 
			return this.positionOfWordInDocument - comparedObj.positionOfWordInDocument;		
	}


}
