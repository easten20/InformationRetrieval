package de.hpi.krestel.mySearchEngine.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
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
 public class ReadXMLParser implements Iterator<WikiPage> {


	private XMLEventReader eventReader;
	static final String TITLE = "title";
	static final String ID = "id";
	static final String TEXT = "text";
	static final String PAGE = "page";	
	private WikiPage nextWikiPage;
	FileInputStream inputStream;	

	public ReadXMLParser(String xmlFile) throws IOException, XMLStreamException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		inputStream = new FileInputStream(xmlFile);	
		eventReader = inputFactory.createXMLEventReader(inputStream);
	}
	
	public void jumpToPosition(long position) throws IOException {
		inputStream.getChannel().position(position);
	}

	@Override
	public boolean hasNext() {		
		if (nextWikiPage == null){
			try {
				this.readNewWikiPage();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return (nextWikiPage != null);			
		}
		else
		{
			return true;
		}
	}

	private void readNewWikiPage() throws IOException{		
		try {		
			assert this.nextWikiPage == null;
			XMLEvent event = eventReader.nextEvent();
			while (eventReader.hasNext()) {
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					// If we have an page element, we create a new wikipage
					if (startElement.getName().getLocalPart() == (PAGE)) {
						this.nextWikiPage = new WikiPage();
						nextWikiPage.setPositionInXMLFile(startElement.getLocation().getCharacterOffset());
					}
				}	    	
				if (event.isStartElement()) {
					if (event.asStartElement().getName().getLocalPart().equals(TEXT)) {
						event = eventReader.nextEvent();
						while (event.isCharacters()) {
							this.nextWikiPage.addText(event.asCharacters().getData());
							event = eventReader.nextEvent();
						}
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
				event = eventReader.nextEvent();
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
			throw new NoSuchElementException("check your code damn it!!! use hashNext() befor calling me.");
		}
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

}
