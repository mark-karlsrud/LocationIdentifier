import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Database
{
	public static String locations_dir = "Geoname/geoname.transducer.txt";//dico.txt";
	public static String country_dir = "Geoname/outputIso.txt";
		
	public static void getLocations() throws FileNotFoundException
	{
		BufferedReader br = new BufferedReader(new FileReader(locations_dir));
	    try {
	        String line;
			try {
				line = br.readLine();				
			} catch (IOException e1) {
				System.out.println("IOException");
				return;
			}
	        int count = 0;
	        
	        while (line != null) {
	        	count++;
	            
	            String[] tokens = line.split("\t");
	            String location = tokens[0];
	            String other = tokens[1];
	            String[] tokens2 = other.split("</FEAT>");
	            String geotag = tokens2[0].replace("<LOC>", "").replace("<FEAT>", "");
	            
	            Main.hash_locations(location, geotag);
	            if(count % 100000 == 0)
	            {
	            	System.out.println(Integer.toString(count/100000) + " hundred thousand");
	            }
	            try {
					line = br.readLine();
				} catch (IOException e) {
					Main.LOGGER.info("IOException on line " + count);
					return;
				}
	        }
	    } finally {
	        try {
				br.close();
			} catch (IOException e) {
				Main.LOGGER.info("cannot close file");
			}
	    }
	}
}