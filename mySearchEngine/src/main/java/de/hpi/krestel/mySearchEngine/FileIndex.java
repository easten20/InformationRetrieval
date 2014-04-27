package de.hpi.krestel.mySearchEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
	
	BufferedReader getSeekListReader () throws IOException {
		if (!hasSeekList()) {
			createSeekList();
		}
		return new BufferedReader(new InputStreamReader(new FileInputStream(seekListPath), "utf-8"));
	}
	
	public boolean hasSeekList() {
		return new File(seekListPath).isFile();
	}
	
	public void createSeekList() throws IOException {
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
	
	
	
	public List<Occurence> findDocuments(String word) throws IOException {
		int indexInIndex = this.findIndexOfWordInSeekList(word);
		return this.findDocuments(indexInIndex, word);
	}
	
	public long seekListSize() {
		assert hasSeekList(); // seek list mus be created
		return new File(seekListPath).length();
	}
	
	/**
	 * 
	 * @param word
	 * @return seek index of line starting with word or -1 otherwise
	 * @throws IOException 
	 */
	private int findIndexOfWordInSeekList(String word) throws IOException {
		// TODO: binary search the index index file
		BufferedReader seekList = getSeekListReader();
		long start = 0;
		long stop = seekListSize();
		return 0;// TODO
	}

	List<Occurence> findDocuments(int index, String word) throws IOException {
		BufferedReader reader = getIndexReader();
		assert reader.skip(index) == index;
		return documentsFor(word, reader.readLine());
	}
	
	private List<Occurence> documentsFor(String word, String line) {
		List<Occurence> occurences = new ArrayList<>();
		String[] splitLine = line.split(" ");
		assert (word.equals(splitLine[0]));
		for (String item : splitLine){
			if (item.equals(word)) continue; // skip word
			occurences.add(new Occurence(item, word));
		}
		return occurences;
	}

	
}
