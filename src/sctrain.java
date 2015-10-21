import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class sctrain {
	
	private static final int COLLOCATION_RANGE = 5;
	private static final int NUM_ITERATIONS = 100000; 
	private static final double LEARNING_RATE = 1/(double) NUM_ITERATIONS;
	
	private static Model model1;
	private static Model model2;
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
		
		String keyWord1 = args[0];
		String keyWord2 = args[1];
		trainFileName = args[2];
		modelFileName = args[3];
		
		model1 = new Model(keyWord1);
		model2 = new Model(keyWord2);
		
		sentences = new ArrayList<Sentence>();

		// parse words and store inputs in sentences
		ArrayList<String> lines = FileReaderWriter.readFromFile(trainFileName);
		ArrayList<String> stopWords = FileReaderWriter.readFromStopWordFile();
		parseSentences(lines, stopWords);
		
		// train model
		trainModel();
		
		System.out.println("This program takes " + (System.currentTimeMillis() - time) + " milliseconds");
		
		// write models to text files
		ArrayList<String> modelLogs = model1.getModelLogs();
		modelLogs.addAll(model2.getModelLogs());
		FileReaderWriter.writeToFile(modelFileName, modelLogs);
		
		for(String log : modelLogs)
			System.out.println(log);
		
	}
	
	
	/*
	 * Parse sentence into words and collocations and store them in "sentences" and "model1" or "model2"
	 * */
	
	private static void parseSentences(ArrayList<String> lines, ArrayList<String> stopWords) {
		try {
			int index = 0;
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
		sentence.setOutput(keyword, getKeywordOutput(keyword));
		
		// extract every word except punctuation 
		Model model = getModel(keyword);
		String[] words = parsedLine.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		
		// surrounding words
		for(String word : words) {
			if(!word.equals("") && !word.equals(keyword) && !isStopWord(stopWords, word)) {
				model.addWord(word);
				sentence.addWord(word);
			}
		}
		
		// Collocations
		int keyWordIndex = getKeyWordIndex(words, keyword);
		
		for(int relativePos = -COLLOCATION_RANGE; relativePos<=COLLOCATION_RANGE-1; relativePos++) {
			int pos = relativePos + keyWordIndex;
			if(pos < 0 || relativePos == -1 || relativePos == 0)
				continue;
			
			if(pos >= words.length-1)
				break;
			
			String word1 = words[pos];
			String word2 = words[pos+1];
			if(!word1.equals("") && !word1.equals(keyword) && !isStopWord(stopWords, word1) &&
					!word2.equals("") && !word2.equals(keyword) && !isStopWord(stopWords, word2)) {
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
				System.out.println("Processing iteration " + iteration);
				for(Sentence sentence : sentences) {
					double logisticRegressionVal = getLogisticRegression(sentence);
					double expectedOutput = sentence.getOutput();
					Model model = getModel(sentence.getKeyword());
					
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
		String keyword = sentence.getKeyword();
		Model model = getModel(keyword);
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
	
	private static Model getModel(String keyWord) throws Exception{
		String keyword1 = model1.getKeyWord();
		String keyword2 = model2.getKeyWord();
		
		if(keyword1.equals(keyWord))
			return model1;
		else if(keyword2.equals(keyWord))
			return model2;
		else
			throw new Exception("Keyword " + keyWord + " are not amongst the key words: " + 
					keyword1 + ", " + keyword2);
	}
	
	private static int getKeywordOutput(String keyWord) throws Exception {
		String keyword1 = model1.getKeyWord();
		String keyword2 = model2.getKeyWord();
		
		if(keyword1.equals(keyWord))
			return Sentence.FIRST_WORD_OUTPUT;
		else if(keyword2.equals(keyWord))
			return Sentence.SECOND_WORD_OUTPUT;
		else
			throw new Exception("Keyword " + keyWord + " are not amongst the key words: " + 
					keyword1 + ", " + keyword2);
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
