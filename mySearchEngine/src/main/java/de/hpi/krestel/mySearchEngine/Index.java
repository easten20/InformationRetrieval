package de.hpi.krestel.mySearchEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;

import de.hpi.krestel.mySearchEngine.domain.WikiPage;

public class Index {
	
	private long maximumMemoryUsageInBytes = 128 * 1024 * 1024;
	private String wikipediaXMLFilePath;
	private MemoryIndex temporaryIndex;
	long wikipediaXMLFileSize;
	private WikiPage lastWikiPage;
	
	Index (String wikipediaXMLFilePath) {
		this.wikipediaXMLFilePath = wikipediaXMLFilePath;
		assert new File(wikipediaXMLFilePath).isFile();
		wikipediaXMLFileSize = new File(wikipediaXMLFilePath).length();
		if (wikipediaXMLFileSize == 0) {
			wikipediaXMLFileSize = 1;
		}
		temporaryIndex = new MemoryIndex();
	}
		
	//build an index for a  document
	public void addTerms (long positionInXMLFile, Iterable <String> terms) throws IOException{
		temporaryIndex.add(positionInXMLFile, terms);
	}
	
	
	///// start: make index resumable
	
	public void writeToDisk() throws IOException {
		temporaryIndex.writeTo(freeIndexFilePath());
		markLastLocationInWikiFile();
		temporaryIndex = new MemoryIndex();	
	}
	
	public long getlastPositionInXMLFile() {
		RandomAccessFile randomAccessFile;
		String line;
		try {
			randomAccessFile = new RandomAccessFile(lastLocationPath(), "r");
		} catch (FileNotFoundException e) {
			return 0;
		}
		try {
			line = randomAccessFile.readLine();
			randomAccessFile.close();
		} catch (IOException e) {
			return 0;
		}
		if (line == null) {
			return 0;
		}
		try {
			return Long.parseLong(line);
		} catch (NumberFormatException e) {
			return 0;
		}	
	}
	
	String lastLocationPath() {
		return wikipediaXMLFilePath + ".lastPosition";
	}
	
	void markLastLocationInWikiFile() throws FileNotFoundException, IOException {
		if (lastWikiPage == null) {
			return ;
		}
		RandomAccessFile randomAccessFile = new RandomAccessFile(lastLocationPath(), "rw");
		randomAccessFile.writeBytes(Long.toString(lastWikiPage.getStopPositionInXMLFile()));
		randomAccessFile.close();
	}
	
	//// end: make index resumable
	
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
		lastWikiPage = wikiPage;
		addTerms(wikiPage.getPositionInXMLFile(), tokens);
		if (temporaryIndex.bytesUsed() >= maximumMemoryUsageInBytes) {
			writeToDisk();
		}
		System.out.println("page Id: " + wikiPage.getId() + " " + (wikiPage.getPositionInXMLFile() / (wikipediaXMLFileSize / 100) + "%"));
		System.out.println(((List<String>) tokens).size());
	}
	
	public String indexFilePath() {
		return wikipediaXMLFilePath + ".index";
	}
	
	public String seekListFilePath () {
		return new FileIndex(wikipediaXMLFilePath, indexFilePath()).seekListFilePath();
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
