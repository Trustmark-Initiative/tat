package nstic.td.io

import org.dom4j.Element

/**
 * Created by brad on 10/7/14.
 */
public interface Importer {

    /**
     * Returns a human-readable name which should uniquely identify this importer.
     */
    public String getName();

    /**
     * Returns an object if the instance of Importer can read the given data.  Returns null if not supported.
     * The result of this method MUST be passed back to the doImport() method below, so implementations should utilize
     * this to avoid processing the data multiple times.
     */
    public Object supports(File file, String contentType);
    public Object supports(Reader reader, String contentType);
    public Object supports(InputStream inputStream, String contentType);
    public Object supports(String data, String contentType);
    public Object supports(Element xmlElement);

    /**
     * Actually performs the import to the database using the given data object obtained from the supports() method.
     * Returns the main object imported by this importer.
     */
    public Object doImport( Object data ) throws ImportException;

}//end Importer