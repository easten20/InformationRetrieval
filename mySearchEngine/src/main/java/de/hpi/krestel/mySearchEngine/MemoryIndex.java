package de.hpi.krestel.mySearchEngine;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MemoryIndex {

	Map<String, List<String>> index;
	int usedBytes;
	
	public MemoryIndex(){
		
		index = new TreeMap <String, List<String>>();
		usedBytes=0;
	}
	
	public int bytesUsed (){
		
		return usedBytes;
	}
	
	public void add (long documentId, Iterable<String> terms){
		int i = 0;
		for (String term:terms){
			if (term.matches(" |\n")) {
				throw new AssertionError("There must be no ' ' or '\\n' in a term or the index gets destroyed.");
			};
			String entry = Long.toString(documentId) + ":" + Integer.toString(i);
			if (index.containsKey(term)){
				index.get(term).add(entry);
				usedBytes+=entry.length();
			}else{
				List<String> emptyList = new ArrayList<>();
				emptyList.add(entry);
				index.put(term, emptyList);
				usedBytes+=entry.length() + term.length();
			}
			i++;
		}	
	}
	
	public void writeTo (Writer writer) throws IOException{
		for (Entry<String,List<String>> entry : index.entrySet()){
			
			writer.write(entry.getKey().toString());
			for (String value: entry.getValue()){
				writer.write( " " + value.toString());
			}
			writer.write("\n");
		}
	}
	
	public void writeTo(String path) throws IOException{
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "utf-8"));
		writeTo(writer);
		writer.close();
	}

}
