package de.hpi.krestel.mySearchEngine;

import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
class Page {   
    String title;
    String id;   
    public Page(){        
    }
}


public class ReadXMLFile {
 
   public static void main(String argv[]) {
 
    try {
 
	SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser saxParser = factory.newSAXParser();	
 
	DefaultHandler handler = new DefaultHandler() {
	
		
	List<Page> pageL = new ArrayList<Page>();
	Page page;
	String tmpValue;
	boolean btitle = false;
	boolean bid = false;	
	boolean isPage = false;
    int limit = 5;
    
	public void startElement(String uri, String localName,String elementName, 
                Attributes attributes) throws SAXException {
 
		
		System.out.println("Start Element :" + elementName);
 
		if (elementName.equalsIgnoreCase("page")) {			
			 page = new Page();			
			 isPage = true;			 			 			
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
		        	limit = limit - 1;		        	
		        	if (limit == 0)
		        	{		        		 
		        		for (Page pages : pageL) {		        			 
		        			System.out.println(pages.id);
		        			System.out.println(pages.title);
		        		}		        			 		        			     
		        		throw new SAXException(new Exception());
		        	}
		        }
		    }
		System.out.println("End Element :" + element);
 
	}
 
	public void characters(char ch[], int start, int length) throws SAXException {		
			tmpValue = new String(ch, start, length);					
	}
 
     };
 
     try{
       saxParser.parse("C:\\Users\\easten\\Documents\\Information Retrieval\\dewiki-20140216-pages-articles-multistream.xml", handler);
     }
       catch (SAXException e) {
    	    if (e instanceof Exception) {
    	    	System.out.println("Finish Parsing"); 	        
    	    }
    	}
     } catch (Exception e) {
       e.printStackTrace();
     }
 
   }
 
}