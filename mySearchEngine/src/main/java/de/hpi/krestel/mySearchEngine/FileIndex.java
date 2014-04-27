package de.hpi.krestel.mySearchEngine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class FileIndex {
	
	/**
	 * 
	 * SeekList Format: \n{<word> <indexOfWordInIndexFile>\n} 
	 * Index Format: {<word>{ <DocumentID>:<indexOfWordInDocumentTokens>}\n}
	 * 
	 */

	String indexPath;
	String seekListPath;
	
	FileIndex(String indexPath, String seekListPath) {
		initFileIndex(indexPath, seekListPath);
	}
	
	FileIndex(String indexPath) {
		initFileIndex(indexPath, indexPath + ".skl");
	}
	
	public String indexFilePath() {
		return indexPath;
	}
	
	public String seekListFilePath() {
		return seekListPath;
	}
	
	private void initFileIndex(String indexPath, String seekListPath) {
		this.indexPath = indexPath;
		this.seekListPath = seekListPath;
	}

	RandomAccessFile getIndexReader () throws FileNotFoundException {
		return new RandomAccessFile(indexPath, "r");
	}
	
	RandomAccessFile getSeekListReader () throws IOException {
		if (!hasSeekList()) {
			createSeekList();
		}
		return new RandomAccessFile(seekListPath, "r");
	}
	
	public boolean hasSeekList() {
		return new File(seekListPath).isFile();
	}
	
	public void createSeekList() throws IOException {
		RandomAccessFile writer = new RandomAccessFile(seekListPath, "rw");
		createSeekList(writer);
		writer.close();
	}
	
	void createSeekList(RandomAccessFile writer) throws IOException {
		String line;
		String term;
		int position = 0;
		RandomAccessFile reader = getIndexReader();
		writer.writeBytes("\n");
		while (true) {
			line = reader.readLine();
			if (line == null) {
				break;
			}
			term = line.split(" ", 2)[0];
			writer.writeBytes(term);
			writer.writeBytes(" ");
			writer.writeBytes(Integer.toString(position));
			writer.writeBytes("\n");
			// end of loop
			position += line.length() + 1;
		}
	}
	
	
	public List<Occurence> findDocuments(String word) throws IOException {
		long indexInIndex;
		try {
			indexInIndex = this.findIndexOfWordInSeekList(word);
		} catch (NoSuchElementException e) {
			return new ArrayList<Occurence> ();
		}
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
	private long findIndexOfWordInSeekList(String word) throws IOException, NoSuchElementException {
		// we need to search without UTF8 because then we could not skip to byte positions and had to read the whole file
		String startBytes = new String(Charset.forName("UTF-8").encode(word).array(), "ASCII");
		String line;
		String lineStart;
		RandomAccessFile seekList = getSeekListReader();
		long start = 0;
		long stop = seekListSize();
		long middle;
		int entryLength;
		int comparism;
		while (true) {
			middle = (start + stop) / 2;
			seekList.seek(middle);
			entryLength = seekList.readLine().length(); // skip line
			line = seekList.readLine();
			entryLength += line.length();
			lineStart = line.split(" ", 2)[0];
			comparism = lineStart.compareTo(startBytes);
			if (comparism < 0) {
				start = middle;
			} else if (comparism > 0) {
				stop = middle;
			} else {
				break;
			}
			if (start + entryLength + 1 > stop) {
				throw new NoSuchElementException();
			}
		}
		return start;
	}

	List<Occurence> findDocuments(long indexInIndex, String word) throws IOException {
		RandomAccessFile reader = getIndexReader();
		reader.seek(indexInIndex);
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

	public List<String> findDocumentIds(Iterable<String> words) throws IOException {
		// thanks to http://stackoverflow.com/a/15378048/1320237
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (String word : words) {
			for (Occurence occurence : findDocuments(word)) {
				String documentId = occurence.getDocumentId();
			    if (map.containsKey(documentId)) {
			        map.put(documentId, map.get(documentId) + 1);
			    } else {
			        map.put(documentId, 1);
			    }
			}
		}
		
		ValueComparator<String, Integer> comparator = new ValueComparator<String, Integer> (map);
		Map<String, Integer> sortedMap = new TreeMap<String, Integer> (comparator);
		sortedMap.putAll(map);

		List<String> sortedList = new ArrayList<String> (sortedMap.keySet());
		
		return sortedList;
		
	}
	

	
}

class ValueComparator<K, V extends Comparable<V>> implements Comparator<K> {
	
	Map<K, V> map;
	
	public ValueComparator(Map<K, V> base) {
	    this.map = base;
	}
	
	@Override
	public int compare(K o1, K o2) {
	     return map.get(o2).compareTo(map.get(o1));
	}
}