import java.util.ArrayList;

public class sctrain {
	
	private static final int COLLOCATION_RANGE = 5;
	
	private static Model model1;
	private static Model model2;
	private static String trainFileName;
	private static String modelFileName;
	
	private static ArrayList<String> sentences;
	
	private static ArrayList<String> stopWords;

	public static void main(String[] args) {
		if(args.length != 4) {
			System.err.println("This program needs four arguments: first keyword, second keyword, "
					+ "training file's name, and model file's name");
			return;
		}
		
		String keyWord1 = args[0];
		String keyWord2 = args[1];
		trainFileName = args[2];
		modelFileName = args[3];
		
		model1 = new Model(keyWord1);
		model2 = new Model(keyWord2);

		sentences = FileReaderWriter.readFromFile(trainFileName);
		stopWords = FileReaderWriter.readFromStopWordFile();
		parseSentences();
		
	}
	
	private static void parseSentences() {
		try {
			
			for(String sentence : sentences) {
				parseSentence(sentence);
			}
			
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	
	private static void parseSentence(String line) throws Exception{
		// extract sentence id
		String[] split1 = line.split("\t");
		if(split1.length != 2) {
			throw new Exception("The sentence does not contain \t, " + line);
		}
		
		// extract keyword
		String parsedLine = split1[1];
		int start = parsedLine.indexOf(">>");
		int end = parsedLine.indexOf("<<");
		String keyword = parsedLine.substring(start+2, end); // exclude << and >>
		keyword = keyword.replaceAll(" ", "").toLowerCase();
		
		// extract every word except punctuation 
		Model model = getModel(keyword);
		String[] words = parsedLine.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		
		// surrounding words
		for(String word : words) {
			if(!word.equals("") && !word.equals(keyword) && !isStopWord(word))
				model.addWord(word);
		}
		
		// Collocations
		int keyWordIndex = getKeyWordIndex(words, keyword);
		
		for(int relativePos = -3; relativePos<=2; relativePos++) {
			int pos = relativePos + keyWordIndex;
			if(pos < 0 || relativePos == -1 || relativePos == 0)
				continue;
			
			if(pos == words.length)
				break;
			
			int secondPos = pos+1;
			model.addCollocation(words[pos], words[secondPos]);
		}
		
		
	}
	
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
	
	private static int getKeyWordIndex(String[] words, String keyWord) throws Exception {
		for(int index=0; index<words.length; index++) {
			if(words[index].equals(keyWord))
				return index;
		}
		
		throw new Exception("Cannot find keyword: " + keyWord);
	}
	
	private static boolean isStopWord(String word) {
		for(String stopWord : stopWords) {
			if(stopWord.equalsIgnoreCase(word))
				return true;
		}
		
		return false;
	}
	
	private static double sigmoid(double z) {
		return 1/(1+Math.exp(z));
	}

}
