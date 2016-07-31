package de.scrapebee;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import sun.net.www.content.text.Generic;

/**
 * Klasse für Keyword Extraction
 */
public class Keywords {

	/**
	 * Liefert Keywords zu ArtikelGruppe
	 * @param ag Artikel Gruppe
	 */
	public static void extractKeywords(ArticleGroup ag) {
		// to extract keyphrases from new documents,
		
		TreeMap<String, Integer> keywordList = new TreeMap<String, Integer>();
		
		for (Article a : ag.getList().values()) {
			LinkedHashMap<String, Integer> sortedList = extractKeywordsByTitle(a.getTitle(), a.getContent());
			 
			for (String word : sortedList.keySet()) {
				if (word.length() < 2) {
					continue;
				}
				if (keywordList.containsKey(word)) {
					keywordList.put(word, keywordList.get(word) + sortedList.get(word));
				} else {
					keywordList.put(word, sortedList.get(word));
				}
			}
		}
		List<Entry<String,Integer>> resultList = entriesSortedByValues(keywordList);
		printMap(keywordList);
		
		int countForReturn = 0;
		
		for (Entry<String,Integer> entry : resultList) {
			ag.addKeyword(entry.getKey());
			countForReturn += 1;
			if (countForReturn >= 3) {
				break;
			}
		}
	}
	
	// http://stackoverflow.com/questions/11647889/sorting-the-mapkey-value-in-descending-order-based-on-the-value
	private static <K,V extends Comparable<? super V>> 
	    List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {
	
		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
		
		Collections.sort(sortedEntries, 
		    new Comparator<Entry<K,V>>() {
		        @Override
		        public int compare(Entry<K,V> e1, Entry<K,V> e2) {
		            return e2.getValue().compareTo(e1.getValue());
		        }
		    }
		);
		
		return sortedEntries;
	}
	
	public static void printMap(TreeMap<String, Integer> map) {
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			System.out.println("Key : " + entry.getKey() 
                                      + " Value : " + entry.getValue());
		}
	}
	
	/**
	 * Liefert Liste mit Keywords
	 * @param title Titel
	 * @param text Inhalt
	 * @return Liste mit Keywords
	 */
	public static LinkedHashMap<String, Integer> extractKeywordsByTitle(String title, String text) {
		text = text.toLowerCase();
		// Stoppwörter entfernen
		title = title.replace(",", "").replace(".", "").replace(":", "").replace(";", "").replace("!", "").replace("?", "").replace(" - ", "").replace("\"", "").replace("/", "");
		title = title.replace("Der", "").replace("Die", "").replace("Das", "").replace("Und", "").replace("Den", "").replace("Dem", "").replace("Der", "");
		title = title.replace("Hat", "").replace("Dann", "").replace("Sind", "").replace("Auch", "").replace("Erst", "").replace("Über", "").replace("Für", "");
		title = title.replace("In", "").replace("Im", "").replace("Es", "").replace("Er", "").replace("Sie", "").replace("Es", "");
		String[] titleWords = title.split(" ");
		LinkedHashMap<String, Integer> sortedList = new LinkedHashMap<String, Integer>();
		
		for (String titleWord : titleWords) {
			// Nur Wörter die mit Großbuchstaben beginnen verwenden
			if (titleWord != "" && titleWord.length() > 0 && Character.isUpperCase(titleWord.charAt(0))) {
				titleWord = titleWord.toLowerCase().trim();
				sortedList.putIfAbsent(titleWord, countWordInText(titleWord, text, 0, 0));
			}
		}
		
		for (String key : sortedList.keySet()) {
			System.out.println(key + " - " + sortedList.get(key) + " mal im Text.");
		}
		return sortedList;
	}
	
	/**
	 * Zählt Wortvorkommnisse
	 * @param word Wort
	 * @param text INhalt
	 * @param fromIndex Start Index
	 * @param counter Zähler
	 * @return Anzahl
	 */
	public static int countWordInText(String word, String text, int fromIndex, int counter) {
		int index = text.indexOf(word, fromIndex);
		if (index > fromIndex) {
			counter += 1;
		} else {
			return counter;
		}
		return countWordInText(word, text, index+1, counter);
	}
}
