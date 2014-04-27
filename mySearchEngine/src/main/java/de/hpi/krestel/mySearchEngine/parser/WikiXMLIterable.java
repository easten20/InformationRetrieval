package de.hpi.krestel.mySearchEngine.parser;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;


import de.hpi.krestel.mySearchEngine.domain.WikiPage;
public class WikiXMLIterable implements Iterable<WikiPage> {
	String fileName;
	public WikiXMLIterable(String fileName){
		this.fileName = fileName;
	}	
	@Override
	public ReadXMLParser iterator() {
		try {
			return new ReadXMLParser(fileName);
		} catch (IOException | XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}

}
