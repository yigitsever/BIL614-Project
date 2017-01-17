import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
 * import com.hrzafer.reshaturkishstemmer.Resha;
 * import com.hrzafer.reshaturkishstemmer.Stemmer;
 */

public class Main {
	
	public static ConcurrentHashMap<String, String> nerHash = null;
	

	public static void main(String[] args) {
		
		String confPath = "app.config";
		
		if(args.length>0)
			confPath = args[0];
		
		HashMap<String,Object> config = initializeConfig(confPath);

		boolean executeMostCommon = (boolean) config.get("executeMostCommon");//true;
		boolean executeTermFreq = (boolean) config.get("executeTermFreq");
		boolean executeSemantic = (boolean) config.get("executeSemantic");
		boolean executeTermFreqForTest = (boolean) config.get("executeTermFreqForTest");
		boolean executeSemanticForTest = (boolean) config.get("executeSemanticForTest");
		boolean executePrepareTree = (boolean) config.get("executePrepareTree");
		boolean executePrepareTestTree = (boolean) config.get("executePrepareTestTree");
		boolean executeRandomForest = (boolean) config.get("executeRandomForest");

		boolean executeOnlySemanticViaWorkers = (boolean) config.get("executeOnlySemanticViaWorkers");

		if (executeOnlySemanticViaWorkers) {
			executeSemanticWorkers();
		} else {
			Manager man = new Manager((String)config.get("trainFolder"),
					(String)config.get("trainFolder"), 374);

			man.execute(false, executeMostCommon, executeTermFreq, executeSemantic, executeTermFreqForTest,
					executeSemanticForTest, executePrepareTree, executePrepareTestTree, executeRandomForest);
		}

	}

	private static HashMap<String, Object> initializeConfig(String configPath) {
		HashMap<String,Object> conf = new HashMap<>(); 
		InputDelegate idel = new InputDelegate(configPath);
		idel.openFile();
		
		String line = idel.readFile();
		while(line != null)
		{
			String[] temp = line.split("=");
			
			if(temp[1].equals("true") )
			{
				conf.put(temp[0],true);
			}
			else if(temp[1].equals("false") )
			{
				conf.put(temp[0],false);
			}
			else
			{
				conf.put(temp[0],temp[1]);
				
			}
			line = idel.readFile();
		}
		return conf;
	}

	private static void executeSemanticWorkers() {
		
		nerHash = new ConcurrentHashMap<>();
		
		

		OutputDelegate odel = new OutputDelegate("SemanticWorkerResults.txt");

		ExecutorService pool = Executors.newFixedThreadPool(6);
		Set<Future<String>> set = new HashSet<Future<String>>();

		Manager man = new Manager(null, null, 0);
		man.findStopWords();
		ArrayList<String> paths = new ArrayList<>();
		//paths.add("/Users/erantoker/Downloads/BIL614-Project-master/tek/haziran/");

		paths.add("/Users/erantoker/Downloads/BIL614-Project-master/train/haziran/");
		paths.add("/Users/erantoker/Downloads/BIL614-Project-master/train/mart/");
		paths.add("/Users/erantoker/Downloads/BIL614-Project-master/train/mayıs/");
		paths.add("/Users/erantoker/Downloads/BIL614-Project-master/train/şubat/");
		paths.add("/Users/erantoker/Downloads/BIL614-Project-master/train/temmuz/");
		paths.add("/Users/erantoker/Downloads/BIL614-Project-master/train/unpopular/");
		

		for (String word : paths) {
			Callable<String> callable = new Worker(word);
			Future<String> future = pool.submit(callable);
			set.add(future);
		}
		int sum = 0;
		for (Future<String> future : set) {
			try {
				String sa = future.get();

					odel.write(sa);
					odel.newLine();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		odel.stop();
	}

}
