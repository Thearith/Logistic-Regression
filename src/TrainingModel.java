import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class TrainingModel {
	
	protected static final String KEYWORD = "Keyword";
	
	private HashMap<String, Double> surroundingWords;
	private HashMap<Collocation, Double> collocations;
	
	public TrainingModel() {
		surroundingWords = new HashMap<String, Double>();
		collocations = new HashMap<Collocation, Double>();
	}
	
	
	/*
	 * Surrounding words
	 * */
	
	public void addWord(String word) {
		double weight = getWordWeight(word);
		surroundingWords.put(word, weight);
	}
	
	public void addWord(String word, double weight) {
		changeWordWeight(word, weight);
	}
	
	public void changeWordWeight(String word, double weight) {
		surroundingWords.put(word,  weight);
	}
	
	public double getWordWeight(String word) {
		return surroundingWords.containsKey(word) ? 
				surroundingWords.get(word) : getInitialWeight();
	}
	
	public double getWordWeight(String word, double defaultValue) {
		return surroundingWords.containsKey(word) ? 
				surroundingWords.get(word) : defaultValue;
	}
	
	
	/*
	 * Collocation 
	 * */
	
	public void addCollocation(String word1, String word2) {
		Collocation collocation = new Collocation(word1, word2);
		double weight = getCollocationWeight(collocation);
		collocations.put(collocation, weight);
	}
	
	public void addCollocation(String word1, String word2, double weight) {
		changeCollocationWeight(word1, word2, weight);
	}
	
	public void changeCollocationWeight(String word1, String word2, double weight) {
		Collocation collocation = new Collocation(word1, word2);
		collocations.put(collocation, weight);
	}
	
	public void changeCollocationWeight(Collocation collocation, double weight) {
		collocations.put(collocation, weight);
	}
	
	public double getCollocationWeight(Collocation collocation) {
		return collocations.containsKey(collocation) ?
				collocations.get(collocation) : getInitialWeight();
	}
	
	public double getCollocationWeight(Collocation collocation, double defaultValue) {
		return collocations.containsKey(collocation) ?
				collocations.get(collocation) : defaultValue;
	}
	
	
	/*
	 * Helper function
	 * */
	
	private double getInitialWeight() {
		return 0;
	}
	
	public void printModel() {
		ArrayList<String> logs = getModelLogs();
		for(String log : logs)
			System.out.println(log);
	}
	
	public ArrayList<String> getModelLogs() {
		ArrayList<String> logs = new ArrayList<String>(); 
		
		String log = "";
		
		Set<String> wordSet = surroundingWords.keySet();
		for(String word : wordSet) {
			log = word + " " + surroundingWords.get(word);
			logs.add(log);
		}
		
		Set<Collocation> collocationSet = collocations.keySet();
		for(Collocation collocation : collocationSet) {
			log = collocation.word1 + "," + collocation.word2 + 
					" " + collocations.get(collocation);
			logs.add(log);
		}
		
		return logs;
	}
}
