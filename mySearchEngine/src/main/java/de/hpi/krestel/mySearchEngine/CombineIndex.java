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
	private Iterable<String> indexFilePaths;
	private List<String> insertWordList;
	

	public CombineIndex(Iterable<String> indexFilePaths){
		this.indexFilePaths = indexFilePaths;
	}
	
	public void saveToFile(String combinedIndexPath) throws IOException{
		List<RandomAccessFile> inputFiles = listOfInputFiles();
		List<String> lines = readOneLineFromEachFile(inputFiles);
		RandomAccessFile combinedIndex = openCombinedIndex(combinedIndexPath);
		while (!lines.isEmpty()) {
			writeNextLine(lines, inputFiles, combinedIndex);
		}
		combinedIndex.close();
	}
		
	private List<String> readOneLineFromEachFile(
			List<RandomAccessFile> inputFiles) throws IOException {
		List<String> lines = new ArrayList<String>();
		String line;
		RandomAccessFile file;
		for (int index = 0; index < inputFiles.size(); ) {
			file = inputFiles.get(index);
			line = file.readLine();
			if (line == null) {
				inputFiles.remove(index);
				continue;
			}
			lines.add(line);
			index++;
		}
		return lines;
	}

	private void writeNextLine(List<String> lines,
			List<RandomAccessFile> inputFiles, RandomAccessFile combinedIndex) throws IOException {
		List<Integer> smallestLineIndices = getindicesOfLowestLines(lines);
		mergeLinesIntoOneLineAt(lines, smallestLineIndices, combinedIndex);
		replaceAt(lines, inputFiles, smallestLineIndices);
	}

	private void replaceAt(List<String> lines,
			List<RandomAccessFile> files,
			List<Integer> indicesToReplace) throws IOException {
		int indexToReplace;
		int lastIndexToReplace = lines.size();
		String line;
		RandomAccessFile file;
		for (int index = indicesToReplace.size() - 1;  index >= 0; index --) {
			indexToReplace =  indicesToReplace.get(index);
			assert indexToReplace < lastIndexToReplace; // we remove elements so we start at the end and go to the smaller indices because otherwise we would remove the wrng lines
			lastIndexToReplace = indexToReplace;
			file = files.get(indexToReplace);
			line = file.readLine();
			if (line == null) {
				files.remove(indexToReplace);
				lines.remove(indexToReplace);
			} else {
				lines.set(indexToReplace, line);				
			}
		}
		
	}

	private void mergeLinesIntoOneLineAt(List<String> lines, List<Integer> lineIndices, RandomAccessFile combinedIndex) throws IOException {
		String line;
		String[] splitLine;
		String positionsOfTheWord;
		String wordOfTheLine;
		String word = wordOfLine(lines.get(lineIndices.get(0)));
		combinedIndex.writeBytes(word);
		for (int lineIndex : lineIndices) {
			line = lines.get(lineIndex);
			splitLine = line.split(" ", 2);
			wordOfTheLine = splitLine[0]; 
			positionsOfTheWord = splitLine[1];
			assert wordOfTheLine == word; // we only merge "documentId:position" for the same word
			combinedIndex.writeBytes(" ");
			combinedIndex.writeBytes(positionsOfTheWord);
		}
		combinedIndex.writeBytes("\n");
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
			}
			lineIndex++;
		}
		return lineIndices;
	}

	private RandomAccessFile openCombinedIndex(String combinedIndexPath) throws FileNotFoundException {
		return new RandomAccessFile(combinedIndexPath, "rw");
	}

	private List<RandomAccessFile> listOfInputFiles() throws FileNotFoundException {
		List<RandomAccessFile> inputFiles = new ArrayList<RandomAccessFile>();
		for (String filePath : indexFilePaths) {
			inputFiles.add(new RandomAccessFile(filePath, "r"));
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