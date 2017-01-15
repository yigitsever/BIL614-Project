import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class makes output operations
 * @author Ridvan
 *
 */
public class OutputDelegate {

	private BufferedWriter buff;
	private FileWriter output;
	public OutputDelegate(String fileName) {
		try {
			output =new FileWriter(fileName,true);
			buff=new BufferedWriter(output);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public void newLine()
	{
		try {
			buff.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(String s) {
		try {
			buff.write(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void stop() {
		try {
			buff.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}