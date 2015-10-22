import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class findDiff {
	public static void main(String[] args) throws IOException{
		String file1 = args[0], file2 = args[1];
		BufferedReader fin1 = new BufferedReader(new FileReader(file1));
		BufferedReader fin2 = new BufferedReader(new FileReader(file2));
		String input1, input2;
		int diff = 0;
		int num = 0;
		input1 = fin1.readLine();
		input2 = fin2.readLine();
		while(input1 != null && input2 != null) {
			if(!(input1.equalsIgnoreCase(input2))){
				diff++;
			}
			num++;
			input1 = fin1.readLine();
			input2 = fin2.readLine();
		}
		System.out.println(diff + " out of " + num + " is wrong");
		fin1.close();
		fin2.close();
	}
}
