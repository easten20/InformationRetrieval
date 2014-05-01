package de.hpi.krestel.mySearchEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CombineIndex {
	
	static final String FILEPATH = "/Users/jaeyoonjung/Downloads/oldfile.txt";
	static final String TEMPORAL_FILEPATH = "/Users/jaeyoonjung/Downloads/newfile.txt";
	private ArrayList<String> insertWordList;

	public CombineIndex(ArrayList<String> insertWordList){
		this.insertWordList = insertWordList;
	}
	
	public void Combine(){
	
		try {
			
			//make a temporal file to contain both index txt file and memory hashmap
			File newFile = new File(TEMPORAL_FILEPATH);
			
			//if the file doesn't exist, make a new file
			if (!newFile.exists()) {
				newFile.createNewFile();
			}
			
			//make FileWrite and BufferedWrite to prepare for writing
			FileWriter fw = new FileWriter(newFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			//make BufferedReader to read from index txt file
			BufferedReader br = new BufferedReader(new FileReader(FILEPATH));
			String line;
			
			//read in line by line
			while ((line = br.readLine()) != null) {
				
				for(int i = 0 ; i < insertWordList.size() ; i++){
					
					String insertWord = insertWordList.get(i);
					
					if(insertWord.compareTo(line) > 0) break;
					//if insertWord equals to line, combine them. 
					else if(insertWord.compareTo(line) == 0) break;
					else{
						bw.write(insertWord + "\n");
						insertWordList.remove(insertWord);
					}
					
				}//end for
				
				bw.write(line + "\n");
				
			}//end while
			
			//close BufferedReader and BufferedWriter
			br.close();
			bw.close();
			
			//delete oldfile
			File oldFile = new File(FILEPATH);
    		if(oldFile.delete()){
    			System.out.println(oldFile.getName() + " is deleted!");
    		}else{
    			System.out.println("Delete operation is failed.");
    		}
    		
    		//rename "newfile" to "oldfile"
    		File fromThisName =new File(TEMPORAL_FILEPATH);
    		File toThisName =new File(FILEPATH);
     
    		if(fromThisName.renameTo(toThisName)){
    			System.out.println("Rename succesful");
    		}else{
    			System.out.println("Rename failed");
    		}
			
		} catch (IOException e) {
			e.printStackTrace();
		}//end try and catch
		
	}//end Combine
		
}//end class