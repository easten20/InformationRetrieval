package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author easten
 * XML parser class to parse xml file
 */
public class ReadXMLFile extends DefaultHandler {
				
		public List<WikiPage> pageL = new ArrayList<WikiPage>();
		WikiPage page;
		String tmpValue;		
		String fileName;
		boolean isPage = false;
	    int limit = 5;
	    
	    public ReadXMLFile(String fileName) {
	    	this.fileName = fileName;
	    	parseDocument();
	    }
	    
		public void startElement(String uri, String localName,String elementName, 
	                Attributes attributes) throws SAXException {	
			
			//System.out.println("Start Element :" + elementName);
	 
			if (elementName.equalsIgnoreCase("page")) {			
				 page = new WikiPage();			
				 isPage = true;			 			 			
			}
		}
		
		 private void parseDocument() {			 
			         SAXParserFactory factory = SAXParserFactory.newInstance();			 
			         try {			 
			             SAXParser parser = factory.newSAXParser();			 
			             parser.parse(this.fileName, this);			 
			         } catch (ParserConfigurationException e) {			
			             System.out.println("ParserConfig error");			
			         } catch (SAXException e) {		
			             System.out.println("finish parsing xml");			
			         } catch (IOException e) {		
			             System.out.println("IO error");			
			         }			 
			     }

	 
		public void endElement(String uri, String localName,
			String element) throws SAXException {
			 if (isPage) {
			        if (element.equals("id") && !(page.id != null)) 		        	
			            page.id = this.tmpValue;
			        else if (element.equals("title"))
			        	page.title = this.tmpValue;
			        else if (element.equals("page"))
			        {
			        	pageL.add(page);
			        	isPage = false;
			        	--limit;		        	
			        	// throws error when the parser reach its limit
			        	if (limit == 0)			        	        		 			        		    			   
			        		throw new SAXException();			        	
			        }
			    }
			//System.out.println("End Element :" + element);
	 
		}
	 
		public void characters(char ch[], int start, int length) throws SAXException {		
				tmpValue = new String(ch, start, length);					
		}
	 	
	
   public static void main(String argv[]) {
     
 	
	ReadXMLFile handler = new ReadXMLFile("C:\\Users\\easten\\Documents\\Information Retrieval\\dewiki-20140216-pages-articles-multistream.xml");
    
     for (WikiPage page : handler.pageL)
  	   System.out.println("title :" + page.title);     
 
   }
   
}
 