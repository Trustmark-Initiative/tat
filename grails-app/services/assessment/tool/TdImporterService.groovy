package assessment.tool

import grails.gorm.transactions.Transactional
import net.sf.jmimemagic.Magic
import net.sf.jmimemagic.MagicMatch
import nstic.td.io.Importer
import nstic.web.td.TrustmarkDefinition
import org.springframework.beans.factory.annotation.Autowired

/**
 * Performs a quick and dirty import of any *.zip file found in the given tdDirectory.  Does not
 * stop for errors, merely prints to the console.
 *
 * This is "quick and dirty" because it doesn't do any formal check or verification of the Td, just shoves what it can
 * into the database and ignores errors.  It also takes the liberty of randomly assigning TD images, if not given, as well
 * as other simple data which can be faked if not given.
 *
 * Created by slee35 on 5/1/14.
 */
@Transactional
class TdImporterService {
    //==================================================================================================================
    //  Services
    //==================================================================================================================
    @Autowired
    List<Importer> importers;
    @Autowired
    FileService fileService;
    @Autowired
    ContentTypeService contentTypeService;
    //==================================================================================================================
    //  Service Methods
    //==================================================================================================================
    /**
     * Imports a Trustmark Definition from its XML File.
     */
    public TrustmarkDefinition doTdImport( File theTdFile ){
        if( importers ){
            String contentType = contentTypeService.getContentType(theTdFile);
            for( Object obj : importers ){
                log.debug("Object in importers list: ${obj.class.name}")
                Importer importer = (Importer) obj;
                Object tempObj = importer.supports(theTdFile, contentType);
                if( tempObj ){
                    log.info("Importing file[${theTdFile.canonicalPath}] with importer: ${importer.name}")
                    TrustmarkDefinition td = importer.doImport(tempObj);
                    // TODO Print info imported.
                    return td;
                }else{
                    log.debug("Importer[${importer.getName()}] does not support content type: ${contentType}")
                }
            }
        }else{
            log.warn("Could not find any importers.")
        }
        return null;
    }//end doImport()

//    /**
//     * A recursive method for collecting TD zip files from a directory.
//     */
//    private void collectTrustmarkDefinitionZipFiles(File directory, List<File> tds){
//        tds.addAll(directory.listFiles({file -> return file.getName()?.toLowerCase().endsWith(".zip"); } as FileFilter));
//        def subdirs = directory.listFiles({file -> return file.isDirectory(); } as FileFilter)
//        subdirs.each{ subdir ->
//            collectTrustmarkDefinitionZipFiles(subdir, tds);
//        }
//    }//end collectTrustmarkDefinitionZipFiles()

}//end QuickAndDirtyImporter
