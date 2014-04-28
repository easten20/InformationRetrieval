package de.hpi.krestel.mySearchEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
			System.out.println("ERROR: Word not found: " + word);
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
		String[] splitLine;
		RandomAccessFile seekList = getSeekListReader();
		long start = 0;
		long stop = seekListSize();
		long middle;
		int entryLength;
		int comparism;
		while (true) {
			middle = (start + stop) / 2;
			seekList.seek(middle);
			seekList.readLine(); // skip line
			line = seekList.readLine();
			entryLength = line.length() + 1;
			splitLine = line.split(" ", 2);
			lineStart = splitLine[0];
			comparism = lineStart.compareTo(startBytes);
			if (comparism < 0) {
				start = middle;
			} else if (comparism > 0) {
				stop = middle;
			} else {
				return Integer.parseInt(splitLine[1]);
			}
			if (start + entryLength + 1 > stop) {
				throw new NoSuchElementException();
			}
		}
		
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

	public List<Long> findDocumentPositionsInXMLFile(Iterable<String> words) throws IOException {
		// thanks to http://stackoverflow.com/a/15378048/1320237
		Map<Long, Integer> map = new HashMap<Long, Integer>();
		for (String word : words) {
			for (Occurence occurence : findDocuments(word)) {
				long documentId = occurence.getPositionOfDocumentInXMLFile();
			    if (map.containsKey(documentId)) {
			        map.put(documentId, map.get(documentId) + 1);
			    } else {
			        map.put(documentId, 1);
			    }
			}
		}

		List<Long> sortedList = getWordInDescendingFreqOrder(map);
		
		return sortedList;
		
	}
	
	static List<Long> getWordInDescendingFreqOrder(Map<Long, Integer> wordCount) {
		// thanks to http://stackoverflow.com/a/10159540/1320237

	    // Convert map to list of <String,Integer> entries
	    List<Map.Entry<Long, Integer>> list = 
	        new ArrayList<Map.Entry<Long, Integer>>(wordCount.entrySet());

	    // Sort list by integer values
	    Collections.sort(list, new Comparator<Map.Entry<Long, Integer>>() {
	        public int compare(Map.Entry<Long, Integer> o1, Map.Entry<Long, Integer> o2) {
	            // compare o2 to o1, instead of o1 to o2, to get descending freq. order
	            return (o2.getValue()).compareTo(o1.getValue());
	        }
	    });

	    // Populate the result into a list
	    List<Long> result = new ArrayList<Long>();
	    for (Map.Entry<Long, Integer> entry : list) {
	        result.add(entry.getKey());
	    }
	    return result;
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