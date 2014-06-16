import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Main
{
	private static MakeIndex luceneWriter = null;
	private static String indexDir = "index";
	private static int SEARCH_LIMIT = 100;
	public final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	public static HashMap<String, ArrayList<Location>> map;
	private static boolean TEST = true;
	
	public static void main(String[] args)
	{
		if(!TEST){
			try{
				String dictionaryDir = args[0];
				String docDir = args[1];
				Database.dir = dictionaryDir;
				ReadDocument.dir = docDir;			
			}catch(IndexOutOfBoundsException e){
				System.out.println("Usage: java Main <dictionary directory (file)> <directory with documents (folder)>");
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
		map = new HashMap<String, ArrayList<Location>>();
		try {
			Database.getEntries(false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(print)
			printHash();
	}
	
	public static void makeIndex()
	{
		luceneWriter = new MakeIndex(indexDir);
		luceneWriter.openIndex();
		try {
			Database.getEntries(true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		luceneWriter.finish();
	}
	
	public static boolean search(String str)
	{
		IndexReader reader = null;
		boolean match = false;
		str = str.toLowerCase();
		
		try
		{
			reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			
			QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "location", analyzer);
			Query query = null;
			try{
				query = parser.parse(str);
			} catch(ParseException e){
				LOGGER.info("Cannot parse:" + str);
				return false;
			}
//			TopDocs hits = searcher.search(query,null,100);
//			System.out.println(hits);
			ScoreDoc[] hits = searcher.search(query,SEARCH_LIMIT).scoreDocs;
			
			if(hits.length > 0)
			{
				for(int i=0;i<hits.length;i++)
				{
					Document doc = searcher.doc(hits[i].doc);
					String location = doc.get("location");
					String[] tokens = location.split("\\s");
					String firstWord = tokens[0];
					if((firstWord.toLowerCase()).compareTo(str) == 0)
					{
						match = true;
						/*
						System.out.println(location
								//+ "\t" + doc.get("latlng")
								+ "\t" + hits[i].score);
						*/
					}
				}
			}
			else
			{
				//System.out.println("Match not found.");
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return match;
	}
	
	//Method 1: index the data using Apache Lucene
	public static void index(String location, String latlng)
	{
		Location loc = new Location(location,latlng);
		luceneWriter.addLocation(loc);
	}
	
	//Method 2: hash the data using first two letters
	public static void hash(String location, String latlng)
	{
		Location loc = new Location(location, latlng);
		String firstTwo = location.substring(0,1);
		try{
			firstTwo += location.substring(1,2);
		} catch(Exception e){} //In case a location is only 1 letter
		
		ArrayList<Location> list = null;
		list = map.get(firstTwo);
		if(list == null)
			list = new ArrayList<Location>();
		list.add(loc);
		map.put(firstTwo, list);
	}
	
	public static void printHash() //biggest list size = 156,744
	{
		int maxListSize = 0;
		ArrayList<Location> biggestList = null;
		Set<?> set = map.entrySet();
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