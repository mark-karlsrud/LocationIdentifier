import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

//import org.ardverk.collection.PatriciaTrie;
//import org.ardverk.collection.Trie;

public class ReadDocument
{
	public static String dir = "Geoname/texts";
	private static Scanner scan;
	private static ArrayList<String> doc;
	
	public static void readDocs() throws FileNotFoundException {
		File folder = new File(dir);
	    for (final File fileEntry : folder.listFiles()) {
			try {
				readDoc(fileEntry);
			} catch (UnsupportedEncodingException e) {
				Main.LOGGER.info("unsupported encoding!");
			}
	    }
	}
	
	public static void readDoc(File file) throws FileNotFoundException, UnsupportedEncodingException{
		String filename = file.getName();
		Main.LOGGER.info("Reading Doc: " + filename);
		NewFile newFile = new NewFile(filename);
		
		scan = new Scanner(file);
		String word;
		String wordToWrite;
    	doc = getWords();
        
        //read by word from document
        for(int i = 0; i < doc.size(); i++) {
        	word = doc.get(i);
        	wordToWrite = word;
        	Location match = null;
        	int numOfWords = 0;

    		String firstTwo = word.substring(0,1);
    		try{
    			firstTwo += word.substring(1,2);
    		} catch(Exception e){} //In case a location is only 1 letter
    		
    		ArrayList<Location> list = null;
    		HashMap<String, Node> map = null;
    		if(!Main.PATRICIA)
    			list = Main.locations_map.get(firstTwo);
    		else
    			map = Main.locations_map_trie.get(firstTwo);
    		
    		if(!Main.PATRICIA){
	    		try{
	    			
	    			boolean runAgain = false;
	    			
	    			for(int j = 0; j < list.size(); j++){
	        			Location loc = list.get(j);
	        			String location = loc.getlocation();
	        			
	        			if(location.contains(word))
	        			{
	        				//direct match. Result will change if a longer word is also a direct match
	        				if(word.compareTo(location) == 0){
	        					String latlng = loc.getlatlng();
		        				match = new Location(location,latlng);
		        				i += numOfWords;
		        			}
	        				runAgain = true;
	        			}
	        			if(j == list.size()-1 && runAgain){
	        				j = -1;
	        				runAgain = false;
	        				numOfWords++;
	        				
	        				//Add next word from document to see if a multi-word location is in the dictionary
	        				try{
	        					String nextWord = doc.get(i + numOfWords);
	        					word += nextWord;
	        				}catch(Exception e){
	        					break;//no more words!
	        				}	
	        			}
	        		}
	    			
	    		}catch(NullPointerException e){
	    			//List not found. Pass
	    		}
    		}
    		else{
    			try{
    				Node parent = map.get(word);
	    			if(parent != null){
	    				if(parent.getlatlng() != null){
	    					match = new Location(word, parent.getlatlng());
	    				}
	    				ArrayList<Node> nodes = parent.getNodes();
	    				Location newLoc = dothing(word, word, nodes, i);
	    				if(newLoc != null)
	    					match = newLoc;
	    			}	    			
	    		}catch(NullPointerException e){
	    			//List not found. Pass
	    		}
    		}
    		
    		//print to file
			if(match != null)//numOfWords != 0) // or if match is not null
			{
				newFile.annotateLocation(match);
				if(Main.PATRICIA){
					String[] tokens = match.getlocation().split(ReadDocument.getDelimeters());
					i += tokens.length - 1;
				}
			}
			else
				newFile.write(wordToWrite);
        }
        newFile.close();
	}
	
	public static Location dothing(String wholeWord, String word, ArrayList<Node> nodes, int index){
		Location match = null;
		
		try{
			String nextWord = doc.get(index+1);
			word = nextWord;
		}catch(Exception e){
			return null;//no more words!
		}
		
		for(int j = 0; j < nodes.size(); j++){
			Node node = nodes.get(j);
			String key = node.getKey();
			//System.out.println("comparing " + key + " and " + word);
			if(key.equals(word)){
				wholeWord += word;
				
				if(node.getlatlng() != null){
					match = new Location(wholeWord, node.getlatlng());
					//System.out.println("got match");
				}
				//System.out.println(key);
				index++;
				try{
					String nextWord = doc.get(index);
					Location newLoc = dothing(wholeWord, nextWord, node.getNodes(),index);
					if(newLoc != null)
						match = newLoc;
				}catch(Exception e){
					break;//no more words!
				}
				break;
			}
		}
		return match;
	}
	
	public static String getDelimeters(){
		String NEW_LINES = "((?<=\r\n)|(?=\r\n))|((?<=[\r\n])|(?=[\r\n]))"; //included as a "word"
		String SPACES = "((?<= )|(?= ))"; //include as "word"
		String PUNCTUATION = "((?<=\\p{P})|(?=\\p{P}))"; //include as "word"
		return NEW_LINES + "|" + SPACES + "|" + PUNCTUATION;
	}
	
	//get all words from document
	public static ArrayList<String> getWords()
	{
		scan.useDelimiter(getDelimeters());
		
		ArrayList<String> doc = new ArrayList<String>();
    	String word;
    	
        while(scan.hasNext()) {
        	word = scan.next();
        	doc.add(word);
        }
        return doc;
	}
}