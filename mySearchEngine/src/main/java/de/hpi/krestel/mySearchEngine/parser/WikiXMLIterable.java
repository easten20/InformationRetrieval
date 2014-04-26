package de.hpi.krestel.mySearchEngine.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;
public class WikiXMLIterable implements Iterable<WikiPage> {
	String fileName;
	public WikiXMLIterable(String fileName){
		this.fileName = fileName;
	}	
	@Override
	public Iterator<WikiPage> iterator() {
		return new ReadXMLParser(fileName); 
	}

}
class ReadXMLParser implements Iterator<WikiPage> {


	private XMLEventReader eventReader;
	static final String TITLE = "title";
	static final String ID = "id";
	static final String TEXT = "text";
	static final String PAGE = "page";	
	private WikiPage nextWikiPage;

	public ReadXMLParser(String xmlFile){
		try {
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			InputStream in;		
			in = new FileInputStream(xmlFile);		
			this.eventReader = inputFactory.createXMLEventReader(in);
		} catch (FileNotFoundException | XMLStreamException e) {
			// TODO Auto-generated catch block			
			e.printStackTrace();
			System.out.println("Read XML File failed: " + e.getMessage());			
		}
	}	

	@Override
	public boolean hasNext() {		
		if (nextWikiPage == null){
			this.readNewWikiPage();
			return (nextWikiPage != null);			
		}
		else
		{
			return true;
		}
	}

	private void readNewWikiPage(){		
		try {		
			assert this.nextWikiPage == null;
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					// If we have an page element, we create a new wikipage
					if (startElement.getName().getLocalPart() == (PAGE)) {
						this.nextWikiPage = new WikiPage();	            	            
					}
				}	    	
				if (event.isStartElement()) {
					if (event.asStartElement().getName().getLocalPart().equals(TEXT)) {
						event = eventReader.nextEvent();
						this.nextWikiPage.setText(event.asCharacters().getData());
						continue;
					}
				}	    	 
				if (event.isStartElement()) {
					if (event.asStartElement().getName().getLocalPart().equals(TITLE)) {
						event = eventReader.nextEvent();
						this.nextWikiPage.setTitle(event.asCharacters().getData());
						continue;
					}
				}
				if (event.isStartElement()) {
					if (event.asStartElement().getName().getLocalPart().equals(ID) && this.nextWikiPage.getId() == null) {
						event = eventReader.nextEvent();
						this.nextWikiPage.setId(event.asCharacters().getData());	               
						continue;
					}
				}
				if (event.isEndElement()) {
					EndElement endElement = event.asEndElement();
					if (endElement.getName().getLocalPart() == (PAGE)) {
						return;
					}
				}
			}
		}
		catch (XMLStreamException e) {
			System.out.println("Fails to return Wikipage: " + e.getMessage());
		}				
	}

	@Override
	public WikiPage next() {			
		if (this.hasNext()){		
			WikiPage wikiPage = this.nextWikiPage;
			this.nextWikiPage = null;
			return wikiPage;			
		}
		else {
			throw new NoSuchElementException("check your code damn it!!! use hashnext befor calling me");
		}
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

}
