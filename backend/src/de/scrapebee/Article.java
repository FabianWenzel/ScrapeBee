package de.scrapebee;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import de.l3s.boilerpipe.extractors.ArticleSentencesExtractor;

/**
 * Artikel Klasse für einen Artikel einer Newsseite
 *
 */
public class Article {
	private String id;
	private String title;
	private String content;
	private String contentTranslated;
	private String link;
	private List<String> locations;
	private List<String> keywords;
	private String language;
	private String category;
	private double similarity;
	private String mood;
	private String icon;
	
	/**
	 * Standard Konstruktor für einen Artikel
	 * @param content Inhalt des Artikels
	 */
	public Article(String content) {
		this.content = content;
		try {
			// Als Id wird ein MD5 Hash Code verwendet
			this.id = Helper.getMD5(this.content);
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e);
		}
	}
	
	/**
	 * Standard Konstruktor für einen Artikel
	 * @param title Titel des Artikels
	 * @param content Inhalt des Artikels
	 */
	public Article(String title, String content) {
		this(content);
		this.title = title;
		try {
			this.id = Helper.getMD5(this.content);
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e);
		}
	}
	
	/**
	 * Liefert Id
	 * @return Id
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Liefert Ähnlichkeitswert
	 * @return Ähnlichkeitswert
	 */
	public double getSimilarity() {
		return similarity;
	}
	
	/**
	 * Setzt Ähnlichkeitswert
	 * @param sim Ähnlichkeitswert
	 */
	public void setSimilarity(double sim) {
		this.similarity = sim;
	}
	
	/**
	 * Liefert Titel des Artikels
	 * @return Titel
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Setzt Titel des Artikels
	 * @param title Titel
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Liefert Icon für Artikel News Seite
	 * @return Icon Src
	 */
	public String getIcon() {
		if (this.icon == null) {
			String result = "";
			HashMap<String, String> icons = new HashMap<String,String>();
			icons.put("spiegel.de", "images/spiegel.ico");
			icons.put("focus.de", "images/focus.ico");
			icons.put("sueddeutsche.de", "images/sz.ico");
			icons.put("zeit.de", "images/zeit.ico");

			
			String checkLink = this.link.toLowerCase();
			
			for(String key : icons.keySet()) {
				if (checkLink.contains(key)) {
					result = icons.get(key);
					break;
				}
			}
			this.icon = result;
		}
		if (this.icon.isEmpty()) {
			this.icon = "images/empty.png";
		}
		return this.icon;
	}
	
	/**
	 * Funktion die Kategorie anhand URL ausfindig macht und zuordnet
	 * @return Kategorie
	 */
	public String getCategory() {
		if (this.category == null) {
			HashMap<String, String> categories = new HashMap<String,String>();
			categories.put("wirtschaft", "Wirtschaft");
			categories.put("finanzen", "Wirtschaft");
			categories.put("capital", "Wirtschaft");
			categories.put("business", "Wirtschaft");
			categories.put("politik", "Politik");
			categories.put("sport", "Sport");
			categories.put("gesellschaft", "Gesellschaft");
			categories.put("schulspiegel", "Sonstiges");
			categories.put("kultur", "Kultur");
			categories.put("culture", "Kultur");
			categories.put("panorama", "Panorama");
			categories.put("earth", "Panorama");
			categories.put("auto", "Mobilität");
			categories.put("mobilitaet", "Mobilität");
			categories.put("mobilität", "Mobilität");
			categories.put("technik-motor", "Mobilität");
			categories.put("digital", "Digital");
			categories.put("netzwelt", "Digital");
			categories.put("wissenschaft", "Wissenschaft");
			categories.put("gesundheit", "Gesundheit");
			categories.put("health", "Gesundheit");
			categories.put("karriere", "Karriere");
			categories.put("beruf-chance", "Karriere");
			categories.put("reise", "Reise");
			categories.put("travel", "Reise");
			categories.put("reisen", "Reise");
			categories.put("wissen", "Wissen");
			categories.put("uni", "Uni");
			categories.put("technik", "Technik");
			categories.put("technology", "Technik");
			categories.put("sonstiges", "Sonstiges");
	
			
			String checkLink = this.link.toLowerCase();
			
			// Vergleiche Inhalt des Links mit Kategorie
			for(String key : categories.keySet()) {
				if (checkLink.contains(key)) {
					this.category = categories.get(key);
					break;
				}
			}
			
			if (this.category == null || this.category.isEmpty()) {
				this.category = categories.get("sonstiges");
			}
		}
		return this.category;
	}
	
	/**
	 * Setze Kategorie des Artikels
	 * @param cat Kategorie
	 */
	public void setCategory(String cat) {
		this.category = cat;
	}
	
	/**
	 * Liefert Inhalt des Artikels
	 * @return Inhalt
	 */
	public String getContent() {		
		return content;
	}
	
	/**
	 * Liefert übersetzten Inhalt eines Artikels
	 * @return übersetzter Inhalt
	 */
	public String getContentTranslated() {
		if (this.contentTranslated == null) {
			try {
				this.contentTranslated = Translator.translate(this.content);
			} catch (ClientProtocolException e) {
				System.err.println(e);
			} catch (IOException e) {
				System.err.println(e);
			}
		}
		return this.contentTranslated;
	}
	
	/**
	 * Inhalt eines Artikels setzen
	 * @param content Inhalt
	 */
	public void setContent(String content) {
		if (this.content.isEmpty()) {
			this.content = content;
		} else {
			System.err.println("Error: Inhalt darf nicht geändert werden.");
		}
	}
	
	/**
	 * Liefert Link eines Artikels
	 * @return Url
	 */
	public String getLink() {
		return link;
	}
	
	/**
	 * Setzt Link eines Artikels
	 * @param link Url
	 */
	public void setLink(String link) {
		this.link = link;
	}
	
	/**
	 * Liefert Standorte eines Artikels
	 * @return Liste mit Standorten
	 */
	public List<String> getLocations() {
		if (locations == null) {
			try {
				locations = NLP.getLocation(this.content, this.getLanguage());
			} catch (Exception e) {
				System.err.println(e);
			}
		}
		return locations;
	}

	/**
	 * Liefert Keywords eines Artikels
	 * @return Keywords
	 */
	public List<String> getKeywords() {
		return keywords;
	}
	
	/**
	 * Liefert Keywords als String für DB
	 * @return Keywords Komma Separtiert
	 */
	public String getKeywordsAsStr() {
		String result = "";
		if (this.keywords == null) {
			this.keywords = new ArrayList<String>();
		}
		for (String s : this.getKeywords()) {
			result += s+";";
		}
		if (result.length() > 1) {
			result = result.substring(0,result.length() - 1);
		}
		return result;
	}
	
	/**
	 * Setzt Keywords eines Artikels
	 * @param keywords Keywords
	 */
	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
	
	/**
	 * Liefer Sprache zu Inhalt
	 * @return Sprach Code
	 */
	public String getLanguage() {
		if (language == null) {
			try {
				//language = Language.detectLanguageFromText(this.content);
				language = Translator.getLanguage(this.content);
			} catch (Exception e) {
				System.err.println(e);
			}
		}
		return language;
	}
	
	/**
	 * Liefert Artikel zu Internetadresse
	 * @param urlStr URL von Artikel
	 * @return Artikel Objekt mit Inhalt
	 * @throws MalformedURLException
	 */
	public static Article getArticleFromUrl(String urlStr) throws MalformedURLException {
		Article result = null;
		final URL url = new URL(urlStr);
		try {
		    String content = ArticleSentencesExtractor.INSTANCE.getText(url);
			result = new Article(content);
			result.setLink(urlStr);
		
		} catch (Exception e) {
			System.err.println(e);
		}
		return result;		
	}
	
	/**
	 * Liefert Artikel Schreibweise (Positiv/Negativ/Neutral)
	 * @return Mood
	 */
	public String getMood() {
		if (this.mood == null) return "";
		return this.mood;
	}
	
	/**
	 * Setzt Artikel Schreibweise (Positiv/Negativ/Neutral)
	 * @param mood Mood :)
	 */
	public void setMood(String mood) {
		this.mood = mood;
	}
	
	/**
	 * Liefert Kategorie aus DB
	 * @param categoryId Kategorie Id
	 * @return Kategorie Name
	 */
	public String getCategory(int categoryId) {
		String result = "";
		try {
			// Datenbank Verbindung herstellen
			Database.connect(Database.DB_ARTICLES);
			String sql = "SELECT name FROM category WHERE id = "+categoryId+" LIMIT 1";
			ResultSet rs = Database.read(sql);

			if (rs.next()) {
				result = rs.getString("name");
			}
		} catch (ClassNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;
	}
	
	/**
	 * Liefert Kategorie ID zu Kategorie des ARtikels
	 * @return ID
	 */
	public int getCategoryId() {
		int result = -1;
		try {
			// Datenbank Verbindung herstellen
			Database.connect(Database.DB_ARTICLES);
			String sql = "SELECT id FROM category WHERE name LIKE '"+this.getCategory()+"' LIMIT 1";
			ResultSet rs = Database.read(sql);

			if (rs.next()) {
				result = rs.getInt("id");
			} else {
				sql = "INSERT INTO category (name) VALUES ('"+this.getCategory()+"')";
				Database.insert(sql);
				result = this.getCategoryId();
			}
		} catch (ClassNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;
	}
	
	/**
	 * Fügt Artikel der DB hinzu
	 */
	public void addToDatabase() {
		
		try {
			if (!this.isInDatabase()) {
				// Datenbank Verbindung herstellen
				Database.connect(Database.DB_ARTICLES);
				String sql = "INSERT INTO article (title,link,keywords,mood,language,content,category) VALUES ('"+this.getTitle()+"','"+this.getLink()+"','"+this.getKeywordsAsStr()+"','"+this.getMood()+"','"+this.getLanguage()+"','"+this.getContent()+"',"+this.getCategoryId()+")";
				Database.insert(sql);
			}
		} catch (ClassNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println(e);
		}

	}
	
	/**
	 * Prüft ob Artikel bereits in DB ist
	 * @return true/false
	 */
	public boolean isInDatabase() {
		boolean result = false;
		try {
			// Datenbank Verbindung herstellen
			Database.connect(Database.DB_ARTICLES);
			String sql = "SELECT uniqueId FROM article WHERE link LIKE '"+this.getLink()+"' LIMIT 1";
		
			ResultSet rs = Database.read(sql);
			result = rs.next();
			
		} catch (ClassNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;
	}

	
	/**
	 * Prüft ob Artikel bereits in DB ist
	 * @param article SimpleArtikel
	 * @return Artikel aus DB
	 */
	public static Article isInDatabase(SimpleArticle article) {
		Article result = null;
		try {
			// Datenbank Verbindung herstellen
			Database.connect(Database.DB_ARTICLES);
			String sql = "SELECT * FROM article WHERE link LIKE '"+article.link+"' LIMIT 1";
		
			ResultSet rs = Database.read(sql);

			if (rs.next()) {
				result = new Article(rs.getString("content"));
				result.setLink(rs.getString("link"));
				result.setCategory(result.getCategory(rs.getInt("category")));
				//result.setContent(rs.getString("content"));
				result.setTitle(rs.getString("title"));
			}
			
		} catch (ClassNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;
	}
}
