package de.scrapebee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Language {

	private static HashMap<String, Integer> wordIds = new HashMap<String, Integer>();
	private static HashMap<String, Integer> languageIds = new HashMap<String, Integer>();
	private static HashMap<String, Integer> urlLang = new HashMap<String, Integer>();

	
//	public static void main(String[] args) {
//		try {
//			List<String> langs = new ArrayList<String>();
//			langs.add("de");
//			langs.add("en");
//			langs.add("es");
//			langs.add("fr");
//			langs.add("ru");
//			String key = "Stuttgart";
//			for (String langId : langs) {
//				String url = "wiki:" + key;
//
//				if (!urlExists(url, langId)) {
//					String wikiText = getArticleFromWiki(langId, key);
//					List<String> wordList = getWordsFromArticle(wikiText);
//					int countNewWords = insertWordsInDatabase(langId,
//							wordList);
//					insertUrlInDatabase(langId, url, countNewWords);
//
//					for (String word : wordList) {
//						String url2 = "wiki:" + word.toLowerCase();
//						if (!urlExists(url2, langId)) {
//							String wikiText2 = getArticleFromWiki(langId,
//									word.toLowerCase());
//							List<String> wordList2 = getWordsFromArticle(wikiText2);
//							int countNewWords2 = insertWordsInDatabase(
//									langId, wordList2);
//							insertUrlInDatabase(langId, url2,
//									countNewWords2);
//							System.out.println(langId + ": " + url2 + " - "
//									+ countNewWords2
//									+ " neue Wörter im Text ("
//									+ wordList2.size() + ")");
//						}
//					}
//				}
//			}
//			
//		} catch (Exception e) {
//			System.err.println(e);
//		}
//	}

	public static void insertWordsFromContentToDb(String langId, String source,
			String path) throws IOException {

		BufferedReader reader = Files.newBufferedReader(Paths.get(path),
				StandardCharsets.UTF_8);

		String line = reader.readLine();

		int ind = 1;
		int countAllNewWords = 0;
		//19850
		while (line != null) {
			List<String> wordList = getWordsFromContent(line);
			int countNewWords = insertWordsInDatabase(langId, wordList);
			countAllNewWords += countNewWords;
			System.out.println("[" + ind + "] neue Wörter: " + countNewWords
					+ " - insg.: " + countAllNewWords);
			ind++;
			line = reader.readLine();
		}
		Language.insertUrlInDatabase(langId, source, countAllNewWords);
		System.out.print(source + " : " + countAllNewWords);

	}

	public static String detectLanguageFromText(String text)
			throws ClassNotFoundException, SQLException {

		System.out.println("0: " + DateTime.now().toString());
		List<String> wordsFromText = getWordsFromContent(text);

		List<String> wordsToCheck = new ArrayList<String>();
		int count = wordsFromText.size();
		double maxWordsToCheck1 = count * 0.15;
		int maxWordsToCheck = (int) maxWordsToCheck1; // 10% der Wörter
		if (count <= 10) {
			wordsToCheck = wordsFromText;
		} else if (maxWordsToCheck <= 10) {
			maxWordsToCheck = 10;
			Random r = new Random();
			for (int i = 0; i < maxWordsToCheck; i++) {
				int randomIndex = r.nextInt(count);
				String checkWord = wordsFromText.get(randomIndex);

				if (!wordsToCheck.contains(checkWord)) {
					wordsToCheck.add(checkWord);
				} else {
					i--;
				}
			}
		} else {
			Random r = new Random();
			for (int i = 0; i < maxWordsToCheck; i++) {
				int randomIndex = r.nextInt(count);
				String checkWord = wordsFromText.get(randomIndex);
				if (!wordsToCheck.contains(checkWord)) {
					wordsToCheck.add(checkWord);
				} else {
					i--;
				}
			}
		}
		System.out.println("1: " + DateTime.now().toString());
		Database.connect(Database.DB_LANGUAGE);
		System.out.println("2: " + DateTime.now().toString());

		String sqlWordIds = "SELECT id FROM word WHERE ";
		for (String word : wordsToCheck) {
			sqlWordIds += "word LIKE '" + word + "' OR ";
		}
		sqlWordIds = sqlWordIds.substring(0, sqlWordIds.length() - 3); // letztes
																		// OR
																		// entfernen

		ResultSet rsWordIds = Database.read(sqlWordIds);

		System.out.println("3: " + DateTime.now().toString());
		String sql = "SELECT count(c.langId) as count, c.langId, d.code FROM (SELECT a.wordId, a.langId "
				+ "FROM wordinlang a LEFT OUTER JOIN wordinlang b ON a.wordId = b.wordId AND a.occur > b.occur WHERE ";
		while (rsWordIds.next()) {
			sql += "b.wordId = " + rsWordIds.getInt("id") + " OR ";
		}
		sql = sql.substring(0, sql.length() - 3); // letztes OR entfernen
		sql += "GROUP BY wordId) c, language d WHERE d.id = c.langId GROUP BY c.langId ORDER BY count DESC LIMIT 1";

		ResultSet rs = Database.read(sql);
		System.out.println("4: " + DateTime.now().toString());

		if (rs.next()) {
			int countWordsWithSameLang = rs.getInt("count");
			//int langId = rs.getInt("langId");
			String code = rs.getString("code");
			System.out.println(countWordsWithSameLang + "/" + maxWordsToCheck
					+ " [" + count + "] for " + code);
			return code;
		}

		/*
		 * Ergebnis sollte ausgeben 7 der Wörter de, 3 der Wörter en essen | de
		 * leckere | de Hamburger | en => de 2x => LANG = de
		 */
		return "";
	}

	public static String detectLanguageFromSite(String url) {
		return null;
	}

	/**
	 * URL in Datenbank einfügen um wiederholten Abruf auszuschließen
	 * 
	 * @param language
	 *            Sprachcode
	 * @param url
	 *            URL
	 * @param newWordsCount
	 *            Anzahl neu hinzugefügter Wörter
	 */
	private static void insertUrlInDatabase(String language, String url,
			int newWordsCount) {
		try {
			// Datenbank Verbindung herstellen
			Database.connect(Database.DB_LANGUAGE);
			String sql = "INSERT INTO url (url,langId,newWords) VALUES ('"
					+ url.trim() + "',(SELECT id FROM language WHERE code = '"
					+ language + "' LIMIT 1)," + newWordsCount + ")";
			Database.insert(sql);

		} catch (ClassNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println(e);
		}
	}

	/**
	 * Ist URL bereits in Datenbank vorhanden?
	 * 
	 * @param url
	 *            URL
	 * @param language
	 *            Sprache Code (de, en, ..)
	 * @return bereits vorhanden? true/false
	 */
	private static boolean urlExists(String url, String language) {
		boolean result = false;
		try {
			if (urlLang.containsKey(url + ":" + language)) {
				result = true;
			} else {
				// Datenbank Verbindung herstellen
				Database.connect(Database.DB_LANGUAGE);
				String sql = "SELECT id FROM url WHERE url = '"
						+ url
						+ "' AND langId = (SELECT id FROM language WHERE code = '"
						+ language + "' LIMIT 1) LIMIT 1";
				ResultSet rs = Database.read(sql);
				result = rs.next();
				if (result) {
					urlLang.putIfAbsent(url + ":" + language, rs.getInt("id"));
				}
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
	 * Wörter in Datenbank einfügen
	 * 
	 * @param language
	 *            Sprach Code
	 * @param words
	 *            Liste mit Wörtern
	 * @return Anzahl neu hinzugefügter Wörter
	 */
	private static int insertWordsInDatabase(String language, List<String> words) {
		int countNewWords = 0;
		int languageId = -1;
		ResultSet resultSet;
		try {
			// Datenbank Verbindung herstellen
			Database.connect(Database.DB_LANGUAGE);

			if (languageIds.containsKey(language)) {
				languageId = languageIds.get(language);
			} else {
				// ID der Sprache Abfragen
				resultSet = Database
						.read("SELECT id FROM language WHERE code = '"
								+ language + "' LIMIT 1");
				if (resultSet.next()) {
					languageId = resultSet.getInt("id");
					languageIds.putIfAbsent(language, languageId);
				}
			}
		} catch (ClassNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println(e);
		}

		for (String word : words) {
			try {
				String sql;
				if (!wordIds.containsKey(word)) {
					// ID von Wort abfragen, sofern vorhanden
					sql = "SELECT id FROM word WHERE word = '" + word
							+ "' LIMIT 1";

					resultSet = Database.read(sql);

					if (!resultSet.next()) {
						// Wort nicht vorhanden und deswegen einfügen
						sql = "INSERT INTO word (word) VALUES ('" + word + "')";
						Database.insert(sql);
						countNewWords += 1;
					} else {
						wordIds.putIfAbsent(word, resultSet.getInt("id"));
					}
				}

				// WordInLanguage ID und Vorkommnis (Anzahl) auslesen
				resultSet = Database
						.read("SELECT id, occur FROM wordinlang WHERE wordId = (SELECT id FROM word WHERE word = '"
								+ word
								+ "' AND langId = "
								+ languageId
								+ " LIMIT 1)");
				int idFromWordInLang = -1;
				int occur = -1;
				if (resultSet.next()) {
					idFromWordInLang = resultSet.getInt("id");
					occur = resultSet.getInt("occur");
				}
				if (idFromWordInLang >= 0) {
					// Eintrag vorhanden, Vorkommnis +1 hochzählen
					sql = "UPDATE wordinlang SET occur = (" + (occur + 1)
							+ ") WHERE id = " + idFromWordInLang;
					Database.update(sql);
				} else {
					//
					sql = "INSERT INTO wordinlang (wordId, langId, occur) VALUES ((SELECT id FROM word WHERE word = '"
							+ word + "' LIMIT 1)," + languageId + ",1)";
					Database.insert(sql);
				}

			} catch (SQLException e) {
				System.err.println(e);
			}
		}
		return countNewWords;
	}

	/**
	 * Splittet Artikel in einzelne Wörter auf und liefert diese als Liste
	 * zurück
	 * 
	 * @param article
	 *            Text
	 * @return Liste mit eindeutigen Wörtern
	 */
	private static List<String> getWordsFromArticle(String article) {
		List<String> wordList = new ArrayList<String>();
		JsonElement jsonElement = new JsonParser().parse(article);
		JsonElement pages = jsonElement.getAsJsonObject().get("query")
				.getAsJsonObject().get("pages");
		JsonArray entrySet = pages.getAsJsonArray();
		JsonElement page = entrySet.get(0).getAsJsonObject();
		if (!(page.getAsJsonObject().has("missing") && page.getAsJsonObject()
				.get("missing").getAsBoolean())) {

			String content = page.getAsJsonObject().get("extract")
					.getAsString();

			Pattern r = Pattern.compile("([äöü\\-ÖÜß\\p{L}]+)");
			// (\\p{L}+)");
			// ([a-zA-Z]*ö*ß*ü*ä*Ö*Ü*Ä*-*[a-zA-Z]*[a-zA-Z]*)");

			Matcher m = r.matcher(content);

			while (m.find()) {
				if (m.group(1) != null) {
					String word = m.group(1).trim();
					if (word.length() <= 1 || wordList.contains(word))
						continue;

					wordList.add(word);
				}
			}
		}
		return wordList;
	}

	private static List<String> getWordsFromContent(String content) {
		List<String> wordList = new ArrayList<String>();

		Pattern r = Pattern.compile("([äöü\\-ÖÜß\\p{L}]+)");
		// (\\p{L}+)");
		// ([a-zA-Z]*ö*ß*ü*ä*Ö*Ü*Ä*-*[a-zA-Z]*[a-zA-Z]*)");

		Matcher m = r.matcher(content);

		while (m.find()) {
			if (m.group(1) != null) {
				String word = m.group(1).trim();
				if (word.length() <= 1 || wordList.contains(word))
					continue;

				wordList.add(word);
			}
		}

		return wordList;
	}

	/**
	 * Extrahiere Wikipedia Artikel
	 * 
	 * @param lang
	 *            Sprach Code für Wiki Seite
	 * @param key
	 *            Artikel Titel der extrahiert werden soll
	 * @return Artikel Inhalt
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static String getArticleFromWiki(String lang, String key)
			throws ClientProtocolException, IOException {
		String result = "";
		HttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(
				"https://"
						+ lang
						+ ".wikipedia.org/w/api.php?action=query&prop=extracts&explaintext&titles="
						+ key + "&format=json&formatversion=2");
		// HttpGet request = new
		// HttpGet("https://de.wikipedia.org/w/api.php?action=query&titles="+key+"&prop=revisions&rvprop=content&format=json&formatversion=2");
		request.setHeader(HttpHeaders.USER_AGENT, "Example/1.0");
		request.setHeader(HttpHeaders.EXPIRES, "0"); // Proxies.

		HttpResponse response = client.execute(request);
		response.setHeader("Cache-Control",
				"no-cache, no-store, must-revalidate"); // HTTP 1.1
		response.setHeader("Pragma", "no-cache"); // HTTP 1.0
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			InputStream inputStream = entity.getContent();
			try {

				StringWriter writer = new StringWriter();
				IOUtils.copy(inputStream, writer, "UTF-8");
				result = writer.toString();

			} finally {
				inputStream.close();
			}
		}

		return result;
	}

	/**
	 * Liefert Sprache zu Text
	 * @param text Text
	 * @return Sprache
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String getLanguageFromText(String text)
			throws ClientProtocolException, IOException {
		String result = "";
		// http://gateway-a.watsonplatform.net/calls/text/TextGetLanguage?apikey=00eb2dc5125fac52df6ba0647c176d6cdba37429&text=Welcome%20to%20GErmany

		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(
				"http://gateway-a.watsonplatform.net/calls/text/TextGetLanguage");

		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("apikey",
				""));
		params.add(new BasicNameValuePair("text", text));
		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		// Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			InputStream inputStream = entity.getContent();
			try {

				StringWriter writer = new StringWriter();
				IOUtils.copy(inputStream, writer, "UTF-8");
				result = writer.toString();

			} finally {
				inputStream.close();
			}
		}

		return result;
	}
}
