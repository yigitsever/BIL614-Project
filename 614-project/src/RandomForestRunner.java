import java.util.ArrayList;

import com.rf.fast.MainRun;

public class RandomForestRunner {

	private String trainData = "";
	private String testData = "";

	public RandomForestRunner(String trainData, String testData) {
		this.trainData = trainData;
		this.testData = testData;
	}

	public ArrayList<ArrayList<String>> run() {
		MainRun rfFast = new MainRun();

		return rfFast.RFRunner(trainData, testData, "N,N,N,N,L", "n", "n", 10, 4, 4, 4);

	}

}
