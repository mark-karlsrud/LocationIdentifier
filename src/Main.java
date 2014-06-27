import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
//import org.apache.lucene.search.TopDocs;

import javax.xml.crypto.NodeSetData;

//import org.ardverk.collection.PatriciaTrie;
//import org.ardverk.collection.StringKeyAnalyzer;
//import org.ardverk.collection.Trie;

public class Main
{
	public final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	public static HashMap<String, ArrayList<Location>> locations_map;
	public static HashMap<String, HashMap<String, Node>> locations_map_trie;
	private static boolean TEST = false;
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
		//printTrie();
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
			locations_map_trie = new HashMap<String, HashMap<String, Node>>();
		}
		else
		{
			locations_map = new HashMap<String, ArrayList<Location>>();
		}
		try {
			Database.getLocations();
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
			String[] tokens = location.split(ReadDocument.getDelimeters());
			HashMap<String, Node> map = null;
			map = locations_map_trie.get(firstTwo);
			if(map == null){
				map = new HashMap<String, Node>();
			}
			Node parent;
			String token = tokens[0];
			parent = map.get(token);
			
			if(parent == null){
				parent = makeNode(tokens, latlng);
			}
			else{
				try{
					//System.out.println("PARENT="+parent.getKey());
					parent = add(tokens, latlng, parent);
				}catch(IllegalArgumentException e){
					parent.setlatlng(latlng);
				}
			}
			if(!parent.getKey().equals(token)){
				LOGGER.info("WRONG");
			}
			
			map.put(token, parent);
				
			locations_map_trie.put(firstTwo, map);
		}
	}
	
	public static Node makeNode(String[] tokens, String latlng){
		String token = "";
		try{
			token = tokens[0];
		}catch(IndexOutOfBoundsException e){
			return null;
		}
		//System.out.println(token);
		tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
		//System.out.println("1");
		ArrayList<Node> nodes = makeNodeList(tokens, latlng);
		Node node = null;
		
		if(nodes.size() == 0)
		{
			node = new Node(token, latlng, nodes);
		}
		else
			node = new Node(token, null, nodes);
		
		return node;
	}
	
	public static ArrayList<Node> makeNodeList(String[] tokens, String latlng){
		//System.out.println("2");
		ArrayList<Node> nodes = new ArrayList<Node>();
		Node newNode = makeNode(tokens, latlng);
		if(newNode != null)
			nodes.add(newNode);
		
		return nodes;
	}
	
	public static Node add(String[] tokens, String latlng, Node parent){
		//System.out.println("3");
		String token = "";
		try{
			token = tokens[0];
		}catch(IndexOutOfBoundsException e){
			parent.setlatlng(latlng);
			return parent;
		}
		
		if(tokens.length == 1){
			parent.setlatlng(latlng);
			return parent;
		}
		
		ArrayList<Node> children = null;
		int size = 0;
		try{
			children = parent.getNodes();
			size = children.size();
			//System.out.println("size="+size);
			//System.out.println("parent="+parent.getKey());
			if(size == 0){
				tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
				children = new ArrayList<Node>();
				Node node = null;
				node = makeNode(tokens, latlng);
				if(node != null){
					children.add(node);
				}
				parent.setNodes(children);
				
				return parent;
			}
		}catch(NullPointerException e){
			children = new ArrayList<Node>();
			Node node = null;
			node = makeNode(tokens, latlng);
			if(node != null){
				children.add(node);
			}
			parent.setNodes(children);
			
			return parent;
		}
		
		tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
		token = tokens[0];
		
		int i = 0;
		for(i = 0; i < size; i++){
			Node child = children.get(i);
			//System.out.print("comparing " + token);
			//System.out.println(" with " + child.getKey());
			if(token.equals(child.getKey())){
				if(tokens.length == 0){
					//System.out.println("HEREHEREHERE");
					child.setlatlng(latlng); //this should never happen if it's in alphabetical order and there aren't duplicates
				}
				else{
					child = add(tokens, latlng, child);
				}
				//if(child != null)
				children.set(i, child);
				break;
			}
		}
		if(i == children.size()){ //must make new node
			Node node = null;
			node = makeNode(tokens, latlng);
			if(node != null){
				children.add(node);
			}
				
		}
		parent.setNodes(children);
		
		return parent;
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
	
	public static void printTrie(){
		System.out.println("printing tree");
		Set<?> set = locations_map_trie.entrySet();
		// Get an iterator
		Iterator<?> it = set.iterator();
		while(it.hasNext()) {
			Entry me  = (Entry) it.next();
			String key = (String) me.getKey();
			System.out.println("KEY=" + key);
			HashMap<String, Node> map = (HashMap<String, Node>) me.getValue();
			
			Set<?> set2 = map.entrySet();
			// Get an iterator
			Iterator<?> it2 = set2.iterator();
			while(it2.hasNext()) {
				Entry me2  = (Entry) it2.next();
				String key2 = (String) me2.getKey();
				Node node = (Node) me2.getValue();
				
				//System.out.println(key2 + ":" + node.getNodes().size());
				printNode("", node);
			}
			
		}
	}
	public static void printNode(String beginning, Node node){
		String key = node.getKey();
		ArrayList<Node> nodes = node.getNodes();
		System.out.println(beginning + key + ":" + nodes.size());
		if(node.getlatlng() != null)
			System.out.println(node.getlatlng());
		
		
		for(int i = 0; i < nodes.size(); i++){
			try{
			printNode(beginning + key, nodes.get(i));
			}catch(NullPointerException e){}
		}
		
	}
}