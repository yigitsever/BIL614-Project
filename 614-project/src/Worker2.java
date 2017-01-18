import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class Worker2 implements Callable<String> {
	private String trainFolder = "";

	private URL dbpedia;
	private HttpURLConnection dbpedia_connection;
	

	public Worker2(String fileName) {
		this.trainFolder = fileName;
		
		
	}

	@Override
	public String call() throws Exception {

		ArrayList<ArrayList<Integer>> list = findSemanticVariables();

		ArrayList<String> returnObj = new ArrayList<>();

		for (ArrayList<Integer> l : list) {
			if (trainFolder.contains("unpopular")) {
				returnObj.add("No Person=" + l.get(0) + " Organization=" + l.get(1) + " Location=" + l.get(2));
			} else {
				returnObj.add("Yes Person=" + l.get(0) + " Organization=" + l.get(1) + " Location=" + l.get(2));
			}

		}

		String r = returnObj.get(0);

		for (int i = 1; i < returnObj.size(); i++) {
			r += "\n" + returnObj.get(i);
		}

		return r;
	}

	public ArrayList<ArrayList<Integer>> findSemanticVariables() {
		ArrayList<ArrayList<Integer>> allResults = new ArrayList<>();

		File[] files = new File(trainFolder).listFiles();
		ArrayList<String> folders = new ArrayList<String>();

		int i = 0;
		for (File file : files) {
			if (file.isHidden()) {
				continue;
			}
			if (!file.isDirectory()) {
				folders.add(file.getAbsolutePath());
				i++;

			}

		}

		for (String folder : folders) {

			ArrayList<Integer> results = new ArrayList<>();

			System.err.println(folder);
			
			if(folder.contains("output"))
				continue;

			InputDelegate inputDelegate = new InputDelegate(folder);
			inputDelegate.openFile();

			// System.out.println("line okuyorum");
			String line = inputDelegate.readFile();
			// System.out.println("line okudum");
			int personCount = 0;
			int locCount = 0;
			int orgCount = 0;

			while (line != null) {
				// System.out.println("split edecğim");
				String[] words = line.split("\\.");
				// System.out.println("split ettim");

				System.out.println(words.length);
				int length = words.length;
				int wc = 0;

				for (String word : words) {
					wc++;
					System.out.println(wc+"/"+length);
					//System.out.println(word);
					// System.out.println("stem edeceğim");

					word = word.trim();
					if (word.contains("@"))
						continue;

					if (word.length() < 2)
						continue;
					

					String ner = "";

					ArrayList<Integer> resultsNer = findNerViaDBPedia(word);

					

					personCount += resultsNer.get(0);
					orgCount += resultsNer.get(1);
					locCount += resultsNer.get(2);

				}
				
				//String ner = findNER(word);
				line = inputDelegate.readFile();
			}

			inputDelegate.closeFile();
			results.add(personCount);
			results.add(orgCount);
			results.add(locCount);

			allResults.add(results);

			 OutputDelegate outSemanticVariables = new
			 OutputDelegate(trainFolder+"output.txt");
			 outSemanticVariables
			 .write("Person=" + personCount + " Organization=" + orgCount + " Location=" + locCount);
			 outSemanticVariables.newLine();
			 outSemanticVariables.stop();

		}
		return allResults;
	}

	public String findNER(String word) {
		/*
		//System.out.println(word);
		String input = word;
		
		//input= "Cumhurbaşkanı Recep Tayyip Erdoğan Bakanlar Kurulunu Ankara'da topladı";

		

		parameters.add(new BasicNameValuePair("tool", "morphanalyzer"));
		parameters.add(new BasicNameValuePair("input", input));
		parameters.add(new BasicNameValuePair("token", NLP_TOKEN));

		try {
			post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			//System.out.println("to morph");
			HttpResponse resp = client.execute(post);
			//System.out.println("mopr biter");

			String response = EntityUtils.toString(resp.getEntity());
			
			//parameters.set(0, new BasicNameValuePair("tool", "disambiguator"));
			//parameters.set(1, new BasicNameValuePair("input", response));
			
			//resp = client.execute(post);
			
			//response = EntityUtils.toString(resp.getEntity());

			parameters.set(0, new BasicNameValuePair("tool", "ner"));
			
			String[] rArr = response.split("\\n");

			String nerInput = input + " " + rArr[0];

			//for (int i = 1; i < rArr.length; i++) {
				//nerInput += "\n" + input + " " + rArr[i];
			//}

			 System.out.println(nerInput);
			//System.out.println("ner baslar");

			parameters.set(1, new BasicNameValuePair("input", nerInput));

			post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			resp = client.execute(post);
			
			//System.out.println("ner biter");

			response = EntityUtils.toString(resp.getEntity());

			 System.out.println(response);
			if (response.toLowerCase().contains("person")) {
				return "person";
			} else if (response.toLowerCase().contains("orga")) {
				return "organization";
			} else if (response.toLowerCase().contains("loc")) {
				return "location";
			}

			// System.out.println(response);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return null;
	}
	
	public ArrayList<Integer> findNerViaDBPedia(String string)
	{
		ArrayList<Integer> results = new ArrayList<>();
		
		int personCount = 0 ;
		int orgCount = 0 ;
		int locCount = 0 ;
		
		String output = "";
		

		try {
			
			try {
				dbpedia = new URL("http://localhost:8080/rest/annotate");
				dbpedia_connection = (HttpURLConnection) dbpedia.openConnection();
				dbpedia_connection.setDoOutput(true);
				dbpedia_connection.setRequestMethod("GET");
				dbpedia_connection.setRequestProperty("Accept", "application/json");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String urlParameters = "confidance=0.2&support=80&text=";
			urlParameters = urlParameters.concat(URLEncoder.encode(string));

			
			DataOutputStream wr = new DataOutputStream(dbpedia_connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			dbpedia_connection.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(dbpedia_connection.getInputStream()));
			String inputLine;
			
			while ((inputLine = in.readLine()) != null) {
				output += inputLine;
				System.out.println(inputLine);
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		personCount = StringUtils.countMatches( output,"Schema:Person");
		orgCount = StringUtils.countMatches( output,"Schema:Organization");

		locCount = StringUtils.countMatches( output,"Schema:Place");
		
		results.add(personCount);
		results.add(orgCount);
		results.add(locCount);
		
		return results;
	
	}

}
