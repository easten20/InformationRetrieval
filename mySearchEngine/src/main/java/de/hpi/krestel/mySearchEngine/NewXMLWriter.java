package de.hpi.krestel.mySearchEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringEscapeUtils;

import de.hpi.krestel.mySearchEngine.domain.WikiPage; 
import de.hpi.krestel.mySearchEngine.parser.BufferedRaf;

public class NewXMLWriter {
	
	String XMLFilePath;
	RandomAccessFile  randomAccessFile;
	
	public NewXMLWriter(String XMLFilePath){
		try {			
			this.XMLFilePath = XMLFilePath + ".copy";			
			this.randomAccessFile = new RandomAccessFile(this.XMLFilePath, "rw");			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public String getCopyName(){
		return this.XMLFilePath;
	}

	public void writeNewXMLFile(WikiPage wikipage) throws IOException{					
		wikipage.setPositionInXMLFile(this.randomAccessFile.getFilePointer());				
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<page>\n");
		strBuilder.append("<title>\n");
		strBuilder.append(wikipage.getTitle() + "\n");		
		strBuilder.append("</title>" + "\n");
		strBuilder.append("<text xml:space=\"preserve\">" + "\n");
		strBuilder.append(StringEscapeUtils.escapeXml(wikipage.getText()) + "\n");		
		strBuilder.append("</text>" + "\n");				
		strBuilder.append("</page>" + "\n");
		this.randomAccessFile.write(strBuilder.toString().getBytes(Charset.forName("UTF-8")));		
	}
	
	public void close() throws IOException{
		this.randomAccessFile.close();
	}

}
