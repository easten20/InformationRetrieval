package de.hpi.krestel.mySearchEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class FileIndex {

	String indexPath;
	String seekListPath;
	
	FileIndex(String indexPath, String seekListPath) {
		initFileIndex(indexPath, seekListPath);
	}
	
	FileIndex(String indexPath) {
		initFileIndex(indexPath, indexPath + ".skl");
	}
	
	private void initFileIndex(String indexPath, String seekListPath) {
		this.indexPath = indexPath;
		this.seekListPath = seekListPath;
	}

	BufferedReader getIndexReader () throws UnsupportedEncodingException, FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(indexPath), "utf-8"));
	}
	
	void createSeekList() throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(seekListPath), "utf-8"));
		createSeekList(writer);
		writer.close();
	}
	
	void createSeekList(Writer writer) throws IOException {
		String line;
		String term;
		int position = 0;
		BufferedReader reader = getIndexReader();
		while (true) {
			line = reader.readLine();
			if (line == null) {
				break;
			}
			term = line.split(" ", 1)[0];
			writer.write(term);
			writer.write(" ");
			writer.write(Integer.toString(position));
			writer.write("\n");
			// end of loop
			position += line.length() + 1;
		}
	}
	
	
	
	List<Occurence> findDocuments(String word) throws IOException {
		int indexInIndex = this.findIndexOfWordInSeekList(word);
		return this.findDocuments(indexInIndex, word);
	}
	
	private int findIndexOfWordInSeekList(String word) {
		// TODO Auto-generated method stub
		// TODO: binary search the index file
		return 0;
	}

	List<Occurence> findDocuments(int index, String word) throws IOException {
		BufferedReader reader = getIndexReader();
		assert reader.skip(index) == index;
		return documentsFor(word, reader.readLine());
	}
	
	private List<Occurence> documentsFor(String word, String line) {
		List<Occurence> occurences = new ArrayList<>();
		String[] splitLine = line.split(" ");
		assert (word==splitLine[0]);
		for (String item : splitLine){
			if (item==word)
				continue;
			occurences.add(new Occurence(item, word));
			
		}
		// TODO: parse the line to occurences
		return occurences;
	}

	
}
