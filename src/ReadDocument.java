import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class ReadDocument
{
	public static String dir = "Geoname/texts";//1731092.txt"; //total words: 8686
	private static Scanner scan;
	
	public ReadDocument(){}
	
	public static void readDocs() throws FileNotFoundException {
		File folder = new File(dir);
	    for (final File fileEntry : folder.listFiles()) {
	        //if (fileEntry.isDirectory()) {
	            //readDocs(fileEntry);
	        //} else {
					try {
						readDoc(fileEntry);
					} catch (UnsupportedEncodingException e) {
						Main.LOGGER.info("unsupported encoding!");
					}
	        //}
	    }
	}
	
	public static void readDoc(File file) throws FileNotFoundException, UnsupportedEncodingException{
		String filename = file.getName();
		Main.LOGGER.info("Reading Doc: " + filename);
		NewFile newFile = new NewFile(filename);
		
		scan = new Scanner(file);
		String word;
		String wordToWrite;
    	ArrayList<String> doc = getWords();
        
        //read by word from document
        for(int i = 0; i < doc.size(); i++) {
        	word = doc.get(i);
        	wordToWrite = word;
        	Location match = null;
        	int numOfWords = 0;
        	
        	//if(word != null)
        	//{
        		String firstTwo = word.substring(0,1);
        		try{
        			firstTwo += word.substring(1,2);
        		} catch(Exception e){} //In case a location is only 1 letter
        		ArrayList<Location> list = Main.map.get(firstTwo);
        		
        		try{
        			
        			boolean runAgain = false;
        			
        			for(int j = 0; j < list.size(); j++){
	        			Location loc = list.get(j);
	        			String location = loc.getlocation();
	        			String latlng = loc.getlatlng();
	        			
	        			if(location.contains(word))
	        			{
	        				//direct match. Result will change if a longer word is also a direct match
	        				if(word.compareTo(location) == 0){
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
        		
        		//print to file
    			if(match != null)//numOfWords != 0) // or if match is not null
    				newFile.annotate(match);
    			else
    				newFile.write(wordToWrite);
        	//}
        }
        newFile.close();
	}
	
	//get all words from document
	public static ArrayList<String> getWords()
	{
		String NEW_LINES = "((?<=\r\n)|(?=\r\n))|((?<=[\r\n])|(?=[\r\n]))"; //included as a "word"
		String SPACES = "((?<= )|(?= ))"; //include as "word"
		String PUNCTUATION = "((?<=\\p{P})|(?=\\p{P}))"; //include as "word"
		scan.useDelimiter(NEW_LINES + "|" + SPACES + "|" + PUNCTUATION);
		
		ArrayList<String> doc = new ArrayList<String>();
    	String word;
    	
        while(scan.hasNext()) {
        	word = scan.next();
        	//word = removePunctuation(word);
        	doc.add(word);
        }
        return doc;
	}
		
	//This function removes punctuation at the end of the word. Not used right now
	public static String removeEnd(String str)
	{
		int length = str.length();
		if(length == 0)
		{
			return null;
		}
		char lastChar = str.charAt(length-1);
    	if(!Character.isLetterOrDigit(lastChar))
    	{
    		str = str.substring(0,length-1);
    		str = removeEnd(str); //call it again, in case of ellipses etc.
    	}
		
		return str;
	}
	
	//This function removes all punctuation. Might need to modify this later, as some locations have hyphens etc.
	public static String removePunctuation(String str)
	{
		str = removeEnd(str);
		
		//remove possessives
		if(str.endsWith("'s"))
		{
			str = str.substring(0,str.length()-2);
		}
		
		//str = str.replaceAll("\\p{P}", "");
		return str;
	}
	
	public static void write(){
		
	}
}