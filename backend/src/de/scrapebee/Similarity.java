package de.scrapebee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.scrapebee.vectorspace.DocumentPair;
import de.scrapebee.vectorspace.VectorSpace;

public class Similarity {

	public Similarity() {
		

	}
	
	/**
	 * Berechnet Ähnlichkeit der ARtikel
	 * @param articles ARtikel
	 * @return Dokumenten Paare
	 */
	public static List<DocumentPair> calcSimilarity(Map<String, Article> articles) {

		VectorSpace vs = new VectorSpace();

		// Vector Space erzeugen
		for (Article article : articles.values()) {
			vs.addDocument(article, NLP.tokenize(article.getContent(), article.getLanguage()));
		}

		System.out.println("Calculating pairs:");

		List<DocumentPair> pairs = new ArrayList<>();
		for (Article article : articles.values()) {
			pairs.add(vs.getNearestNeighbour(vs.getDocument(article.getId())));
			System.out.print(".");
		}

		System.out.println();
		System.out.println("Done.");

		Collections.sort(pairs);

		return pairs;
	}

}
