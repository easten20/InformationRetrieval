package de.hpi.krestel.mySearchEngine.domain;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.parser.ReadXMLParser;
import de.hpi.krestel.mySearchEngine.parser.WikiXMLIterable;


/**
 * @author easten
 * class to contain pages from xml file
 */
public class WikiPage {
	private String title;
	private String id;   
	private String text;
	private long positionInXMLFile;

	public WikiPage(){  
		text = "";
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void addText(String data) {
		text += data;
	}

	public void setPositionInXMLFile(long position) {
		positionInXMLFile = position;
	}	
	
	public long getPositionInXMLFile() {
		return positionInXMLFile;
	}
	
	public static WikiPage from(String xmlFilePath, long positionInXMLFile) throws IOException, XMLStreamException {
		WikiXMLIterable wikiPages = new WikiXMLIterable(xmlFilePath);
		ReadXMLParser parser = wikiPages.iterator();
		parser.jumpToPosition(positionInXMLFile);
		boolean thereIsAWikiPage = parser.hasNext(); 
		assert thereIsAWikiPage;
		WikiPage wikiPage =  parser.next();
		assert wikiPage.getPositionInXMLFile() == positionInXMLFile;
		wikiPage.setPositionInXMLFile(positionInXMLFile);
		return wikiPage;
	}
}
