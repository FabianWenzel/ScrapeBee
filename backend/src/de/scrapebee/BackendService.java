package de.scrapebee;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Socket;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.scrapebee.vectorspace.DocumentPair;

//Read more: http://mrbool.com/communicating-node-js-and-java-via-sockets/33819#ixzz47Vzwwkba

/**
 * BackendService beinhaltet die Main Methode und führt den Service aus
 */
public class BackendService {

	public final static Object LOCK = new Object();
	public static Map<String, Article> articles;

	public static List<WorkerThread> workerThreads;
	public static List<String> urls;
	private static ExecutorService executor;
	
	
	public static void main(String[] args) throws ClassNotFoundException, URISyntaxException, InterruptedException {
		
		// Lädt Classifier der verfügbaren Sprachen
		NLP.init();
		
		while(true) {
		
			articles = new HashMap<String, Article>();
			workerThreads = new ArrayList<WorkerThread>();
			urls = new ArrayList<String>();
			executor = Executors.newFixedThreadPool(5);  //creating a pool of 5 threads  
	 		
			Socket socket = new Socket("ws://"+SystemNames.ENGINE_HOST+":"+SystemNames.ENGINE_PORT);
			socket.on(Socket.EVENT_OPEN, new Emitter.Listener() {
			  
				@Override
				public void call(Object... args) {
					
					socket.send("Java Backend want a connection :)");
					System.out.println("Hello there");
				}
			});
			socket.on("message", new Emitter.Listener() {
				  
					@Override
					public void call(Object... args) {
						
						if (args != null && args.length > 0) {
	
							System.out.println(args[0]);
							JsonParser parser = new JsonParser();
							JsonObject jo = parser.parse(args[0].toString()).getAsJsonObject();
							
							// Status von JSON Objekt prüfen
							if (jo.get(SystemNames.STATUS).getAsInt() == SystemNames.STATUS_OK) {
								switch (jo.get(SystemNames.TYPE).getAsString()) {
									// Links werden gesendet
									case SystemNames.LINKS:
										// http://stackoverflow.com/questions/18421674/using-gson-to-parse-a-json-array
										Gson gson = new Gson();
										SimpleArticle[] simpleArticleList = gson.fromJson(jo.get(SystemNames.LINKS), SimpleArticle[].class);
										
										WorkerThread worker = new WorkerThread(simpleArticleList);  
							            executor.execute(worker);//calling execute method of ExecutorService  
							            workerThreads.add(worker);
		
										break;
									case SystemNames.MESSAGE:
										System.out.println(jo.get(SystemNames.MESSAGE).getAsString());
										break;
								}
							} else if (jo.get(SystemNames.STATUS).getAsInt() == SystemNames.STATUS_ERROR) {
								System.err.println(jo.get(SystemNames.MESSAGE).getAsString());
							}
						}
					}
				});
			socket.open();

			// Links werden aufbereitet in den 5 Threads
	        while (!executor.isTerminated()) { 
	        	if (workerThreads.size() != 0) {
	        		int sumArticlesToDo = 0;
	        		int sumCompletedArticles = 0;
	    			Thread.sleep(500);
	    			Gson gson = new Gson();
	    			// Summiere Anzahl Artikel für Status Meldungen
	    			for (WorkerThread w : workerThreads) {
	    				sumArticlesToDo += w.getSize();
	    				sumCompletedArticles += w.getCompleted();
	    			}
	    			if (sumArticlesToDo != sumCompletedArticles) {
	    				// Schicke Statusmeldung solange Artikel aufbereitet werden
	    				JsonObject jsonObj = new JsonObject();
	    				jsonObj.addProperty(SystemNames.STATUS, SystemNames.STATUS_OK);
	    				jsonObj.addProperty(SystemNames.TYPE, SystemNames.MESSAGE);
	    				jsonObj.addProperty(SystemNames.MESSAGE, "Aktuell wurden " + sumCompletedArticles + " von " + sumArticlesToDo + " Artikeln bearbeitet.");
	    				
	    				socket.send(jsonObj.toString());
	    			} else if (workerThreads.size() > 1 && sumCompletedArticles >= articles.size()) {
	    				// Nachdem alle Artikel eingelesen wurden weitere Statusmeldungen schicken
	    				JsonObject jsonObj = new JsonObject();
	    				jsonObj.addProperty(SystemNames.STATUS, SystemNames.STATUS_OK);
	    				jsonObj.addProperty(SystemNames.TYPE, SystemNames.MESSAGE);
	    				jsonObj.addProperty(SystemNames.MESSAGE, "Aktuell wurden " + sumCompletedArticles + " von " + sumArticlesToDo + " Artikeln bearbeitet.");
	    				
	    				socket.send(jsonObj.toString());
	    				WorkerThread worker = new WorkerThread(articles.values().toArray(new Article[articles.values().size()]));  
			            executor.execute(worker);
			            workerThreads.add(worker);
	    				
			            jsonObj = new JsonObject();
	    				jsonObj.addProperty(SystemNames.STATUS, SystemNames.STATUS_OK);
	    				jsonObj.addProperty(SystemNames.TYPE, SystemNames.MESSAGE);
	    				jsonObj.addProperty(SystemNames.MESSAGE, "Ähnlichkeit der Artikel wird kalkuliert.");
	    				socket.send(jsonObj.toString());
	    				List<DocumentPair> pairs = Similarity.calcSimilarity(articles);
	    				
	    				
	    				jsonObj = new JsonObject();
	    				jsonObj.addProperty(SystemNames.STATUS, SystemNames.STATUS_OK);
	    				jsonObj.addProperty(SystemNames.TYPE, SystemNames.MESSAGE);
	    				jsonObj.addProperty(SystemNames.MESSAGE, "Artikel werden zu Themen vereint.");
	    				socket.send(jsonObj.toString());
	    				List<ArticleGroup> articleGroups = new ArrayList<ArticleGroup>();
	    				int countPair = 0;
	    				int maxPairs = pairs.size();
	    				// Ähnlichkeiten kalkulieren:
	    				for (DocumentPair p : pairs) {
	    					countPair += 1;
	    					jsonObj = new JsonObject();
	        				jsonObj.addProperty(SystemNames.STATUS, SystemNames.STATUS_OK);
	        				jsonObj.addProperty(SystemNames.TYPE, SystemNames.MESSAGE);
	        				jsonObj.addProperty(SystemNames.MESSAGE, "Artikel werden zu Themen vereint. ("+countPair+"/"+maxPairs+")");
	        				socket.send(jsonObj.toString());
	    					
	    					System.out.println(p.dv1.getId() + " ---> " + p.dv2.getId() + " ("
	    							+ p.similarity + ")");
	    					System.out.println(articles.get(p.dv1.getId()).getLink() + " ---> " + articles.get(p.dv2.getId()).getLink()  + " ("
	    							+ p.similarity + ")");
	    					
	    					// Bei einem Ähnlichkeitswert von über 0.16 gehören die Artikel wahrscheinlich zum selben Thema
	    					if (p.similarity > 0.16){
	    						boolean added = false;
	    						
	    						articles.get(p.dv1.getId()).setSimilarity(p.similarity);
	    						articles.get(p.dv2.getId()).setSimilarity(p.similarity);
	    						
	    						for (ArticleGroup aGroup : articleGroups) {
	    							if (aGroup.getList().containsKey(p.dv1.getId()))
	    							{ 
	    								aGroup.addArticle(articles.get(p.dv2.getId()));
	    								added = true;
	    							} else if (aGroup.getList().containsKey(p.dv2.getId())) {
	    								aGroup.addArticle(articles.get(p.dv1.getId()));
	    								added = true;
	    							}
	    						}
	    						
	    						if (!added) {
	    							// Aritkel zu einer Artikelgruppe zusammenfügen
	    							ArticleGroup ag = new ArticleGroup(articles.get(p.dv1.getId()).getCategory());
	        						ag.addArticle(articles.get(p.dv1.getId()));
	        						ag.addArticle(articles.get(p.dv2.getId()));
	        						articleGroups.add(ag);
	    						}
	    						
	    					}
	    				}
	    				
	
						jsonObj = new JsonObject();
	    				jsonObj.addProperty(SystemNames.STATUS, SystemNames.STATUS_OK);
	    				jsonObj.addProperty(SystemNames.TYPE, SystemNames.MESSAGE);
	    				jsonObj.addProperty(SystemNames.MESSAGE, "Artikel Gruppen werden ihren Kategorien zugeordnet.");
	    				socket.send(jsonObj.toString());
	    				Map<String, Category> categoryList = new HashMap<String, Category>();
	    				int countAG = 0;
	    				int maxAG = articleGroups.size();
	    				for (ArticleGroup aGroup : articleGroups) {
	    					countAG += 1;
	
	    					// Keywords aus Artikel der Artikelgruppe generieren
	    					Keywords.extractKeywords(aGroup);
	    					
	    					jsonObj = new JsonObject();
	        				jsonObj.addProperty(SystemNames.STATUS, SystemNames.STATUS_OK);
	        				jsonObj.addProperty(SystemNames.TYPE, SystemNames.MESSAGE);
	        				jsonObj.addProperty(SystemNames.MESSAGE, "Artikel Gruppen werden ihren Kategorien zugeordnet. ("+countAG+"/"+maxAG+")");
	        				socket.send(jsonObj.toString());
	    					if (categoryList.containsKey(aGroup.getCategory())) {
	    						categoryList.get(aGroup.getCategory()).addArticleGroup(aGroup);
	    					} else {
	    						Category cat = new Category(aGroup.getCategory());
	    						cat.addArticleGroup(aGroup);
	    						categoryList.put(cat.getName(), cat);
	    					}
	    				}
	    				
	    				
	
	    				String json = gson.toJson(categoryList);
	    				
	    				JsonObject jsonObj2 = new JsonObject();
	    				jsonObj2.addProperty(SystemNames.STATUS, SystemNames.STATUS_OK);
	    				jsonObj2.addProperty(SystemNames.TYPE, SystemNames.ARTICLES);
	    				jsonObj2.addProperty(SystemNames.ARTICLES, json);
	    				
	    				System.out.println(jsonObj2.toString());
	    				// JSON Ergebnis zurück an Client senden
	    				socket.send(jsonObj2.toString());
	    				break;
	    				
	    			}
	        	}
	        }
		}
	}

