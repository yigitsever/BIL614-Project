import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

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

public class Worker implements Callable<String> {
	private String trainFolder = "";

	private final String NLP_TOKEN = "WfR6AVnnJ8Ee3uja0NQGvFLoZcWSkaxY";
	
	//private HashMap<String,String> nerHash = null;
	
	private HttpClient client =null;
	private HttpPost post =null;
	private List<NameValuePair> parameters = null;

	public Worker(String fileName) {
		this.trainFolder = fileName;
		//this.nerHash = new HashMap<>();
		
		RequestConfig requestConfig = RequestConfig.custom()/*.setConnectTimeout( 2000).setConnectionRequestTimeout(2000)*/.setSocketTimeout(10000).build();

		
		this.client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		this.post = new HttpPost("http://tools.nlp.itu.edu.tr/SimpleApi");
		this.parameters = new ArrayList<NameValuePair>(3);
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
				String[] words = line.split("[^\\p{L}]");
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

					if (word.trim().length() < 2)
						continue;
					// System.out.println("stop words bakacağım");
					if (Manager.stopWords.contains(word)) {
						continue;
					}
					// System.out.println("stop words baktım");

					// word = Manager.stemmIt(word);

					// System.out.println("stem ettim");

					if (Manager.stopWords.contains(word)) {
						continue;
					}

					String ner = "";

					// System.out.println(";" + word);

					if (Main.nerHash.containsKey(word)) {

						ner =Main.nerHash.get(word);

						if (ner.equals(""))
							continue;
					} else {
						ner = findNER(word);

						if (ner == null)
							Main.nerHash.put(word, "");
						else
							Main.nerHash.put(word, ner);
					}

					if (ner == null) {
						continue;
					} else if (ner.equals("person")) {
						personCount++;
					} else if (ner.equals("organization")) {
						orgCount++;
					} else if (ner.equals("location")) {
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
		//System.out.println(word);
		String input = word;

		

		parameters.add(new BasicNameValuePair("tool", "morphanalyzer"));
		parameters.add(new BasicNameValuePair("input", input));
		parameters.add(new BasicNameValuePair("token", NLP_TOKEN));

		try {
			post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			//System.out.println("to morph");
			HttpResponse resp = client.execute(post);
			//System.out.println("mopr biter");

			String response = EntityUtils.toString(resp.getEntity());

			 System.out.println(response);

			String[] rArr = response.split("\\n");

			parameters.set(0, new BasicNameValuePair("tool", "ner"));

			String nerInput = input + " " + rArr[0];

			/*for (int i = 1; i < rArr.length; i++) {
				nerInput += "\n" + input + " " + rArr[i];
			}*/

			// System.out.println(nerInput);
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
		}
		return null;
	}

}
