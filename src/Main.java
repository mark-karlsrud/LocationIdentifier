import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
//import org.apache.lucene.search.TopDocs;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;
import org.ardverk.collection.Trie;

public class Main
{
	public final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	public static HashMap<String, ArrayList<Location>> locations_map;
	public static HashMap<String, ArrayList<Location>> country_map;
	public static HashMap<String, Trie<String, String>> locations_map_trie;
	public static HashMap<String, Trie<String, String>> country_map_trie;
	private static boolean TEST = true;
	public static boolean PATRICIA = true;
	
	public static void main(String[] args)
	{
		if(!TEST){
			try{
				String locations_dir = args[0];
				String country_dir = args[1];
				String doc_dir = args[2];
				Database.locations_dir = locations_dir;
				Database.country_dir = country_dir;
				ReadDocument.dir = doc_dir;			
			}catch(IndexOutOfBoundsException e){
				System.out.println("Usage: java Main <location dictionary directory (file)> <country dictionary directory (file)> <directory with documents (folder)>");
				return;
			}
		}

		LOGGER.info("Begin");
		
		boolean print = false;
		makeHash(print);
		//makeIndex();
		readDocument();
		
		LOGGER.info("End");
	}
	
	public static void readDocument()
	{
		try {
			ReadDocument.readDocs();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void makeHash(boolean print)
	{
		if(PATRICIA)
		{
			locations_map_trie = new HashMap<String, Trie<String, String>>();
			country_map_trie = new HashMap<String, Trie<String, String>>();
		}
		else
		{
			locations_map = new HashMap<String, ArrayList<Location>>();
			country_map = new HashMap<String, ArrayList<Location>>();
		}
		try {
			Database.getLocations();
			//Database.getCountries();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(print)
			printHash();
	}
	
	//hash the data using first two letters
	public static void hash_locations(String location, String latlng)
	{
		Location loc = new Location(location, latlng);
		String firstTwo = "";
		try{
			firstTwo = location.substring(0,1);
		}catch(StringIndexOutOfBoundsException e){
			System.out.println("empty location");
			return;
		}
		try{
			firstTwo += location.substring(1,2);
		} catch(Exception e){} //In case a location is only 1 letter
		if(!PATRICIA){
			ArrayList<Location> list = null;
			list = locations_map.get(firstTwo);
			if(list == null)
				list = new ArrayList<Location>();
			list.add(loc);
			locations_map.put(firstTwo, list);
		}
		else{
			Trie<String, String> trie = null;
			trie = locations_map_trie.get(firstTwo);
			if(trie == null)
				trie = new PatriciaTrie<String, String>(StringKeyAnalyzer.INSTANCE);
			trie.put(location, latlng);
			locations_map_trie.put(firstTwo, trie);
		}
	}
	
	public static void hash_countries(String location, String latlng)
	{
		Location loc = new Location(location, latlng);
		String firstTwo = location.substring(0,1);
		try{
			firstTwo += location.substring(1,2);
		} catch(Exception e){} //In case a location is only 1 letter
		
		if(!PATRICIA){
			ArrayList<Location> list = null;
			list = country_map.get(firstTwo);
			if(list == null)
				list = new ArrayList<Location>();
			list.add(loc);
			country_map.put(firstTwo, list);
		}
		else{
			Trie<String, String> trie = null;
			trie = country_map_trie.get(firstTwo);
			if(trie == null)
				trie = new PatriciaTrie<String, String>(StringKeyAnalyzer.INSTANCE);
			trie.put(location, latlng);
			country_map_trie.put(firstTwo, trie);
		}
	}
	
	public static void printHash() //biggest list size = 156,744
	{
		int maxListSize = 0;
		ArrayList<Location> biggestList = null;
		Set<?> set = locations_map.entrySet();
		// Get an iterator
		Iterator<?> it = set.iterator();
		while(it.hasNext()) {
			int count = 0;
			Entry me  = (Entry)it.next();
			String key = (String)me.getKey();
			ArrayList<Location> list = (ArrayList<Location>)me.getValue();
			System.out.print(key + ": ");
			for(int i = 0; i < list.size(); i++){
				Location loc = list.get(i);
				String location = loc.getlocation();
				String latlng = loc.getlatlng();
				System.out.print(location + ",");
				count++;
			}
			if(count > maxListSize){
				maxListSize = count;
				biggestList = list;
			}
			System.out.println();
		}
		/*
		for(int i = 0; i < biggestList.size(); i++){
			String location = biggestList.get(i);
			System.out.print(location + ",");
		}*/
		//System.out.println("MaxListSize:" + maxListSize);
	}
}