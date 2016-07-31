package de.scrapebee;

import java.util.ArrayList;
import java.util.List;

/**
 * Kategorie Klasse fasst alle Artikelgruppen der selben Kategorie zusammen
 */
public class Category {
	
	private String name;
	private List<ArticleGroup> list;
	
	public Category(String name) {
		this.list = new ArrayList<ArticleGroup>();
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	public List<ArticleGroup> getList() {
		return this.list;
	}
	
	public void addArticleGroup(ArticleGroup a) {
		this.list.add(a);
	}
}
