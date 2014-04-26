package de.hpi.krestel.mySearchEngine;

import java.util.Map;
import java.util.TreeMap;

public class Index {
	
		
	//build an index for a  document
	public Map<String, Integer> addTerms (String documentId, Iterable <String> terms){
		int id = Integer.parseInt(documentId);
		Map<String, Integer> index = new TreeMap<String, Integer>();
		for (String term: terms){
			index.put(term, id);
		}
		return index;
	}

}
