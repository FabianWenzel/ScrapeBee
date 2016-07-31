package de.scrapebee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Artikel Gruppe: Fasst mehrere Artikel zusammen.
 *
 */
public class ArticleGroup {
	
	private Map<String, Article> list;
	private String category;
	private List<String> keywords;
	
	public ArticleGroup(String cat) {
		this.list = new HashMap<String, Article>();
		this.keywords = new ArrayList<String>();
		this.category = cat;
	}
	
	public Map<String, Article> getList() {
		return this.list;
	}
	
	public void addArticle(Article a) {
		this.list.putIfAbsent(a.getId(), a);
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public List<String> getKeywords() {
		return this.keywords;
	}
	
	public void addKeyword(String key) {
		this.keywords.add(key);
	}

}
