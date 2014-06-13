import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ReadDocument
{
	public static String dir = "Geoname/texts/test.txt";//1731092.txt"; //total words: 8686
	private static Scanner scan;
	
	public ReadDocument(){}
	
	public static void read() throws FileNotFoundException{
		Main.LOGGER.info("Reading Doc");
		
		scan = new Scanner(new File(dir));
		String word;
    	ArrayList<String> doc = getWords();
        
        //read by word from document
        for(int i = 0; i < doc.size(); i++) {
        	word = doc.get(i);
        	Location match = null;
        	
        	if(word != null && !word.isEmpty())
        	{
        		String firstTwo = word.substring(0,1);
        		try{
        			firstTwo += word.substring(1,2);
        		} catch(Exception e){} //In case a location is only 1 letter
        		ArrayList<Location> list = Main.map.get(firstTwo);
        		
        		try{
        			int numOfWords = 0;
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
	        					word += " " + nextWord;
	        				}catch(Exception e){
	        					break;//no more words!
	        				}	
	        			}
	        		}
        			
        			//print match
        			if(numOfWords != 0) // or if match is not null
        				System.out.println(match.getlocation() + "\t" + match.getlatlng());
        			
        		}catch(NullPointerException e){
        			//List not found. Pass
        		}
        	}
        }
	}
	
	//get all words from document
	public static ArrayList<String> getWords()
	{
		ArrayList<String> doc = new ArrayList<String>();
    	String word;
    	
        while(scan.hasNext()) {
        	word = scan.next();
        	word = removePunctuation(word);
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
		//remove possessives
		if(str.endsWith("'s"))
		{
			str = str.substring(0,str.length()-2);
		}
		str = str.replaceAll("\\p{P}", "");
		return str;
	}
}