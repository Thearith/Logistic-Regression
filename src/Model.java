import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class Model {
	
	private String keyWord;
	private HashMap<String, CountAndWeight> surroundingWords;
	private HashMap<Collocation, CountAndWeight> collocations;
	
	public Model(String keyword) {
		setKeyWord(keyword);
		surroundingWords = new HashMap<String, CountAndWeight>();
		collocations = new HashMap<Collocation, CountAndWeight>();
	}
	
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	
	public String getKeyWord() {
		return this.keyWord;
	}
	
	public void addWord(String word) {
		CountAndWeight obj = getWordCountAndWeight(word);
		obj.count++;
		surroundingWords.put(word, obj);
	}
	
	public CountAndWeight getWordCountAndWeight(String word) {
		return surroundingWords.containsKey(word) ? 
				surroundingWords.get(word) : new CountAndWeight(0);
	}
	
	public void addCollocation(String word1, String word2) {
		Collocation collocation = new Collocation(word1, word2);
		CountAndWeight obj = getCollocationCountAndWeight(collocation);
		obj.count++;
		collocations.put(collocation, obj);
	}
	
	public CountAndWeight getCollocationCountAndWeight(Collocation collocation) {
		return collocations.containsKey(collocation) ?
				collocations.get(collocation) : new CountAndWeight(0);
	}
	
	public void printModel() {
		System.out.println("------------------------------");
		System.out.println("Keyword: " + keyWord);
	
		Set set = surroundingWords.entrySet();
		Iterator i = set.iterator();

		while(i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			System.out.print(me.getKey() + ": ");
			System.out.println(me.getValue());
		}
		
		System.out.println("\n\n\n\n\n");
	}
}
