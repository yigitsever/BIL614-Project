import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class Semantic {

	private final String NLP_TOKEN = "WfR6AVnnJ8Ee3uja0NQGvFLoZcWSkaxY";
	private int locCount = 0;
	private int personCount = 0;
	private int orgCount = 0;
	private int miscCount = 0;

	private String trainFolder = "";

	public static void main(String[] args) {
		Semantic s = new Semantic("");
		//s.findSemanticVariables();
		s.findNER("Cumhurbaşkanı");
	}

	public ArrayList<Integer> semanticVariables = null;

	public Semantic(String trainFolder) {
		this.trainFolder = trainFolder;
		semanticVariables = new ArrayList<>();
	}
	
	public ArrayList<ArrayList<Integer>> findSemanticVariables(OutputDelegate outSemanticVariables)
	{
		ArrayList<ArrayList<Integer>> allResults = new ArrayList<>();
		
		File[] files = new File(trainFolder).listFiles();
		ArrayList<String> folders = new ArrayList<String>();

		int i = 0;
		for (File file : files) {
			if (file.isHidden()) {
				continue;
			}
			if (file.isDirectory()) {
				folders.add(file.getAbsolutePath());
				i++;

			}

		}
		for (String folder : folders) {
			HashMap<String,String> nerHash = new HashMap<>();
			File[] folderFiles = new File(folder).listFiles();
			for (File folderFile : folderFiles) {
				if (!folderFile.isHidden()) {
					ArrayList<Integer> results = new ArrayList<>();
					
					System.err.println(folderFile.getAbsolutePath());
					
					InputDelegate inputDelegate = new InputDelegate(folderFile.getAbsolutePath());
					inputDelegate.openFile();
					String line = inputDelegate.readFile();
					
					int personCount = 0 ;
					int locCount = 0 ;
					int orgCount = 0 ;

					while (line != null) {

						String[] words = line.split("[^\\p{L}]");
						for (String word : words) {
							
							word = word.trim();
							if (word.contains("@"))
								continue;

							if (word.trim().length() < 2)
								continue;

							if (Manager.stopWords.contains(word)) {
								continue;
							}

							word = Manager.stemmIt(word);
							
							if (Manager.stopWords.contains(word)) {
								continue;
							}
							
							String ner = "";
							
							//System.out.println(";"+word);
							
							if(nerHash.containsKey(word))
							{
								ner = nerHash.get(word);
							}
							else
							{
								ner = findNER(word);
								nerHash.put(word, ner);
							}
							
							 
							
							
							
							if( ner == null)
							{
								continue;
							}
							else if( ner.equals("person"))
							{
								personCount++;
							}
							else if( ner.equals("organization"))
							{
								orgCount++;
							}
							else if( ner.equals("location"))
							{
								locCount++;
							}

						}
						line = inputDelegate.readFile();
					}
					
					inputDelegate.closeFile();
					results.add(personCount);
					results.add(orgCount);
					results.add(locCount);
					
					allResults.add(results);
					
					outSemanticVariables.write("Person="+personCount+" Organization="+orgCount+ " Location="+locCount);
				}
			}
		}
		return allResults;
	}
	

	public String findNER(String word) {
		String input = word;
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2 * 1000).build();


		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost post = new HttpPost("http://tools.nlp.itu.edu.tr/SimpleApi");
		List<NameValuePair> parameters = new ArrayList<NameValuePair>(3);

		parameters.add(new BasicNameValuePair("tool", "morphanalyzer"));
		parameters.add(new BasicNameValuePair("input", input));
		parameters.add(new BasicNameValuePair("token", NLP_TOKEN));

		try {
			post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			HttpResponse resp = client.execute(post);

			String response = EntityUtils.toString(resp.getEntity());
			
			//System.out.println(response);

			String[] rArr = response.split("\\n");

			parameters.set(0, new BasicNameValuePair("tool", "ner"));
			
			String nerInput = input +" " + rArr[0];

			for(int i=1;i < rArr.length ; i++)
			{
				nerInput += "\n" + input +" " +rArr[i];
			}
			
			//System.out.println(nerInput);
			
			parameters.set(1, new BasicNameValuePair("input", nerInput));

			post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			resp = client.execute(post);
			
			response = EntityUtils.toString(resp.getEntity());
			
			//System.out.println(response);
			if (response.toLowerCase().contains("person")) {
				return "person";
			} else if (response.toLowerCase().contains("orga")) {
				return "organization";
			} else if (response.toLowerCase().contains("loc")) {
				return "location";
			}

			//System.out.println(response);

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
		}
		return null;
	}

}
