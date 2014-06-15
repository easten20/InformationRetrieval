package de.hpi.krestel.mySearchEngine.parser;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;


import de.hpi.krestel.mySearchEngine.domain.WikiPage;
public class WikiXMLIterable implements Iterable<WikiPage> {
	String fileName;
	long startPosition;
	
	public WikiXMLIterable(String fileName){
		this.fileName = fileName;
		startPosition = 0;
	}	
	
	public void setPosition(long position) {
		startPosition = position;
	}
	@Override
	public ReadXMLParser2 iterator() {
		ReadXMLParser2 parser;
		try {
			parser = new ReadXMLParser2(fileName);
			parser.jumpToPosition(startPosition);
			return parser;
		} catch (IOException | XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}
}
