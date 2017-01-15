import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Textual {

	private String trainFolder = "";

	private HashMap<String, Integer> documentFrequencyMap = null;
	private ArrayList<String> mostCommonTerms = null;

	private ArrayList<Integer> termFrequencyValues = null;

	public Textual(String trainFolder) {
		documentFrequencyMap = new HashMap<>();
		mostCommonTerms = new ArrayList<>();
		termFrequencyValues = new ArrayList<>();

		this.trainFolder = trainFolder;

	}

	public ArrayList<Integer> findTermFrequencies() {
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
			Map<String, Double> vector = new HashMap<String, Double>();
			ArrayList<String> indexList = new ArrayList<String>();

			File[] folderFiles = new File(folder).listFiles();
			for (File folderFile : folderFiles) {
				if (!folderFile.isHidden()) {
					HashMap<String, Integer> termFrequencies = new HashMap<>();
					InputDelegate inputDelegate = new InputDelegate(folderFile.getAbsolutePath());
					inputDelegate.openFile();
					String line = inputDelegate.readFile();

					int wordCount = 0;
					while (line != null) {

						String[] words = line.split("[^\\p{L}]");
						for (String word : words) {
							
							word = word.trim();
							
							word = word.toLowerCase();
							if (word.contains("@"))
								continue;

							if (word.trim().length() < 2)
								continue;

							if (Manager.stopWords.contains(word)) {
								continue;
							}

							wordCount++;

							if (!mostCommonTerms.contains(word)) {
								word = Manager.stemmIt(word);

								if (Manager.stopWords.contains(word)) {
									continue;
								}
							}
							
							//System.out.println();

							if (mostCommonTerms.contains(word)) {
								if (termFrequencies.containsKey(word)) {
									termFrequencies.put(word, termFrequencies.get(word) + 1);
								} else {
									termFrequencies.put(word, 1);
								}
							} else {
								continue;
							}

						}
						line = inputDelegate.readFile();
					}
					inputDelegate.closeFile();
					
					System.out.println(termFrequencies);

					// normalizing term frequencies

					int totalTermFrequencyForFile = 0;

					for (Entry<String, Integer> entry : termFrequencies.entrySet()) {
						String key = entry.getKey();

						totalTermFrequencyForFile +=  entry.getValue() ;

					}
					
					termFrequencyValues.add(totalTermFrequencyForFile);
				}
			}
			vector = null;
			indexList = null;
		}
		
		return termFrequencyValues;
	}

	public ArrayList<String> findMostCommonTerms(int termCount) {

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
			Map<String, Double> vector = new HashMap<String, Double>();
			ArrayList<String> indexList = new ArrayList<String>();

			File[] folderFiles = new File(folder).listFiles();
			for (File folderFile : folderFiles) {
				if (!folderFile.isHidden()) {
					InputDelegate inputDelegate = new InputDelegate(folderFile.getAbsolutePath());
					inputDelegate.openFile();
					String line = inputDelegate.readFile();
					while (line != null) {

						String[] words = line.split("[^\\p{L}]");
						for (String word : words) {
							
							word = word.trim();
							word = word.toLowerCase();
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

							if (vector.containsKey(word)) {
								vector.put(word, vector.get(word) + (1.0));
							} else {
								indexList.add(word);
								vector.put(word, 1.0);
								if (documentFrequencyMap.containsKey(word)) {
									documentFrequencyMap.put(word, documentFrequencyMap.get(word) + 1);
								} else {
									documentFrequencyMap.put(word, 1);
								}
							}
						}
						line = inputDelegate.readFile();
					}
					inputDelegate.closeFile();

				}
			}
			vector = null;
			indexList = null;
		}

		Map<String, Integer> mostCommon100 = documentFrequencyMap.entrySet().stream()
				.sorted(Map.Entry.comparingByKey(Comparator.reverseOrder())).limit(termCount)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// ArrayList<String> results = new ArrayList<>();

		for (String key : mostCommon100.keySet()) {
			mostCommonTerms.add(key.toLowerCase());
		}

		return mostCommonTerms;
	}

	/*
	 * public static <K, V extends Comparable<? super V>> Map<K, V>
	 * sortByValue(Map<K, V> map) { List<Map.Entry<K, V>> list = new
	 * LinkedList<Map.Entry<K, V>>(map.entrySet()); Collections.sort(list, new
	 * Comparator<Map.Entry<K, V>>() { public int compare(Map.Entry<K, V> o1,
	 * Map.Entry<K, V> o2) { return (o2.getValue()).compareTo(o1.getValue()); }
	 * });
	 * 
	 * Map<K, V> result = new LinkedHashMap<K, V>(); for (Map.Entry<K, V> entry
	 * : list) { result.put(entry.getKey(), entry.getValue()); } return result;
	 * 
	 * }
	 */

}
