package de.hpi.krestel.mySearchEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class CombineIndex {
	
	static final String FILEPATH = "/Users/jaeyoonjung/Downloads/oldfile.txt";
	static final String TEMPORAL_FILEPATH = "/Users/jaeyoonjung/Downloads/newfile.txt";
	private List<String> indexFilePaths;
	private List<String> insertWordList;
	

	public CombineIndex(Iterable<String> indexFilePaths){
		this.indexFilePaths = new ArrayList<String>();
		for (String filePath : indexFilePaths) {
			this.indexFilePaths.add(filePath);
		}
	}
	
	public void saveToFile(String combinedIndexPath) throws IOException{
		List<BufferedReader> inputFiles = listOfInputFiles();
		List<String> lines = readOneLineFromEachFile(inputFiles);
		BufferedWriter combinedIndex = openCombinedIndex(combinedIndexPath);
		while (!lines.isEmpty()) {
			writeNextLine(lines, inputFiles, combinedIndex);
		}
		combinedIndex.close();
		deleteInputIndexFiles();
	}
	
	private void deleteInputIndexFiles() {
		for (String filePath : indexFilePaths) {
			File file = new File(filePath); 
			if (file.exists()) {
				boolean fileWasDeleted = file.delete();
				assert fileWasDeleted;
			}
		}
	}
		
	private List<String> readOneLineFromEachFile(
			List<BufferedReader> inputFiles) throws IOException {
		List<String> lines = new ArrayList<String>();
		String line;
		BufferedReader file;
		for (int index = 0; index < inputFiles.size(); ) {
			file = inputFiles.get(index);
			line = file.readLine();
			if (line == null) {
				inputFiles.remove(index).close();
				continue;
			}
			lines.add(line);
			index++;
		}
		return lines;
	}

	private void writeNextLine(List<String> lines,
			List<BufferedReader> inputFiles, BufferedWriter combinedIndex) throws IOException {
		List<Integer> smallestLineIndices = getindicesOfLowestLines(lines);
		mergeLinesIntoOneLineAt(lines, smallestLineIndices, combinedIndex);
		replaceAt(lines, inputFiles, smallestLineIndices);
	}

	private void replaceAt(List<String> lines,
			List<BufferedReader> files,
			List<Integer> indicesToReplace) throws IOException {
		int indexToReplace;
		int lastIndexToReplace = lines.size();
		String line;
		//RandomAccessFile file;
		for (int index = indicesToReplace.size() - 1;  index >= 0; index --) {
			indexToReplace =  indicesToReplace.get(index);
			assert indexToReplace < lastIndexToReplace; // we remove elements so we start at the end and go to the smaller indices because otherwise we would remove the wrng lines
			lastIndexToReplace = indexToReplace;			
			line = files.get(indexToReplace).readLine();
			if (line == null) {
				files.remove(indexToReplace).close();
				lines.remove(indexToReplace);
			} else {
				lines.set(indexToReplace, line);
			}
		}
		
	}

	private void mergeLinesIntoOneLineAt(List<String> lines, List<Integer> lineIndices, BufferedWriter combinedIndex) throws IOException {
		String line;
		String[] splitLine;
		String positionsOfTheWord;
		String wordOfTheLine;
		String word = wordOfLine(lines.get(lineIndices.get(0)));
		combinedIndex.write(word);
		for (int lineIndex : lineIndices) {
			try {							
				line = lines.get(lineIndex);
				splitLine = line.split(" ", 2);
				wordOfTheLine = splitLine[0]; 
				positionsOfTheWord = splitLine[1];
				assert wordOfTheLine == word; // we only merge "documentId:position" for the same word
				combinedIndex.write(" ");
				combinedIndex.write(positionsOfTheWord);
			} catch (ArrayIndexOutOfBoundsException ex){
				System.out.println(lineIndex);
				System.out.println(lines.get(lineIndex));
				continue;
			}
		}
		combinedIndex.write("\n");
	}
	
	private String wordOfLine(String line) {
		return line.split(" ", 2)[0];
	}

	private List<Integer> getindicesOfLowestLines(List<String> lines) {
		String lowestWord = wordOfLine(lines.get(0));
		String wordOfLine;
		ArrayList<Integer> lineIndices = new ArrayList<Integer>();
		int lineIndex = 0;
		int comparism;		
		for (String line : lines) {
			wordOfLine = wordOfLine(line);
			comparism = wordOfLine.compareTo(lowestWord);
			if (comparism == 0) {
				lineIndices.add(lineIndex);
			} else if (comparism < 0) {
				lineIndices = new ArrayList<Integer>();
				lineIndices.add(lineIndex);
				lowestWord = wordOfLine;
			}
			lineIndex++;
		}
		return lineIndices;
	}	

	private BufferedWriter openCombinedIndex(String combinedIndexPath) throws IOException {
		return new BufferedWriter(new FileWriter(combinedIndexPath));
	}

	private List<BufferedReader> listOfInputFiles() throws FileNotFoundException {		
		List<BufferedReader> inputFiles = new ArrayList<BufferedReader>();
		for (String filePath : indexFilePaths) {
			inputFiles.add(new BufferedReader(new FileReader(filePath)));
		}
		return inputFiles;
	}

	public void combineTwoFiles() {
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