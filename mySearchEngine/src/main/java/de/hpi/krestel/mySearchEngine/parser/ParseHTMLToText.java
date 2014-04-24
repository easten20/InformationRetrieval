package de.hpi.krestel.mySearchEngine.parser;

import java.io.IOException;
import java.io.StringReader;

import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

public class ParseHTMLToText extends ParserCallback {
	private StringBuilder strBuilder;

    public void handleText(char[] data, int pos) {
        strBuilder.append(new String(data));
    }
    
    public String parseHTML(String html){
    	try {
    		strBuilder = new StringBuilder();
    		ParserDelegator parserDelegator = new ParserDelegator();      		
			parserDelegator.parse(new StringReader(html), this, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return strBuilder.toString();    			
    }
}
