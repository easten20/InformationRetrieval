package de.hpi.krestel.mySearchEngine;

import java.io.IOException;
import java.util.List;

import de.hpi.krestel.mySearchEngine.domain.Term;
import de.hpi.krestel.mySearchEngine.domain.WikiPage;

public class BM25 {
	
	Index index;
	List<Term> terms;
	WikiPage wikiPage;
	double k1 = 1.2f;
	double b = 0.75f;
	double k2 = 100;
	
	BM25 (Index index, List<Term> searchTerms, WikiPage wikiPage) {
		this.index = index;
		this.terms = searchTerms;
		this.wikiPage = wikiPage;
	}
	
	public double compute() throws IOException {
		double score = 0;
		// normalizes TF component document length
		double dl = wikiPage.numberOfTerms();
		long avdl = index.averageNumberOfDocumentLength();
		double K = k1 * ((1 - b) + b * dl / avdl);
		for (Term term : terms) {
			// count of term i in document
			int fi = wikiPage.countOfTerm(term);
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
			double idf = Math.log(((ri + 0.5) / (R - ri + 0.5)) / 
							(ni - ri + 0.5) / (N - ni - R + ri + 0.5)); 
			score += idf * 
					 (k1 + 1) * fi / (K + fi) *
					 (k2 + 1) * qfi / (k2 + qfi);
		}
		return score;
	}

	private int countOfTermInQuery(Term term) {
		return 1;
	};
	
	

}
