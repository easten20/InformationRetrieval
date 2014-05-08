package de.hpi.krestel.mySearchEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.germanStemmer;

public class TokenStream {
	
	private StopWord cachedStopWord;
	private SnowballStemmer cachedStemmer;
	
	public TokenStream(){
		
	}
	
	public List<String> preprocessText(String text) {
		List<String> tokens = tokenizeWikiText(text);
		tokens = removeStopWords(tokens);
		tokens = stemText(tokens);
		return tokens;
	}											
	
	public List<String> tokenizeWikiText(String wikiText) {
		// TODO: most unicode characters that are higher than 128 are just usual characters
		//       maybe we can split with this in  mind
		System.out.println(wikiText);		
		//thks to http://stackoverflow.com/questions/163360/regular-expresion-to-match-urls-in-java
		String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";		
		wikiText = wikiText.toLowerCase().replaceAll(regex, "");
		System.out.println(wikiText);
		String[] tokens = wikiText.replaceAll("[^a-zA-Z ]", " ").split("\\s+");		
		return Arrays.asList(tokens);
	}	
	
	List<String> removeStopWords(Iterable<String> tokens) {		
		List<String> stopWordFreeTokens = new ArrayList<String>();
		for (String token : tokens) {
			if (isStopWord(token))
				continue;
			stopWordFreeTokens.add(token);
		}
		return stopWordFreeTokens;
	}
	
	Boolean isStopWord(String token) {
		StopWord stopWord = getStopWord();				
		return (token.length() < 3 || stopWord.GetHashSet().contains(token));		
	}		
	
	StopWord getStopWord() {
		if (cachedStopWord == null) {
			cachedStopWord = StopWord.StopWordFromFiles(); 
		}
		return cachedStopWord;
	}
	
	SnowballStemmer getStemmer() {
		if (cachedStemmer == null) {
			cachedStemmer = new germanStemmer();
		}
		return cachedStemmer;
	}
	
	List<String> stemText(Iterable<String> tokens) {			
		List<String> stemmedTokens = new ArrayList<String>();
		for (String token : tokens) {						
			stemmedTokens.add(this.stemText(token));
		}
		return stemmedTokens;
	}
	
	 String stemText(String token) {	
		SnowballStemmer stemmer  = this.getStemmer();							
		stemmer.setCurrent(token); 		
		stemmer.stem();
		token = stemmer.getCurrent();
		// remove the s at the end
		token = token.replaceAll("s+$", "");					
		return token;
	}

	
	
	
}
