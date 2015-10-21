import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class Model {
	
	private String keyWord;
	private HashMap<String, Double> surroundingWords;
	private HashMap<Collocation, Double> collocations;
	
	public Model(String keyword) {
		setKeyWord(keyword);
		surroundingWords = new HashMap<String, Double>();
		collocations = new HashMap<Collocation, Double>();
	}
	
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	
	public String getKeyWord() {
		return this.keyWord;
	}
	
	
	/*
	 * Surrounding words
	 * */
	
	public void addWord(String word) {
		double weight = getWordWeight(word);
		surroundingWords.put(word, weight);
	}
	
	public void changeWordWeight(String word, double weight) {
		surroundingWords.put(word,  weight);
	}
	
	public double getWordWeight(String word) {
		return surroundingWords.containsKey(word) ? 
				surroundingWords.get(word) : getRandomizedWeight();
	}
	
	
	/*
	 * Collocation 
	 * */
	
	public void addCollocation(String word1, String word2) {
		Collocation collocation = new Collocation(word1, word2);
		double weight = getCollocationWeight(collocation);
		collocations.put(collocation, weight);
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
				collocations.get(collocation) : getRandomizedWeight();
	}
	
	
	/*
	 * Helper function
	 * */
	
	private double getRandomizedWeight() {
		return Math.random();
	}
	
	public void printModel() {
		System.out.println("------------------------------");
		System.out.println("Keyword: " + keyWord);
		
		Set<String> wordSet = surroundingWords.keySet();
		for(String word : wordSet) {
			System.out.println(word + ": " + surroundingWords.get(word));
		}
		
		Set<Collocation> collocationSet = collocations.keySet();
		for(Collocation collocation : collocationSet) {
			System.out.println(collocation.word1 + " " + collocation.word2 + 
					": " + surroundingWords.get(collocation));
		}
		
		System.out.println("\n\n\n\n\n");
	}
}
