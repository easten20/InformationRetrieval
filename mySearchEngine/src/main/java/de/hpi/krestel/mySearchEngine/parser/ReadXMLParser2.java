package de.hpi.krestel.mySearchEngine.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;


public class ReadXMLParser2 implements Iterator<WikiPage> {
	
	private WikiPage nextWikiPage;
	FileInputStream inputStream;	
	BufferedRaf rand;	
	long lastPageLocation; // last end of a page tag
	int lastCharacterOffset; // RANT: THESE damn integers... no way we will parse files greater 2GB.. man I hate them.....
	boolean canParseSeveralWikiPages;
	String xmlFile;		
	File file;		

	public ReadXMLParser2(String xmlFile) throws IOException, XMLStreamException {
		this.xmlFile = xmlFile;		
		file = new File(xmlFile);
		this.rand = new BufferedRaf(file, "r");			
		jumpToPosition(0);
	}	
	
	public void jumpToPosition(long position) throws IOException, XMLStreamException {		
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
		long lastPointerOffset = lastPageLocation;			
		while ((line = rand.readLine2()) != null){				
			if (isPage == false){
				if (line.contains("<page>")){
					this.nextWikiPage = new WikiPage();
					this.nextWikiPage.setPositionInXMLFile(lastPointerOffset);
					isPage = true;
				}
			}
			if (isPage == true && isTitle == false){
				if (line.contains("<title>")){
					isTitle = true;
					line = line.replaceAll("<title>", "");
					line = line.replaceAll("</title>", "");
					this.nextWikiPage.setTitle(line);										
				}
			}					
			if (isPage == true && isTitle == true) {					
				if (isText == true || line.matches(".*?<text.*?>.*")) {
					if (isText == false){
						strBuilderText = new StringBuilder();
						isText = true;
					}											
					strBuilderText.append(line);	
					if (line.matches(".*?</text.*?>")) {
						String reg = "<text.*?>(.*)<\\/text>";
						Pattern p = Pattern.compile(reg, Pattern.DOTALL);			        
						Matcher m = p.matcher(strBuilderText.toString());						
						if (m.find())
						{								
							String s1 = m.group(1);			            			           
							this.nextWikiPage.setText(s1);
							nextWikiPage.setStopPositionInXMLFile(this.getByteOffset());
							return;
						}
					}
				}
			}				
			lastPointerOffset = this.getByteOffset();								
		}
	}
	
	private long getByteOffset() throws IOException {
		return this.rand.getFilePointer();
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

	public void close() throws IOException{
		this.rand.close();
	}
}
