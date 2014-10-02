package de.hpi.krestel.mySearchEngine.score;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.krestel.mySearchEngine.Index;
import de.hpi.krestel.mySearchEngine.domain.DocumentOcc;
import de.hpi.krestel.mySearchEngine.domain.Term;

public class BM25_New {
	
	Index index;
	List<Term> terms;
	DocumentOcc docOcc;
	double k1 = 1.2f;
	double b = 0.75f;
	double k2 = 100;	 
	
	public BM25_New (Index index, List<Term> searchTerms, DocumentOcc docOcc) {
		this.index = index;
		this.terms = searchTerms;
		this.docOcc = docOcc;		
	}
	
	public double compute() throws IOException {
		double score = 0;
		// normalizes TF component document length
		if(this.docOcc.getScore() != 0)
			return this.docOcc.getScore();
		double dl = index.averageNumberOfDocumentLength(); //since we don't have document statistic we assume all doc length similar
		double avdl = index.averageNumberOfDocumentLength();
		double K = k1 * ((1 - b) + b * dl / avdl);		
		Set<Term> setTerms = new HashSet<Term>(this.terms);
		boolean isAllTerms = true;
		for (Term term : setTerms) {
			// count of term i in document
			int fi = docOcc.getTermsInOcc(term);
			if (fi == 0)
				isAllTerms = false;				
			// count of term i in query
			int qfi = countOfTermInQuery(term);
			// k1 and k2 are set empirically
			// number of relevant documents containing term i
			int ri = 0;
			// number of relevant documents for query
			int R = 0;
			// number of documents containing term i
			long ni = index.numberOfDocumentsContaining(term);
			// total number of documents
			int N = index.totalNumberOfDocumets();
			//I added one at inside the log to remove minus
			double idf = Math.log(((ri + 0.5) / (R - ri + 0.5)) / ((ni - ri + 0.5) / (N - ni - R + ri + 0.5)) + 1); 
			score += idf * ((k1 + 1) * fi) / (K + fi) * ((k2 + 1) * qfi) / (k2 + qfi);			
		}
		if (isAllTerms){
			score += 10;
		}
		this.docOcc.setScore(score);
		//wikiPage.setScore(score);		
		return score;
	}

	private int countOfTermInQuery(Term term) {
		int count = 0;
		for (Term term1: this.terms){			
			if (term1.getText().compareTo(term.getText()) == 0)
				count++;
		}
		return count;
	}		
}
