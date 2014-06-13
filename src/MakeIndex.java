import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class MakeIndex
{
	String pathToIndex = "";
    IndexWriter indexWriter = null;
    
    private MakeIndex(){}
    
    public MakeIndex(String pathToIndex) {
        this.pathToIndex = pathToIndex;
    }
    
    public boolean openIndex()
    {
        try
        {
            Directory dir = FSDirectory.open(new File(pathToIndex));
            @SuppressWarnings("deprecation")
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
            @SuppressWarnings("deprecation")
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_CURRENT,analyzer);
            iwc.setOpenMode(OpenMode.CREATE);
            indexWriter = new IndexWriter(dir,iwc);
            return true;
        }
        catch (Exception e) 
        {
        	System.out.println("error");
        	return false;
        }
    }
    
    public void addLocation(Location loc)
    {
        Document doc = new Document();
        
        doc.add((IndexableField) new TextField("location",loc.getlocation(),Field.Store.YES));
        doc.add(new StoredField("latlng",loc.getlatlng()));
        try {
            indexWriter.addDocument(doc);
        } catch (IOException ex) {
            System.out.println("Threw an exception trying to add the doc: " + ex.getClass() + " :: " + ex.getMessage());
        }
    }
    
    public void finish()
    {
        try 
        {
            indexWriter.commit();
            indexWriter.close();
        }
        catch (IOException ex)
        {
        	System.out.println("error commiting");
    	}
    }
}