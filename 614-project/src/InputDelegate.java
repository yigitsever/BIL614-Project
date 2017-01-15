import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;


/**
 * This class is for reading input file
 * @author Eran
 *
 */
public class InputDelegate {

	private String fileName;
	private Scanner input;

	InputDelegate()
	{
		fileName = "input.txt";
		input = null;
	}
	InputDelegate(String inputFile)
	{
		fileName = inputFile;
		input = null;
	}

	public void openFile() 	{
		try
		{
			input = new Scanner(
					new BufferedReader(
							new FileReader(fileName)));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
	}

	public String readFile()
	{
		while(input.hasNext())
		{
			return input.nextLine();
		}
		return null;
	}
	
	public void closeFile()
	{
		if(input != null)
		{
			input.close();
		}

	}
}