/*********************************************************************
 * Name: Sothearith Sreang
 * Matric No: A0106044W
 * Program: Context-sensitive spelling correction
 *********************************************************************/


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class sctest {
	
	private static final String TEST_KEYWORD = "QWERTYUIO"; // keyword just to make sure the word does not make sense and won't be confused with test words
	private static final int COLLOCATION_RANGE = 5;
	
	private static TrainingModel model;
	private static String keyWord1;
	private static String keyWord2;
	private static String testFileName;
	private static String modelFileName;
	private static String answerFileName;
	
	private static ArrayList<String> answers;
	
	public static void main(String[] args) {
		if(args.length != 5) {
			System.err.println("This program needs five arguments: first keyword, second keyword, "
					+ "test file's name, model file's name, and answer file's name");
			return;
		}
		
		long time = System.currentTimeMillis();
		
		keyWord1 = args[0];
		keyWord2 = args[1];
		testFileName = args[2];
		modelFileName = args[3];
		answerFileName = args[4];
		
		answers = new ArrayList<String>();
		
		model = new TrainingModel();
		
		// read from model file
		ArrayList<String> modelLines = FileReaderWriter.readFromFile(modelFileName);
		parseModelSentences(modelLines);

		// read from test file and add answer to "answers" array
		ArrayList<String> testLines = FileReaderWriter.readFromFile(testFileName);
		ArrayList<String> stopWords = FileReaderWriter.readFromStopWordFile();
		parseTestSentences(testLines, stopWords);
		
		// write to answer file
		FileReaderWriter.writeToFile(answerFileName, answers);
		System.out.println("This program takes " + (System.currentTimeMillis() - time) + " milliseconds");
		for(String answer: answers) 
			System.out.println(answer);
		
	}
	
	
	/*
	 * Parse sentence from model file into words and collocations and store them in model so that we can use them to calculate
	 * probability later
	 * */
	
	private static void parseModelSentences(ArrayList<String> lines) {
		try {

			for(String line : lines) {
				String[] splits = line.split(" ");
				String split = splits[0];
				
				double weight = Double.parseDouble(splits[1]);
				
				if(split.contains(",")) { // collocation
					String[] words = split.split(",");
					model.addCollocation(words[0], words[1], weight);
				} else {
					model.addWord(split, weight);
				}
				
			}
			
		} catch(Exception e) {
			System.err.println("Parse Model sentence: " + e.getMessage());
		}
	}
	
	/*
	 * Parse sentence from test file and determine what key word it is
	 * probability later
	 * */
	
	private static void parseTestSentences(ArrayList<String> lines, ArrayList<String> stopWords) {
		try {
			for(String line : lines) {
				parseTestSentence(line, stopWords);
			}
			
			
		} catch(Exception e) {
			System.err.println("Parse Test sentence: " + e.getMessage());
		}
	}
	
	private static void parseTestSentence(String line, ArrayList<String> stopWords) throws Exception {
		String answer;
		Sentence sentence = new Sentence();
		
		// extract sentence id
		String[] split1 = line.split("\t");
		if(split1.length != 2) {
			throw new Exception("The sentence does not contain /t, " + line);
		}
		String id = split1[0];
		answer = id + "\t";
		
		// extract keyword
		String parsedLine = split1[1];
		int start = parsedLine.indexOf(">>");
		int end = parsedLine.indexOf("<<");
		parsedLine = parsedLine.substring(0, start+2) + " " + TEST_KEYWORD + 
				" " + parsedLine.substring(end, parsedLine.length());
		
		// extract every word except punctuation
		String[] words = parsedLine.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		
		// surrounding words
		for(String word : words) {
			if(!word.equals("") && !word.equals(TEST_KEYWORD) && !isStopWord(stopWords, word)) {
				sentence.addWord(word);
			}
		}
		
		// Collocations
		int keyWordIndex = getKeyWordIndex(words, TEST_KEYWORD);
		for(int relativePos = -COLLOCATION_RANGE; relativePos<=COLLOCATION_RANGE-1; relativePos++) {
			int pos = relativePos + keyWordIndex;
			if(pos < 0 || relativePos == -1 || relativePos == 0)
				continue;
			
			if(pos >= words.length-1)
				break;
			
			String word1 = words[pos];
			String word2 = words[pos+1];
			if(!word1.equals("") && !word1.equals(TEST_KEYWORD) &&
					!word2.equals("") && !word2.equals(TEST_KEYWORD)) {
				sentence.addCollocation(word1, word2);
			}
		}
		
		answer += getKeyWord(sentence, id);

		answers.add(answer);
	}
	
	
	/*
	 * Evaluating which keyword is more suitable
	 * */
	
	private static String getKeyWord(Sentence sentence, String id) throws Exception {
		String keyword = "";
		
		double value = getModelLogisticRegression(model, sentence);
		
		if(value > 0.5) { // closer to 1 -- first confusable word
			keyword = keyWord1;
		} else if(value < 0.5) { // closer to 0 -- second confusable word
			keyword = keyWord2;
		} else {
			keyword = "Cannot find which confusable word to choose";
		}
		
		return keyword;
	}
	
	private static double getModelLogisticRegression(TrainingModel model, Sentence sentence) throws Exception {
		double value = getLinearRegression(model, sentence);
		return sigmoid(value);
	}
	
	private static double getLinearRegression(TrainingModel model, Sentence sentence) throws Exception {
		double sum = 0.0f;
		
		HashMap<String, Integer> words = sentence.getWords();
		Set<String> wordSet = words.keySet();
		for(String word : wordSet) {
			sum += sentence.getWordCount(word) * model.getWordWeight(word, 0);
		}
		
		HashMap<Collocation, Integer> collocations = sentence.getCollocations();
		Set<Collocation> collocationSet = collocations.keySet();
		for(Collocation collocation : collocationSet) {
			sum += sentence.getCollocationCount(collocation) * model.getCollocationWeight(collocation, 0);
		}
		
		
		return sum;		
	}
	
	private static double sigmoid(double z) {
		return 1/(1+Math.exp(-z));
	}
	
	/*
	 * Helper methods
	 * */
	
	
	private static int getKeyWordIndex(String[] words, String keyWord) throws Exception {
		for(int index=0; index<words.length; index++) {
			if(words[index].equalsIgnoreCase(keyWord))
				return index;
		}
		
		throw new Exception("Cannot find keyword: " + keyWord);
	}
	
	private static boolean isStopWord(ArrayList<String> stopWords, String word) {
		for(String stopWord : stopWords) {
			if(stopWord.equalsIgnoreCase(word))
				return true;
		}
		
		return false;
	}
}
