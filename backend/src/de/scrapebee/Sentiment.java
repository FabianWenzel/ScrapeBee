package de.scrapebee;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabian on 16.06.2016.
 */
public class Sentiment {

	/**
	 * Liefert Sentiment Analyse zu Inhalt
	 * @param text Inhalt
	 * @return Mood
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
    public static String getSentimentFromText(String text)
            throws ClientProtocolException, IOException {
        String result = "";
        // http://gateway-a.watsonplatform.net/calls/text/TextGetTextSentiment?apikey=ffb0772f6b84bcb7cd31e627adc9810815b01c20&text=Welcome%20to%20GErmany

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(
                "http://gateway-a.watsonplatform.net/calls/text/TextGetTextSentiment");

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("apikey",
                "ffb0772f6b84bcb7cd31e627adc9810815b01c20"));
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
