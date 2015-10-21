import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class FileReaderWriter {
	
	private static final String STOP_WORD_FILE_NAME = "stopwd.txt";
	
	public static ArrayList<String> readFromStopWordFile() {
		return readFromFile(STOP_WORD_FILE_NAME);
	}
	
	public static ArrayList<String> readFromFile(String fileName) {
		BufferedReader bufferedReader = null;
		ArrayList<String> lines = new ArrayList<String>();
		
		try {
			bufferedReader = new BufferedReader(new FileReader(fileName));
			String line = null;
			
			while((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			try {
				if(bufferedReader != null) {
					bufferedReader.close();
				}
				
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		return lines;
	}
	
	public static void writeToFile(String fileName, ArrayList<String> logs) {
		BufferedWriter bufferedWriter = null;
		
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(fileName));
			for(String log : logs)
				bufferedWriter.write(log + "\n");
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			try {
				if(bufferedWriter != null) {
					bufferedWriter.close();
				}
				
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
