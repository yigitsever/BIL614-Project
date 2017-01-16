import java.util.ArrayList;

import com.hrzafer.reshaturkishstemmer.Resha;
import com.hrzafer.reshaturkishstemmer.Stemmer;

public class Manager {
	
	public static ArrayList<String> stopWords = null;
	public static Stemmer stemmer = null;
	
	private String trainFolder = "";
	private String testFolder = "";
	
	private int nonePopularResults = 0 ;
	
	public Manager(String trainFolder, String testFolder,int nonePopularResults)
	{
		stopWords = new ArrayList<>();
		stemmer = Resha.Instance;
		
		this.trainFolder = trainFolder;
		this.testFolder = testFolder;
		this.nonePopularResults = nonePopularResults;
		
		
	}
	
	public void execute(boolean useOutputs)
	{
		
		if(useOutputs == false )
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
			
			OutputDelegate outSemanticVariables = new OutputDelegate("SemanticVariables.txt");
			
			ArrayList<ArrayList<Integer>> semanticVariables = semanticFeatures.findSemanticVariables(outSemanticVariables);
			
			outSemanticVariables.stop();
			
			//here comes to test data
			
			textualFeatures.setTrainFolder(testFolder);
			ArrayList<Integer> testTermFrequenciesForFile = textualFeatures.findTermFrequencies();
			
			OutputDelegate outTestTermFrequencies = new OutputDelegate("TestTermFrequencies.txt");
			
			for(int tf : testTermFrequenciesForFile )
			{
				outTestTermFrequencies.write(tf+""); 
				outTestTermFrequencies.newLine();
			}
			
			outTestTermFrequencies.stop();
			
			OutputDelegate outTestSemanticVariables = new OutputDelegate("TestSemanticVariables.txt");
			
			Semantic testSemanticFeatures = new Semantic(testFolder);
			
			ArrayList<ArrayList<Integer>> testSemanticVariables = testSemanticFeatures.findSemanticVariables(outTestSemanticVariables);
			
			outTestSemanticVariables.stop();
			
			
		}
		
		prepareTreeData("TermFrequencies.txt","SemanticVariables.txt","TreeData.txt");
		prepareTestTreeData("TestTermFrequencies.txt","TestSemanticVariables.txt","TestData.txt");
		
		
		
		RandomForestRunner rfRunner = new RandomForestRunner("TreeData.txt","TestData.txt");
		
		ArrayList<ArrayList<String>> rfResults = rfRunner.run();
		
		OutputDelegate outRFResults = new OutputDelegate("RandomForestResults.txt");
		
		for(ArrayList<String> rfResult : rfResults)
		{
			outRFResults.write(rfResult.get(1) +" " + rfResult.get(0));
			outRFResults.newLine();
		}
		
		outRFResults.stop();
		
		
	}
	
	private void prepareTestTreeData(String termFrequenciesFile, String semanticVariablesFiles, String treeDataFile) {
		InputDelegate termFile = new InputDelegate(termFrequenciesFile);
		InputDelegate semanticFile = new InputDelegate(semanticVariablesFiles);
		OutputDelegate treeFile = new OutputDelegate(treeDataFile);
		
		termFile.openFile();
		semanticFile.openFile();
		
		
		String lineTerm = termFile.readFile();
		String lineSem = semanticFile.readFile();
		
		
		while(lineTerm != null)
		{
			treeFile.write(lineTerm);
			treeFile.write(",");
			
			lineSem = lineSem.replaceAll("Person=", "");
			lineSem = lineSem.replace("Organization=", "");
			lineSem = lineSem.replaceAll("Location=", "");
			
			String[] temp = lineSem.split("\\s+");
			
			treeFile.write(temp[0]);
			treeFile.write(",");
			treeFile.write(temp[1]);
			treeFile.write(",");
			treeFile.write(temp[2]);
			
			lineTerm = termFile.readFile();
			lineSem = semanticFile.readFile();
			
			treeFile.newLine();
		}
		
		treeFile.stop();
		termFile.closeFile();
		semanticFile.closeFile();
		
		
	}

	private void prepareTreeData(String termFrequenciesFile, String semanticVariablesFiles, String treeDataFile) {
		InputDelegate termFile = new InputDelegate(termFrequenciesFile);
		InputDelegate semanticFile = new InputDelegate(semanticVariablesFiles);
		OutputDelegate treeFile = new OutputDelegate(treeDataFile);
		
		termFile.openFile();
		semanticFile.openFile();
		
		
		String lineTerm = termFile.readFile();
		String lineSem = semanticFile.readFile();
		
		int fileCount = 0;
		
		while(lineTerm != null)
		{
			treeFile.write(lineTerm);
			treeFile.write(",");
			
			lineSem = lineSem.replaceAll("Person=", "");
			lineSem = lineSem.replace("Organization=", "");
			lineSem = lineSem.replaceAll("Location=", "");
			
			String[] temp = lineSem.split("\\s+");
			
			treeFile.write(temp[0]);
			treeFile.write(",");
			treeFile.write(temp[1]);
			treeFile.write(",");
			treeFile.write(temp[2]);
			treeFile.write(",");
			
			if(fileCount< nonePopularResults)
				treeFile.write("yes");
			else
				treeFile.write("no");
			
			lineTerm = termFile.readFile();
			lineSem = semanticFile.readFile();
			
			treeFile.newLine();
			
			fileCount++;
		}
		
		treeFile.stop();
		termFile.closeFile();
		semanticFile.closeFile();
		
		
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
