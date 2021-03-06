package de.hpi.krestel.mySearchEngine.parser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;


public class ReadXMLParser implements Iterator<WikiPage> {


	private XMLEventReader eventReader;
	static final String TITLE = "title";
	static final String ID = "id";
	static final String TEXT = "text";
	static final String PAGE = "page";	
	private WikiPage nextWikiPage;
	FileInputStream inputStream;
	BufferedInputStream buffInputStream;
	RandomAccessFile rand;
	long lastPageLocation; // last end of a page tag
	int lastCharacterOffset; // RANT: THESE damn integers... no way we will parse files greater 2GB.. man I hate them.....
	boolean canParseSeveralWikiPages;
	String xmlFile;		

	public ReadXMLParser(String xmlFile) throws IOException, XMLStreamException {
		inputStream = new FileInputStream(xmlFile);
		buffInputStream = new BufferedInputStream(inputStream);
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		eventReader = inputFactory.createXMLEventReader(buffInputStream);
		this.xmlFile = xmlFile;
		jumpToPosition(0);
	}
	
	public void jumpToPosition(long position) throws IOException, XMLStreamException {					
		inputStream.getChannel().position(position);		
		lastPageLocation = position;		
		lastCharacterOffset = 0; // I HATE IT
		canParseSeveralWikiPages = position == 0;					
		//StreamSource source = new StreamSource(inputStream, "ASCII");					
		//XMLStreamReader reader = inputFactory.createXMLStreamReader(countStream, "ASCII");			    	    
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
	
	String eventText(XMLEvent event) {		
		String string = event.asCharacters().getData();		
		/*byte[] bytes = string.getBytes(Charset.forName("UTF-8"));
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		CharBuffer charBuffer =  Charset.forName("UTF-8").decode(buffer);
		String unicodeString = String.valueOf(charBuffer);			
		return unicodeString;
		*/				
		return string;
	}	

	private void readNewWikiPage() throws IOException{		
			try {		
				XMLEvent event = eventReader.nextEvent();
				assert this.nextWikiPage == null;				
				while (eventReader.hasNext()) {					
					if (event.isStartElement()) {
						StartElement startElement = event.asStartElement();
						// If we have an page element, we create a new wikipage
						if (startElement.getName().getLocalPart() == (PAGE)) {
							this.nextWikiPage = new WikiPage();
							//nextWikiPage.setPositionInXMLFile(lastPageLocation);
						}
					}	    	
					if (event.isStartElement()) {
						if (event.asStartElement().getName().getLocalPart().equals(TEXT)) {
							event = eventReader.nextEvent();
							while (event.isCharacters()) {
								this.nextWikiPage.addText(eventText(event));
								event = eventReader.nextEvent();
							}
							continue;
						}
					}
					if (event.isStartElement()) {
						if (event.asStartElement().getName().getLocalPart().equals(TITLE)) {
							event = eventReader.nextEvent();
							this.nextWikiPage.setTitle(eventText(event));
							continue;
						}
					}
					if (event.isStartElement()) {
						if (event.asStartElement().getName().getLocalPart().equals(ID) && this.nextWikiPage.getId() == null) {
							event = eventReader.nextEvent();
							this.nextWikiPage.setId(eventText(event));	               
							continue;
						}
					}
					if (event.isEndElement()) {
						EndElement endElement = event.asEndElement();
						if (endElement.getName().getLocalPart() == (PAGE)) {
							/*
							if (canParseSeveralWikiPages) {
								//int currentCharacterOffset = endElement.getLocation().getCharacterOffset();
								if (inputStream.getChannel().position() < endElement.getLocation().getCharacterOffset())
									System.out.println("stop!!!!");
								if (lastCharacterOffset > currentCharacterOffset) {
									System.out.println("from " + currentCharacterOffset + " to " + currentCharacterOffset);
								}
								lastPageLocation += currentCharacterOffset - lastCharacterOffset;
								lastCharacterOffset = currentCharacterOffset;
								//nextWikiPage.setStopPositionInXMLFile(lastPageLocation);
							}
							*/
							return;
						}
					}
					event = eventReader.nextEvent();
				}
			}
			catch (XMLStreamException e) {
				System.out.println("Fails to return Wikipage for " + lastPageLocation + ": " + e.getMessage());
				try {
					jumpToPosition(lastPageLocation - 1);
				} catch (XMLStreamException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			catch (NoSuchElementException e) {
				System.out.println("no such element.");
				return;
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
			throw new NoSuchElementException("check your code damn it!!! use hashNext() before calling me.");
		}
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

}
