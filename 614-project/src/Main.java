/*
 * import com.hrzafer.reshaturkishstemmer.Resha;
 * import com.hrzafer.reshaturkishstemmer.Stemmer;
 */


public class Main {

	public static void main(String[] args) {
		//CHECK readInput() //use https://github.com/hrzafer/resha-turkish-stemmer check
		//CHECK findMostCommonTerms check
		//CHECK take tf in most common terms check
		//CHECK use morphAnalyzer and ner for find person count vs 
		//CHECK write to output file check
		//TODO using output file create RF decision tree
		//TODO using RF find popular or not
		
		Manager man = new Manager("/Users/erantoker/Documents/workspace/614-project/popular/");
		man.execute();
		
		/*Stemmer stemmer = Resha.Instance;

		String stem = stemmer.stem("kitapçıdaki");
		System.out.println(stem); //kitapçı */


	}

}
