package de.hpi.krestel.mySearchEngine.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;


public class ReadXMLParser2 implements Iterator<WikiPage> {
	
	private WikiPage nextWikiPage;
	FileInputStream inputStream;	
	RandomAccessFile rand;
	long lastPageLocation; // last end of a page tag
	int lastCharacterOffset; // RANT: THESE damn integers... no way we will parse files greater 2GB.. man I hate them.....
	boolean canParseSeveralWikiPages;
	String xmlFile;		

	public ReadXMLParser2(String xmlFile) throws IOException, XMLStreamException {
		this.xmlFile = xmlFile;
		jumpToPosition(0);		
	}
	
	public void jumpToPosition(long position) throws IOException, XMLStreamException {
		File file = new File(xmlFile);
		this.rand = new RandomAccessFile(file, "r");
		this.rand.seek(position);
		lastPageLocation = position;				
	}

	@Override
	public boolean hasNext() {		
		if (nextWikiPage == null){
			try {
				this.readNewWikiPage();
			} catch (IOException  e) {
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
		assert this.nextWikiPage == null;
		String line;
		StringBuilder strBuilderText = new StringBuilder();
		boolean isText = false;
		boolean isPage = false;
		boolean isTitle = false;	
		int count = 0;
		long lastPointerOffset = lastPageLocation;			
		while ((line = rand.readLine()) != null){				
			if (isPage == false){
				if (line.matches(".*<page>.*")){
					this.nextWikiPage = new WikiPage();
					this.nextWikiPage.setPositionInXMLFile(lastPointerOffset);
					isPage = true;
				}
			}
			if (isPage == true && isTitle == false){
				if (line.matches(".*<title>.*")){
					isTitle = true;
					line = line.replaceAll("<title>", "");
					line = line.replaceAll("</title>", "");
					this.nextWikiPage.setTitle(line);
					count++;							
					System.out.println("parse : " + line);
				}
			}					
			if (isPage == true && isTitle == true) {					
				if (isText == true || line.matches(".*<text.*>.*")) {
					if (isText == false){
						strBuilderText = new StringBuilder();
						isText = true;
					}											
					strBuilderText.append(line);	
					if (line.matches(".*</text.*>")) {
						String reg = "<text.*?>(.*)<\\/text>";
						Pattern p = Pattern.compile(reg, Pattern.DOTALL);			        
						Matcher m = p.matcher(strBuilderText.toString());						
						if (m.find())
						{								
							String s1 = m.group(1);			            			           
							this.nextWikiPage.setText(s1);
							nextWikiPage.setStopPositionInXMLFile(rand.getFilePointer());
							return;
						}
					}
				}
			}				
			lastPointerOffset = rand.getFilePointer();								
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
