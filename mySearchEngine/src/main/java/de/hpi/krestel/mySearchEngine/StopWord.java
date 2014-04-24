package de.hpi.krestel.mySearchEngine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class StopWord {
	private HashSet<String> stopWords;	
	
	public StopWord(){		
		stopWords = new HashSet<String>();
	}
	
	public void FillStopWord(String fileName){
		this.GetFromFile(fileName);
	}
	
	public HashSet<String> GetHashSet() {
		return stopWords;
	}
	
	private void GetFromFile(String fileName){
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
		    for(String line; (line = br.readLine()) != null; ) {
		    	stopWords.add(line);
		    }
		    // line is not visible here.
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
