import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class NewFile{
	private static PrintWriter writer;
	private String dir = "annotated/";
	
	public NewFile(String name) throws FileNotFoundException, UnsupportedEncodingException{
		writer = new PrintWriter(dir + name, "UTF-8");
	}
	
	public static void write(String word){
		writer.print(word);
	}
	
	public static void annotate(Location match){
		writer.print("<LOC><FEAT>" + match.getlatlng() + "</FEAT>" + match.getlocation() + "</LOC>");
	}
	
	public static void close(){
		writer.close();
	}
}