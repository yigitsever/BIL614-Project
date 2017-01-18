import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class DBPedia {
	
	public DBPedia()
	{
		
	}
	public static void main(String[] args) {
		try {
			URL dbpedia;
			HttpURLConnection dbpedia_connection;
			dbpedia = new URL("http://localhost:8080/rest/annotate");
			dbpedia_connection = (HttpURLConnection) dbpedia.openConnection();
			dbpedia_connection.setDoOutput(true);
			dbpedia_connection.setRequestMethod("GET");
			dbpedia_connection.setRequestProperty("Accept", "application/json");
			String urlParameters = "confidance=0.5&support=80&text=";
			String metin = "recep tayyip erdoğan İstanbul'a gitti.";
			urlParameters = urlParameters.concat(URLEncoder.encode(metin));

			dbpedia_connection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(dbpedia_connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			dbpedia_connection.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(dbpedia_connection.getInputStream()));
			String inputLine;
			String output = "";
			while ((inputLine = in.readLine()) != null) {
				output += inputLine;
				System.out.println(inputLine);
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
