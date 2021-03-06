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
	
	public void execute(boolean useOutputs, boolean executeMostCommon, boolean executeTermFreq, boolean executeSemantic, boolean executeTermFreqForTest, boolean executeSemanticForTest, boolean executePrepareTree, boolean executePrepareTestTree, boolean executeRandomForest)
	{
		
		if(useOutputs == false )
		{

			findStopWords();
			
			Textual textualFeatures = new Textual(trainFolder); 
			Semantic semanticFeatures = new Semantic(trainFolder);
			
			if(executeMostCommon)
			{
				ArrayList<String> mostCommonTerms = textualFeatures.findMostCommonTerms(200);
				
				OutputDelegate outMostCommon = new OutputDelegate("MostCommonTerms.txt");
				
				for(String mct : mostCommonTerms)
				{
					outMostCommon.write(mct);
					outMostCommon.newLine();
				}
				
				outMostCommon.stop();
			}
			
			if(executeTermFreq)
			{
				ArrayList<Integer> termFrequenciesForFile = textualFeatures.findTermFrequencies();
				
				OutputDelegate outTermFrequencies = new OutputDelegate("TermFrequencies.txt");
				
				for(int tf : termFrequenciesForFile )
				{
					outTermFrequencies.write(tf+""); 
					outTermFrequencies.newLine();
				}
				
				outTermFrequencies.stop();
			}
			
			if(executeSemantic)
			{
				
				ArrayList<ArrayList<Integer>> semanticVariables = semanticFeatures.findSemanticVariables("SemanticVariables.txt");
				
				
			}
			
			
			
			
			//here comes to test data
			
			if(executeTermFreqForTest)
			{
				textualFeatures.setTrainFolder(testFolder);
				ArrayList<Integer> testTermFrequenciesForFile = textualFeatures.findTermFrequencies();
				
				OutputDelegate outTestTermFrequencies = new OutputDelegate("TestTermFrequencies.txt");
				
				for(int tf : testTermFrequenciesForFile )
				{
					outTestTermFrequencies.write(tf+""); 
					outTestTermFrequencies.newLine();
				}
				
				outTestTermFrequencies.stop();
			}
			
			if(executeSemanticForTest)
			{
				
				Semantic testSemanticFeatures = new Semantic(testFolder);
				
				ArrayList<ArrayList<Integer>> testSemanticVariables = testSemanticFeatures.findSemanticVariables("TestSemanticVariables.txt");
				
			}
			
		
			
			
		}
		
		if(executePrepareTree)
		{
			prepareTreeData("TermFrequencies.txt","SemanticVariables.txt","TreeData.txt");
		}
		
		if(executePrepareTestTree)
		{
			prepareTestTreeData("TestTermFrequencies.txt","TestSemanticVariables.txt","TestData.txt");
		}
		
		
		if(executeRandomForest)
		{
			RandomForestRunner rfRunner = new RandomForestRunner("TreeData.txt","TestData.txt");
			
			ArrayList<ArrayList<String>> rfResults = rfRunner.run();
			
			OutputDelegate outRFResults = new OutputDelegate("RandomForestResults.txt");
			

			int count = 0 ; 
			
			int unpopular = 48 ; // 48den sonrasi unpopular
			
			int truePositive = 0 ;
			int falsePositive = 0 ;
			int falseNegative = 0 ;
			
			for(ArrayList<String> rfResult : rfResults)
			{
				String r = rfResult.get(0);
				outRFResults.write(rfResult.get(1) +" " + rfResult.get(0));
				outRFResults.newLine();
				
				if(r.equals("yes") && (count<unpopular))
				{
					truePositive++;
				}
				else if(r.equals("yes") && count >= unpopular)
				{
					falsePositive++;
				}
				else if( r.equals("no") && count < unpopular)
				{
					falseNegative++;
				}
				
				
				
				count++ ;
			}
			
			System.err.println("True Positive" + truePositive);
			System.err.println("False Positive" + falsePositive);
			System.err.println("False Negative" + falseNegative);
			
			double pre = (double) truePositive / (double)(truePositive + falsePositive) ;
			double rec = (double) truePositive / (double)(truePositive + falseNegative) ;
			
			System.err.println("Precision " + pre);
			System.err.println("Recall " + rec);
			
			double f1 = (2* pre * rec) / (pre + rec);
			
			System.err.println("F1 score" +f1);
			
			outRFResults.stop();
		}
		
		
		
		
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
			
			treeFile.write(",?");
			
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

	public void findStopWords() {
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
