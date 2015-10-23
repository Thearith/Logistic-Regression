/*********************************************************************
 * Name: Sothearith Sreang
 * Matric No: A0106044W
 * Program: Context-sensitive spelling correction
 *********************************************************************/


import java.util.HashMap;

public class Sentence {

	protected static final int FIRST_WORD_OUTPUT = 1;
	protected static final int SECOND_WORD_OUTPUT = 1 - FIRST_WORD_OUTPUT;
	
	private int output;
	private HashMap<String, Integer> words;
	private HashMap<Collocation, Integer> collocations;
	
	public Sentence() {
		words = new HashMap<String, Integer>();
		collocations = new HashMap<Collocation, Integer>();
	}
	
	public Sentence(int output) {
		this.output = output;
		words = new HashMap<String, Integer>();
		collocations = new HashMap<Collocation, Integer>();
	}
	
	
	/*
	 * getters and setters
	 * */
	
	public int getExpectedOutput() {
		return this.output;
	}
	
	public void setExpectedOutput(int output) {
		this.output = output;
	}
	
	public HashMap<String, Integer> getWords() {
		return this.words;
	}
	
	public HashMap<Collocation, Integer> getCollocations() {
		return this.collocations;
	}
	
	/*
	 * Surrounding Words
	 * */
	
	public void addWord(String word) {
		int count = getWordCount(word) + 1;
		words.put(word, count);
	}
	
	public int getWordCount(String word) {
		return words.containsKey(word) ?
				words.get(word) : 0;
	}
	
	
	/*
	 * Collocations
	 * */
	
	public void addCollocation(String word1, String word2) {
		Collocation collocation = new Collocation(word1, word2);
		int count = getCollocationCount(collocation) + 1;
		collocations.put(collocation, count);
	}
	
	public int getCollocationCount(Collocation collocation) {
		return collocations.containsKey(collocation) ?
				collocations.get(collocation) : 0;
	}
}
