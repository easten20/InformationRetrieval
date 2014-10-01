package de.hpi.krestel.mySearchEngine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// Don't change this file!
public abstract class SearchEngine {

	//String baseDirectory = "/home/krestel/data/wikipedia-de/";
	String baseDirectory = "/Users/jaeyoonjung/HPI/InformationRetrieval/mySearchEngine/res/";
	String wikiDirectory;
	String directory;
	String logFile;


	public SearchEngine() {

		// Directory to store index and result logs
		this.directory = this.baseDirectory +this.getClass().getSimpleName().toString();
		new File(this.directory).mkdirs();
		this.logFile = this.directory +"/" +System.currentTimeMillis() +".log";
		// Directory to store wikipedia results
		this.wikiDirectory = this.baseDirectory +"wikiQueryResults/";
		new File(this.wikiDirectory).mkdirs();

	}

	void indexWrapper(){

		long start = System.currentTimeMillis();
		if(!loadIndex(this.directory)){
			index(this.directory);
			loadIndex(this.directory);
		}
		long time = System.currentTimeMillis() - start;
		log("Index Time: " +time +"ms");
	}


	void searchWrapper(String query, int topK, int prf){

		long start = System.currentTimeMillis();
		ArrayList<String> ranking = search(query, topK, prf);
		long time = System.currentTimeMillis() - start;
		ArrayList<String> goldRanking = getGoldRanking(query);
		Double ndcg = computeNdcg(goldRanking, ranking, topK);
		String output = "\nQuery: " +query +"\t Query Time: " +time +"ms\nRanking: ";
		System.out.println("query: " +query);
		if(ranking!=null){
			Iterator<String> iter = ranking.iterator();
			while(iter.hasNext()){
				String item = iter.next();
				output += item +"\n";
				//		System.out.println(item);
			}
		}
		output += "\nnDCG@" +topK +": " +ndcg;
		log(output);
	}

	ArrayList<String> getGoldRanking(String query) {
		
		//int numResults = 100;
		ArrayList<String> gold;
		String queryTerms = query.replaceAll(" ", "+");
		try{
			FileInputStream streamIn = new FileInputStream(this.wikiDirectory +queryTerms +".ser");
			ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
			gold = (ArrayList<String>) objectinputstream.readObject();
			System.out.println("gold returned");
			return gold;
		}catch(Exception ex){}

		gold = new ArrayList<String>();
		String url = "http://de.wikipedia.org/w/index.php?title=Spezial%3ASuche&search=" +queryTerms +"&fulltext=Search&profile=default";
		String wikipage = "";	
		try {
			wikipage = (String) new WebFile(url).getContent();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownServiceException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] lines = wikipage.split("\n");
		
		//debug, print lines
		System.out.println("+++string[] lines+++");
		for ( int i = 0 ; i < lines.length ; i ++ ) {
			System.out.println( i + lines[i] );
		}
		
		//convert lines to a string
		String wikipageSource = "";
		StringBuilder builder = new StringBuilder();
		for(String s : lines) {
		    builder.append(s);
		}
		wikipageSource = builder.toString();
		
		//parse HTML
		Document doc = Jsoup.parse(wikipageSource);
		Elements elements = doc.select("div.mw-search-result-heading");
		for ( Element element : elements ) {
			Elements linkElements = element.select("a[href]");
			for ( Element linkElement : linkElements ) {
				String attr = linkElement.attr("title");
				System.out.println("attr: " + attr);
				gold.add(attr);
				break;
			}
		}
		
		
//		for(int i=0;i<lines.length;i++){
//			if(lines[i].startsWith("<li><div class='mw-search-results-heading'>")){
//			//if(lines[i].startsWith("<li>")){
//				
//				doc = (Document) Jsoup.parse(lines[i]);
//				Iterator<Element> productList = ((Element) doc).select("div[class=mw-search-results-heading]").iterator();
//				//assertNotNull(productList.hasNext);
//				while (productList.hasNext()) {
//					Element productLink = product.select("a").first();
//					String href = productLink.attr("abs:href");
//				}
//				
////				Pattern p = Pattern.compile("title=\"(.*)\"");
////				Matcher m = p.matcher(lines[i]);
////				m.find();
////				gold.add(m.group(1));
//				
//				//debug
//				System.out.println("m.group(1): " + m.group(1));
//			}
//		}//end for		
		
		try {
			FileOutputStream fout = new FileOutputStream(this.wikiDirectory +queryTerms +".ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(gold);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//debug
		System.out.println("gold return: " + gold);
		
		return gold;
	}

	synchronized void log(String line) {

		try {
			FileWriter fw = new FileWriter(this.logFile,true);
			BufferedWriter out = new BufferedWriter(fw);
			out.write(line +"\n");
			out.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	abstract boolean loadIndex(String directory);

	abstract void index(String directory);

	abstract ArrayList<String> search(String query, int topK, int prf);

	abstract Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> myRanking, int at);
}