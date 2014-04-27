package de.hpi.krestel.mySearchEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;

public class Index {
	
	private long maximumMemoryUsageInBytes = 1024 * 1024 * 1024;
	private List<String> fileIndexPaths;
	private String indexPath;
	private MemoryIndex temporaryIndex;
	
	Index (String indexPath) {
		this.indexPath = indexPath;
		temporaryIndex = new MemoryIndex();
	}
		
	//build an index for a  document
	public void addTerms (String documentId, Iterable <String> terms) throws IOException{
		temporaryIndex.add(documentId, terms);
		if (temporaryIndex.bytesUsed() >= maximumMemoryUsageInBytes) {
			writeToDisk();
		}
	}
	
	public void writeToDisk() throws IOException {
		temporaryIndex.writeTo(freeIndexFilePath());
		temporaryIndex = new MemoryIndex();	
	}
	
	String indexFilePath(int fileNumber) {
		return indexPath + "." + fileNumber + ".index";
	}
	
	String freeIndexFilePath () {
		int i = 0;
		String filePath;
		while (true) {
			filePath = indexFilePath(i);
			if (!new File(filePath).exists()) {
				return filePath;
			}
		}
		
	}

	public void add(WikiPage wikiPage, Iterable<String> tokens) throws IOException {
		addTerms(wikiPage.getId(), tokens);
		System.out.println("page Id: " + wikiPage.getId());
		System.out.println(((List<String>) tokens).size());
	}
	
	public String indexFilePath() {
		return indexPath + ".index";
	}
	
	public String seekListFilePath () {
		return new FileIndex(indexFilePath()).seekListFilePath();
	}
	
	public void save() throws IOException {
		writeToDisk();
		putAllIndexFilesIntoOneFile();
		createIndexOfIndex();
	}

	Iterable<String> allIndexFilePaths() {
		List<String> indexFilePaths = new ArrayList<String> ();
		String filePath; 
		int i = 0;
		while (true) {
			filePath = indexFilePath(i);
			if (! new File(filePath).exists()) {
				break;
			}
			indexFilePaths.add(filePath);
		}
		return indexFilePaths;
	}
	
	private void putAllIndexFilesIntoOneFile() throws IOException {
		CombineIndex combinedIndex = new CombineIndex(allIndexFilePaths());
		combinedIndex.saveToFile(indexFilePath());
	}

	private void createIndexOfIndex() throws IOException {
		new FileIndex(indexFilePath()).createSeekList();
	}

}
