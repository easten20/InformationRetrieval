package de.hpi.krestel.mySearchEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import de.hpi.krestel.mySearchEngine.domain.StarOp;
import de.hpi.krestel.mySearchEngine.domain.Term;
import de.hpi.krestel.mySearchEngine.parser.BufferedRaf;

public class FileIndex {
	
	/**
	 * 
	 * SeekList Format: \n{<word> <indexOfWordInIndexFile>\n} 
	 * Index Format: {<word>{ <DocumentID>:<indexOfWordInDocumentTokens>}\n}
	 * 
	 */

	String indexPath;
	String seekListPath;
	String xmlPath;
	Map<String, List<Occurence>> cachedFindDocuments;
	BufferedReader myReader;
	BufferedRaf bufferedRaf;
	BufferedRaf seekBufferedRaf;
	
	
	FileIndex(String xmlPath, String indexPath, String seekListPath) {		
		initFileIndex(xmlPath, indexPath, seekListPath);
	}
	
	FileIndex(String xmlPath, String indexPath) {
		initFileIndex(xmlPath, indexPath, indexPath + ".skl");
	}
	
	FileIndex(String xmlPath) {
		initFileIndex(xmlPath, xmlPath + ".index", xmlPath + ".index.skl");
	}
		
	public String indexFilePath() {
		return indexPath;
	}
	
	public String seekListFilePath() {
		return seekListPath;
	}
	
	private void initFileIndex(String xmlPath, String indexPath, String seekListPath) {
		cachedFindDocuments = new HashMap<>();		
		this.indexPath = indexPath;
		this.seekListPath = seekListPath;
		this.xmlPath = xmlPath;		
	}

	BufferedReader getIndexReader() throws FileNotFoundException {
		if (this.myReader == null)
			this.myReader = new BufferedReader(new FileReader(this.indexPath));		
		return this.myReader;		
	}			
	
	BufferedRaf getIndexReaderRaf() throws FileNotFoundException {
		if (this.bufferedRaf == null)
			this.bufferedRaf = new BufferedRaf(new File(this.indexPath), "r");		
		return this.bufferedRaf;		
	}			
	
	BufferedRaf getSeekReaderRaf() throws FileNotFoundException {
		if (this.seekBufferedRaf == null)
			this.seekBufferedRaf = new BufferedRaf(new File(this.seekListPath), "r");		
		return this.seekBufferedRaf;		
	}			
	
	RandomAccessFile getSeekListReader() throws IOException {
		if (!hasSeekList()) {
			createSeekList();
		}
		return this.getSeekReaderRaf();
	}
	
	public boolean hasSeekList() {
		return new File(seekListPath).isFile();
	}
	
	public void createSeekList() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(seekListPath));
		createSeekList(writer);
		writer.close();
	}
	
	void createSeekList(BufferedWriter writer) throws IOException {
		String line;
		String term;
		long position = 0;
		BufferedReader reader = getIndexReader();
		writer.write("\n");
		while (true) {
			line = reader.readLine();
			if (line == null) {
				break;
			}
			term = line.split(" ", 2)[0];
			writer.write(term);
			writer.write(" ");
			writer.write(Long.toString(position));
			writer.write("\n");
			// end of loop
			position += line.length() + 1;
		}
	}
	
	
	public List<Occurence> findDocuments(String word) throws IOException {
		long indexInIndex;
		try {
			if (this.cachedFindDocuments.get(word) != null)
				return this.cachedFindDocuments.get(word);
			indexInIndex = this.findIndexOfWordInSeekList(word);
		} catch (NoSuchElementException e) {
			//System.out.println("ERROR: Word not found: " + word);
			return new ArrayList<Occurence> ();
		}
		return this.findDocuments(indexInIndex, word);
	}
	
	public List<Occurence> findDocuments(Term term) throws IOException {
		if (this.cachedFindDocuments.get(term.getText()) != null)
			return this.cachedFindDocuments.get(term.getText());
		long indexInIndex;
		List<Occurence> occurenceL = new ArrayList<Occurence>();		
		try {			
			if (term.getStarOp() == StarOp.NOSTAR){
				indexInIndex = this.findIndexOfWordInSeekList(term.getText());
				occurenceL.addAll(this.findDocuments(indexInIndex, term.getText()));
			}
			else {
				FileInputStream fis = new FileInputStream(this.seekListPath);
				BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
				String line, splitLine[];						
				while ((line=reader.readLine())!=null) {                
					splitLine = line.split(" ", 2);
					if (term.isRegexMatch(splitLine[0])){
						occurenceL.addAll(this.findDocuments(Long.parseLong(splitLine[1]),splitLine[0]));
					}
            	}		
				reader.close();
				fis.close();
			}
		} catch (NoSuchElementException e) {
			//System.out.println("ERROR: Word not found: " + term.getText());
			return occurenceL;
		}		
		return occurenceL;
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
		// TODO: startBytes need to be a byte string
		String startBytes = word;//new String(Charset.forName("UTF-8").encode(word).array(), "ASCII");
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
			if (line == null) {
				throw new AssertionError("Somehow we iterate over an empty file. This is Wrong!");
			}
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
		if (this.cachedFindDocuments.get(word) != null)
			return this.cachedFindDocuments.get(word);			
		BufferedRaf bufferedRaf = this.getIndexReaderRaf();
		bufferedRaf.seek(indexInIndex);		
		List<Occurence> occurences = documentsFor(word, bufferedRaf.readLine());
		this.cachedFindDocuments.put(word, occurences);
		return occurences;
	}
	
	private List<Occurence> documentsFor(String word, String line) {
		List<Occurence> occurences = new ArrayList<>();
		String[] splitLine = line.split(" ");
		assert (word.equals(splitLine[0]));
		for (String item : splitLine){
			if (item.equals(word)) continue; // skip word
			occurences.add(new Occurence(item, word, xmlPath));
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
	
	public List<Long> findDocumentPositionsInXMLFile(Term term) throws IOException {
		// thanks to http://stackoverflow.com/a/15378048/1320237
		Map<Long, Integer> map = new HashMap<Long, Integer>();
		for (Occurence occurence : findDocuments(term)) {
			long documentId = occurence.getPositionOfDocumentInXMLFile();
			if (map.containsKey(documentId)) {
				map.put(documentId, map.get(documentId) + 1);
			} else {
				map.put(documentId, 1);
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