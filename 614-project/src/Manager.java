import java.util.ArrayList;

import com.hrzafer.reshaturkishstemmer.Resha;
import com.hrzafer.reshaturkishstemmer.Stemmer;

public class Manager {
	
	public static ArrayList<String> stopWords = null;
	public static Stemmer stemmer = null;
	
	private String trainFolder = "";
	
	public Manager(String trainFolder)
	{
		stopWords = new ArrayList<>();
		stemmer = Resha.Instance;
		
		this.trainFolder = trainFolder;
		
		
	}
	
	public void execute()
	{
		findStopWords();
		
		Textual textualFeatures = new Textual(trainFolder); 
		Semantic semanticFeatures = new Semantic(trainFolder);
		
		ArrayList<String> mostCommonTerms = textualFeatures.findMostCommonTerms(100);
		
		OutputDelegate outMostCommon = new OutputDelegate("MostCommonTerms.txt");
		
		for(String mct : mostCommonTerms)
		{
			outMostCommon.write(mct);
			outMostCommon.newLine();
		}
		
		outMostCommon.stop();
		
		ArrayList<Integer> termFrequenciesForFile = textualFeatures.findTermFrequencies();
		
		OutputDelegate outTermFrequencies = new OutputDelegate("TermFrequencies.txt");
		
		for(int tf : termFrequenciesForFile )
		{
			outTermFrequencies.write(tf+""); 
			outTermFrequencies.newLine();
		}
		
		outTermFrequencies.stop();
		
		
		
		/*
		 * 
		 * ArrayList<ArrayList<Integer>> semanticVariables = semanticFeatures.findSemanticVariables();
		
		OutputDelegate outSemanticVariables = new OutputDelegate("SemanticVariables.txt");
		
		for(ArrayList<Integer> sem : semanticVariables )
		{
			outSemanticVariables.write("Person="+sem.get(0)+" Organization="+sem.get(1) + " Location="+sem.get(2));
			outSemanticVariables.newLine();
		}
		
		outSemanticVariables.stop();
		
		*/
	}
	
	private void findStopWords() {
		InputDelegate idel = new InputDelegate("stopWords.txt");
		
		idel.openFile();
		
		String line = idel.readFile();
		
		while(line!= null)
		{
			stopWords.add(line);
			
			String[] words = line.split("\\s+");
			
			if(words.length > 1)
			{
				for(String word : words)
				{
					if(!stopWords.contains(word))
					{
						stopWords.add(word);
					}
				}
				
			}
			
			line = idel.readFile();
			
			
		}
		
		idel.closeFile();
		
		System.out.println(stopWords);
		
	}
	
	public static String stemmIt(String word) {
		

		String stem = stemmer.stem(word);
		
		return stem;
	}
}
