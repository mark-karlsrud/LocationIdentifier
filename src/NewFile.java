import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class NewFile{
	private PrintWriter writer;
	private String dir = "annotated/";
	
	public NewFile(String name) throws FileNotFoundException, UnsupportedEncodingException{
		writer = new PrintWriter(dir + name, "UTF-8");
	}
	
	public void write(String word){
		writer.print(word);
	}
	
	public void annotateLocation(Location match){
		writer.print("<LOC><FEAT>" + match.getlatlng() + "</FEAT>" + match.getlocation() + "</LOC>");
	}
	
	public void annotateCountry(Location match){
		writer.print("<CNTRY><FEAT>" + match.getlatlng() + "</FEAT>" + match.getlocation() + "</CNTRY>");
	}
	
	public void close(){
		writer.close();
	}
}