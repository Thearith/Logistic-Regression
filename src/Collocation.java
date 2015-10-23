/*********************************************************************
 * Name: Sothearith Sreang
 * Matric No: A0106044W
 * Program: Context-sensitive spelling correction
 *********************************************************************/

public class Collocation {

	public String word1;
	public String word2;
	public int posWord1;
	public int posWord2;
	
	public Collocation() {
		
	}
	
	public Collocation(String word1, String word2) {
		this.word1 = word1;
		this.word2 = word2;
	}
	
	public Collocation(String word1, int posWord1, String word2, int posWord2) {
		this.word1 = word1;
		this.word2 = word2;
		this.posWord1 = posWord1;
		this.posWord2 = posWord2;
	}
	
	

	@Override
	public int hashCode() {
		String word = word1 + " " + word2;
		return word.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Collocation) {
			Collocation c = (Collocation) obj;
			return this.word1.equalsIgnoreCase(c.word1) && 
					this.word2.equalsIgnoreCase(c.word2);
		}
		
		return false;
	}
	
	
}