	//public static void main(String[] args) {
		
		
		
		
//			Language.insertWordsFromContentToDb("de", "deu_news_2015_3M", "C:\\Users\\lpr_000\\Downloads\\deu_news_2015_3M.tar\\deu_news_2015_3M\\deu_news_2015_3M-sentences.txt");
	        
//			List<String> links = new ArrayList<String>();
//			links.add("http://www.spiegel.de/politik/ausland/eu-kommission-droht-polen-mit-verfahren-wegen-justizreform-a-1093639.html");
//			links.add("http://www.zdnet.fr/actualites/le-paas-une-reponse-aux-defis-de-l-entreprise-digitale-39836962.htm");
//			links.add("http://www.elespanol.com/social/20160602/129487129_0.html");
//			links.add("http://www.bbc.com/capital/story/20160531-how-to-induce-sleep-without-drugs");
//			links.add("http://www.bbc.com/news/world-europe-36433114");
//			List<Article> list = new ArrayList<Article>();
//			
//			for (String url : links) {
//				Article a = Article.getArticleFromUrl(url);
//				System.out.println("A: " + DateTime.now().toString());
//				System.out.println(a.getLanguage() + ": " + a.getLink());
//				System.out.println("E: " + DateTime.now().toString());
//				list.add(a);
//			}
//			
//			Similarity.calcSimilarity(list.toArray(new Article[list.size()]));
			
			//System.out.println(Language.getLanguageFromText(content));
			

	//		Keywords.extractKeywordsByTitle("Uno-Nothilfegipfel in Türkei: Erdogan schimpft auf Europa", content);
			
			
		//	Location.analyze(content);
	
		
//		// class instance
//		BackendService client = new BackendService();
//
//
	//}
	
}
