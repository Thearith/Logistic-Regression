/*********************************************************************
 * Name: Sothearith Sreang
 * Matric No: A0106044W
 * Program: Context-sensitive spelling correction
 *********************************************************************/


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class sctrain {
	
	private static final int COLLOCATION_RANGE = 3;
	private static final int NUM_ITERATIONS = 10000; 
	private static final double LEARNING_RATE = 1/(double) NUM_ITERATIONS;
	
	private static TrainingModel model;
	private static String keyWord1;
	private static String keyWord2;
	private static String trainFileName;
	private static String modelFileName;
	
	private static ArrayList<Sentence> sentences;

	public static void main(String[] args) {
		if(args.length != 4) {
			System.err.println("This program needs four arguments: first keyword, second keyword, "
					+ "training file's name, and model file's name");
			return;
		}
		
		long time = System.currentTimeMillis();
		
		keyWord1 = args[0];
		keyWord2 = args[1];
		trainFileName = args[2];
		modelFileName = args[3];
		
		model = new TrainingModel();
		
		sentences = new ArrayList<Sentence>();

		// parse words and store inputs in "sentences"
		ArrayList<String> lines = FileReaderWriter.readFromFile(trainFileName);
		ArrayList<String> stopWords = FileReaderWriter.readFromStopWordFile();
		parseSentences(lines, stopWords);
		
		// train model
		trainModel();
		
		System.out.println("This program takes " + (System.currentTimeMillis() - time) + " milliseconds");
		
		// write models to text files
		ArrayList<String> modelLogs = model.getModelLogs();
		FileReaderWriter.writeToFile(modelFileName, modelLogs);
		
		// model.printModel();
	}
	
	
	/*
	 * Parse sentence into words and collocations and store them in "sentences" and "model"
	 * */
	
	private static void parseSentences(ArrayList<String> lines, ArrayList<String> stopWords) {
		try {
			for(String line : lines) {
				parseSentence(line, stopWords);
			}
			
		} catch(Exception e) {
			System.err.println("Parse sentence: " + e.getMessage());
		}
	}
	
	
	private static void parseSentence(String line, ArrayList<String> stopWords) throws Exception{
		Sentence sentence = new Sentence();
		
		// extract sentence id
		String[] split1 = line.split("\t");
		if(split1.length != 2) {
			throw new Exception("The sentence does not contain /t, " + line);
		}
		
		// extract keyword
		String parsedLine = split1[1];
		int start = parsedLine.indexOf(">>");
		int end = parsedLine.indexOf("<<");
		String keyword = parsedLine.substring(start+2, end); // exclude << and >>
		keyword = keyword.replaceAll(" ", "").toLowerCase();
		sentence.setExpectedOutput(getExpectedOutputFromKeyword(keyword));
		
		// extract every word except punctuation 
		String[] words = parsedLine.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		
		// Feature : surrounding words
		for(String word : words) {
			if(!word.equals("") && !word.equals(keyword) && !isStopWord(stopWords, word)) {
				model.addWord(word);
				sentence.addWord(word);
			}
		}
		
		// Feature : Collocations
		int keyWordIndex = getKeyWordIndex(words, keyword);
		
		for(int relativePos = -COLLOCATION_RANGE; relativePos<=COLLOCATION_RANGE-1; relativePos++) {
			int pos = relativePos + keyWordIndex;
			if(pos < 0 || relativePos == -1 || relativePos == 0)
				continue;
			
			if(pos >= words.length-1)
				break;
			
			String word1 = words[pos];
			String word2 = words[pos+1];
			if(!word1.equals("") && !word1.equals(keyword) &&
					!word2.equals("") && !word2.equals(keyword)) {
				model.addCollocation(word1, word2);
				sentence.addCollocation(word1, word2);
			}
		}
		
		sentences.add(sentence);
	}
	
	
	/*
	 * Train model using Logistic regression and Ascent gradient
	 * */
	
	private static void trainModel() {
		try{
			for(int iteration = 0; iteration < NUM_ITERATIONS; iteration++) {
				
				if(iteration % 1000 == 0)
					System.out.println("Processing iteration " + iteration);
				
				for(Sentence sentence : sentences) {
					double logisticRegressionVal = getLogisticRegression(sentence);
					double expectedOutput = sentence.getExpectedOutput();
					
					HashMap<String, Integer> words = sentence.getWords();
					Set<String> wordSet = words.keySet();
					for(String word : wordSet) {
						double weight = model.getWordWeight(word) 
								+ LEARNING_RATE * sentence.getWordCount(word) * 
								(expectedOutput - logisticRegressionVal);
						model.changeWordWeight(word, weight);
					}
					
					HashMap<Collocation, Integer> collocations = sentence.getCollocations();
					Set<Collocation> collocationSet = collocations.keySet();
					for(Collocation collocation : collocationSet) {
						double weight = model.getCollocationWeight(collocation) + 
								LEARNING_RATE * sentence.getCollocationCount(collocation) *
								(expectedOutput - logisticRegressionVal);
						model.changeCollocationWeight(collocation, weight);
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Train: " + e.getMessage());
		}
	}
	
	private static double getLogisticRegression(Sentence sentence) throws Exception {
		double value = getLinearRegression(sentence);
		return sigmoid(value);
	}
	
	private static double getLinearRegression(Sentence sentence) throws Exception {
		double sum = 0.0f;
		
		HashMap<String, Integer> words = sentence.getWords();
		Set<String> wordSet = words.keySet();
		for(String word : wordSet) {
			sum += sentence.getWordCount(word) * model.getWordWeight(word);
		}
		
		HashMap<Collocation, Integer> collocations = sentence.getCollocations();
		Set<Collocation> collocationSet = collocations.keySet();
		for(Collocation collocation : collocationSet) {
			sum += sentence.getCollocationCount(collocation) * model.getCollocationWeight(collocation);
		}
		
		
		return sum;		
	}
	
	private static double sigmoid(double z) {
		return 1/(1+Math.exp(-z));
	}
	
	
	/*
	 * Helper methods
	 * */
	
	private static int getExpectedOutputFromKeyword(String keyWord) throws Exception {
		if(keyWord1.equals(keyWord))
			return Sentence.FIRST_WORD_OUTPUT;
		else if(keyWord2.equals(keyWord))
			return Sentence.SECOND_WORD_OUTPUT;
		else
			throw new Exception("Keyword " + keyWord + " are not amongst the key words: " + 
					keyWord1 + ", " + keyWord2);
	}
	
	private static int getKeyWordIndex(String[] words, String keyWord) throws Exception {
		for(int index=0; index<words.length; index++) {
			if(words[index].equals(keyWord))
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
