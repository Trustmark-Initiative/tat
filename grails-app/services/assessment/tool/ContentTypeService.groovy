package assessment.tool

import net.sf.jmimemagic.Magic
import net.sf.jmimemagic.MagicMatch

/**
 * Created by brad on 10/7/14.
 */
class ContentTypeService {

    String getContentType( File file ){
        String contentType = null;
        MagicMatch match = Magic.getMagicMatch(file, true, true);
        if( match ){
            contentType = match.getMimeType();
        }else{
            log.warn("Could not determine file type from file: ${file.canonicalPath}")
            contentType = "application/octet-stream";
        }

        String extension = getExtension(file.name);
        if( EXTENSION_MAPPINGS.containsKey(extension) ){
            String myMime = EXTENSION_MAPPINGS.get(extension);
            if( !contentType || contentType != myMime ) {
                log.warn("Overwriting mimeType[$contentType] with [$myMime] based on file extension: $extension")
                contentType = myMime
            }
        }


        log.debug("File[${file.canonicalPath}] contentType set to: $contentType")
        return contentType;
    }//end getContentType()


    public String getExtension(String filename){
        int lastIndexOfPeriod = filename.lastIndexOf('.');
        String extension = filename.substring(lastIndexOfPeriod + 1);
        return extension;
    }
    
    
    public static final Map<String, String> EXTENSION_MAPPINGS = [
            html: 'text/html',
            json: 'application/json',
            xml: 'application/xml',
            xhtml: 'text/xhtml'
    ]
    

}
