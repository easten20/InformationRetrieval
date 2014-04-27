package de.hpi.krestel.mySearchEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;

public class Index {
	
	private long maximumMemoryUsageInBytes = 1024 * 1024 * 1024;
	private List<String> fileIndexPaths;
	private String wikipediaXMLFilePath;
	private MemoryIndex temporaryIndex;
	
	Index (String wikipediaXMLFilePath) {
		this.wikipediaXMLFilePath = wikipediaXMLFilePath;
		temporaryIndex = new MemoryIndex();
	}
		
	//build an index for a  document
	public void addTerms (long documentId, Iterable <String> terms) throws IOException{
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
		return wikipediaXMLFilePath + "." + fileNumber + ".index";
	}
	
	String freeIndexFilePath () {
		int i = 0;
		String filePath;
		while (true) {
			filePath = indexFilePath(i);
			if (!new File(filePath).exists()) {
				return filePath;
			}
			i++;
		}
		
	}

	public void add(WikiPage wikiPage, Iterable<String> tokens) throws IOException {
		addTerms(wikiPage.getPositionInXMLFile(), tokens);
		System.out.println("page Id: " + wikiPage.getId());
		System.out.println(((List<String>) tokens).size());
	}
	
	public String indexFilePath() {
		return wikipediaXMLFilePath + ".index";
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
			i++;
		}
		return indexFilePaths;
	}
	
	private void putAllIndexFilesIntoOneFile() throws IOException {
		CombineIndex combinedIndex = new CombineIndex(allIndexFilePaths());
		combinedIndex.saveToFile(indexFilePath());
	}

	private void createIndexOfIndex() throws IOException {
		fileIndex().createSeekList();
	}
	
	FileIndex fileIndex() {
		return new FileIndex(indexFilePath());
	}

	public boolean isValid() {
		return (new File(seekListFilePath()).exists()) && (new File(indexFilePath()).exists());
	}

	public List<Long> documentPositionsMatchingQuery(Iterable<String> queryTokens) throws IOException {
		return fileIndex().findDocumentPositionsInXMLFile(queryTokens);
	}
	
	public List<WikiPage> wikiPagesMatchingQuery(Iterable<String> queryTokens) throws IOException, XMLStreamException {
		List<WikiPage> wikiPages = new ArrayList<WikiPage> ();
		for (long positionInXMLFile : documentPositionsMatchingQuery(queryTokens)) {
			WikiPage wikiPage = WikiPage.from(this.wikipediaXMLFilePath, positionInXMLFile);
			wikiPages.add(wikiPage);
		}
		return wikiPages;
	}
}
