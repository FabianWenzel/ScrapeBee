package de.scrapebee;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import de.scrapebee.Sentiment;

import java.lang.*;

/**
 * Thread Klasse für Multithreads
 * http://www.javatpoint.com/java-thread-pool
 */
public class WorkerThread implements Runnable {
	private SimpleArticle[] list;
	private Article[] articles;
	private int completed;
	private CharSequence positiveSenti ="positive";
	private CharSequence negativeSenti ="negative";
	private CharSequence neutralSenti ="neutral";
	
	/**
	 * Thread Konstruktor
	 * @param list Liste mit Artikeln
	 */
	public WorkerThread(SimpleArticle[] list){  
        this.list = list;  
        this.completed = 0;
    } 
	
	/**
	 * Thread Konstruktor
	 * @param list Liste mit Artikeln
	 */
	public WorkerThread(Article[] list){  
        this.articles = list;  
    } 
	
	/**
	 * Anzahl Artikel in Thread
	 * @return Anzahl
	 */
	public int getSize() {
		if (this.list == null) {
			return 0;
		}
		return this.list.length;
	}
	
	/**
	 * Anzahl fertiggestellter Artikel in Thread
	 * @return Anzahl
	 */
	public int getCompleted() {
		return this.completed;
	}
	
	@Override
	public void run() {
		if (articles == null) {
			System.out.println(Thread.currentThread().getName()+" (Start) Anzahl Links = "+ this.list.length);  
			
			for (SimpleArticle simpleArticle : this.list) {
				try {
					System.out.println(simpleArticle.link);
					Article article = Article.isInDatabase(simpleArticle);
					if (article == null) {
						article = Article.getArticleFromUrl(simpleArticle.link);
						article.setTitle(simpleArticle.title);
						String senti = Sentiment.getSentimentFromText(article.getContent());
						
						if (senti.contains(positiveSenti)){
							senti = "positive";
						} else if (senti.contains(negativeSenti)){
							senti = "negative"; 
						} else {
							senti = "neutral";
						}
						article.setMood(senti);
						//Einmal Icon und Category aufrufen, damit beides gefüllt wird
						String fillcat = article.getCategory();
					}
					String fillicon = article.getIcon();
					synchronized (BackendService.LOCK) {
						if (!BackendService.urls.contains(simpleArticle.link)) {
							BackendService.articles.putIfAbsent(article.getId(), article);
							BackendService.urls.add(simpleArticle.link);
						}
						this.completed += 1;
						
					}
					
				} catch (MalformedURLException e) {
					System.err.println(e);
				}
				catch (IOException e){
					System.err.println(e);
				}

			}		
			
	        System.out.println(Thread.currentThread().getName()+" (End)");//prints thread name  
		} else {
			for (Article article : this.articles) {
				article.addToDatabase();
			}
		}
	}
}
