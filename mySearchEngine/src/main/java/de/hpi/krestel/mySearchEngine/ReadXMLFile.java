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
		StringBuilder strBuilder;
		String fileName;
		boolean isPage = false;
		boolean isElement = false;
	    int limit = 5;
	    
	    public ReadXMLFile(String fileName) {
	    	this.fileName = fileName;
	    	this.strBuilder = new StringBuilder();
	    	parseDocument();
	    }
	    
	    /*
	     * check if cursor is currently inside one of the WikiPage elements
	     */
	    private boolean checkInsideElement(String element)  {
	    	if (element.equals("id") || element.equals("title") || 
	    			element.equals("text") || element.equals("page")) 		        	
	            return true;	 
	    	else
	    		return false;
	    }
	    
		public void startElement(String uri, String localName,String elementName, 
	                Attributes attributes) throws SAXException {	
							 
			if (elementName.equalsIgnoreCase("page")) {			
				 page = new WikiPage();			
				 isPage = true;			 			 			
			}						
			
			if (this.checkInsideElement(elementName)) {
				this.strBuilder = new StringBuilder();
				isElement = true;
			}
			else
				isElement = false;
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
			            page.id = this.strBuilder.toString();
			        else if (element.equals("title"))
			        	page.title = this.strBuilder.toString();
			        else if (element.equals("text"))
			        	page.text = this.strBuilder.toString();
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
	 
		}
	 
		public void characters(char ch[], int start, int length) throws SAXException {
			if (isElement) //only append string builder if inside of the expected node (text,id, etc)
				strBuilder.append(ch, start, length);					
		}
	 	
	
   public static void main(String argv[]) {
     
 	
	ReadXMLFile handler = new ReadXMLFile("C:\\Users\\easten\\Documents\\Information Retrieval\\dewiki-20140216-pages-articles-multistream.xml");
    
     for (WikiPage page : handler.pageL){
  	   System.out.println("title : " + page.title);
  	   System.out.println("text: " + page.text);
     }
 
   }
   
}
 