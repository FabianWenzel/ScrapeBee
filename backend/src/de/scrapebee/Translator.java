package de.scrapebee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Übersetzer Klasse
 */
public class Translator {

	private static String APIKEY = "trnsl.1.1.20160608T224321Z.f4b099d0471e4206.0ae58fa8def28fae0d26dcc3bb56ce772981bc5c";
	private static String LANG = "de";

	/**
	 * Übersetzt Text
	 * @param text Text
	 * @return Übersetzung
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String translate(String text) throws ClientProtocolException,
			IOException {
		String result = "";
		String jsonText =  makeHttpRequest(
				"https://translate.yandex.net/api/v1.5/tr.json/translate?lang="
						+ LANG + "&key=" + APIKEY, text);
		
		if (jsonText != null && jsonText != "") {
			JsonParser parser = new JsonParser();
			JsonObject jo = parser.parse(jsonText).getAsJsonObject();
			
			if (jo.get(SystemNames.CODE).getAsInt() == SystemNames.STATUS_OK) {
				result = jo.get(SystemNames.TEXT).getAsString();
			}
		}
		
		return result;
	}

	/**
	 * Liefert SPrache zu Text
	 * @param text Text
	 * @return Sprache
	 * @throws UnsupportedEncodingException
	 */
	public static String getLanguage(String text)
			throws UnsupportedEncodingException {
		
		String result = "";
		String jsonText =  makeHttpRequest(
				"https://translate.yandex.net/api/v1.5/tr.json/detect?key="
						+ APIKEY + "&hint=de,en,es,fr", text);
		
		if (jsonText != null && jsonText != "") {
			JsonParser parser = new JsonParser();
			JsonObject jo = parser.parse(jsonText).getAsJsonObject();
			
			if (jo.get(SystemNames.CODE).getAsInt() == SystemNames.STATUS_OK) {
				result = jo.get(SystemNames.LANGUGAGE).getAsString();
			}
		}
		
		return result;
	}

	// https://tech.yandex.com/translate/doc/dg/reference/translate-docpage/
	private static String makeHttpRequest(String url, String text)
			throws UnsupportedEncodingException {
		InputStream is = null;
		String json = "";
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
		params.add(new BasicNameValuePair(SystemNames.TEXT, text));
		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		try {
			HttpResponse httpresponse = httpclient.execute(httppost);
			HttpEntity httpentity = httpresponse.getEntity();
			is = httpentity.getContent();

		} catch (ClientProtocolException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "utf-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");

				}
				is.close();
				json = sb.toString();

			} catch (IOException e) {

				e.printStackTrace();
			}

		} catch (Exception e) {
			System.err.println(e);
		}

		return json;

	}
}
