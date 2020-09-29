package nstic.web

import groovy.json.JsonOutput
import nstic.SystemVariableDefinition

import java.text.SimpleDateFormat

/**
 * The domain object responsible for actually tracking the raw byte content in the database.  Separate from the Binary
 * Object for reasons of speed.
 */
class BinaryData {
    /**
     * Contains a Path component that can be used to define where files for the service start.  All binary data objects
     * will NOT contain this path, but it must be added for correct resolution.
     */
    public static final String IDENTIFYING_PATH_COMPONENT = "__ASSESSMENT_FILES__";

    static searchable = false

    static String FILE_TS_PATTERN = "yyyy-MM-dd_HHmmss"

    static transients = ['content']

    static belongsTo = [
        binaryObject: BinaryObject
    ]

    /**
     * =How this works=
     *
     * Either the data will be in the database directly with the "chunks" array, or it will be on the file system
     * in a path relative to a user configured file; it can be both, but should both should never be empty.  If they are both
     * given, then their content must be the same and you can use either.
     *
     * _Make sure when using this class to use the abstractions of getContent() and setContent() to access/set the raw binary data_
     */
    String filePath     // A path relative to some configured directory on the server.

    /**
     * Number of BinaryDataChunk objects that make up this binary data object.
     */
    Integer chunkCount = -1

    static constraints = {
        binaryObject(nullable: false);
        filePath(nullable: true, blank: true, maxSize: 65535)
        chunkCount(nullable: false)
    }

    static mapping = {
        table(name:'binary_data')
        binaryObject(column: 'binary_object_ref')
        filePath(column: 'file_system_path', type: 'text')
        chunkCount(column: 'chunk_count')
    }

    //==================================================================================================================
    //  Public Data Access Methods
    //==================================================================================================================
    /**
     * Sets the content using the given {@link java.io.File}.
     */
    public String setContent(File fileData){
        return setContent(new FileInputStream(fileData));
    }//end setContent()
    /**
     * Sets the content using the given byte array.
     */
    public String setContent(byte[] data){
        return setContent(new ByteArrayInputStream(data));
    }//end setContent
    /**
     * Sets the content using the given input stream.
     */
    public String setContent(InputStream dataStream){
        if( this.getBinaryObject() == null )
            throw new UnsupportedOperationException("Cannot set binary data, since binary object is null.  Set setBinaryObject() first, before calling setContent()")

        log.debug("Copying binary data[${this.binaryObject.id} : ${this.binaryObject.originalFilename}] to chunks & file...");
        File outputFile = resolveOutputFile(this.binaryObject.originalFilename, this.binaryObject.getOriginalExtension());
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile, false);

        byte[] buffer = new byte[BinaryDataChunk.MAX_CHUNK_SIZE];
        int chunkNumber = 0;
        int read = -1;
        while( (read=dataStream.read(buffer)) > 0 ){
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            bytesOut.write(buffer, 0, read);

            BinaryDataChunk.withTransaction {
                BinaryDataChunk chunk = new BinaryDataChunk()
                chunk.byteData = bytesOut.toByteArray();
                chunk.binaryData = this;
                chunk.sequenceNumber = chunkNumber;
                chunk.save(failOnError: true, flush: true);
            }

            fileOutputStream.write(bytesOut.toByteArray());
            fileOutputStream.flush();

            chunkNumber++;
        }
        fileOutputStream.close();

        // @See TI-1653
        String canonicalPath = outputFile.canonicalPath;
        int identifierStartIndex = canonicalPath.indexOf(IDENTIFYING_PATH_COMPONENT);
        String filePath = canonicalPath.substring(identifierStartIndex+IDENTIFYING_PATH_COMPONENT.length());
        log.info("Calculated BinaryData's FilePath to be [${filePath}], from CanonicalPath[${canonicalPath}]");
        this.setFilePath(filePath);

        this.setChunkCount(chunkNumber);
        this.save(failOnError: true, flush: true); // Assumes a transaction on the calling context, which might not be true?

        log.debug("Writing recovery metadata file...");
        File metadataFile = new File(outputFile.parentFile, outputFile.name + ".meta_json");
        def output = [fileMetadata:
            [
                    originalFilename: this.binaryObject.originalFilename,
                    size: this.binaryObject.fileSize,
                    createdBy: this.binaryObject.createdBy,
                    extension: this.binaryObject.originalExtension,
                    resolvedMimeType: this.binaryObject.mimeType,
                    timestamp: System.currentTimeMillis(),
                    digest : [
                            value : this.binaryObject.md5sum,
                            lang: 'md5/base64'
                    ]
            ]
        ]
        metadataFile << JsonOutput.toJson(output)

        log.info("Successfully wrote file[${outputFile.canonicalPath}] and ${chunkNumber} chunks for BinaryObject[${this.binaryObject.id} : ${this.binaryObject.originalFilename}]")
    }//end setContent()

    /**
     * Returns the file pointer to the file on the filesystem.
     */
    public File toFile(){
        if(this.getFilePath() != null){
            String actualPath = getBasedirPath() + File.separator + IDENTIFYING_PATH_COMPONENT + this.getFilePath();
            File file = new File(actualPath);
            if( !file.exists() ){
                log.warn("Could not find file: "+actualPath);
                throw new FileNotFoundException(actualPath);
            }
            return file;
        }else if( this.chunkCount > 0 ){
            File tempFile = File.createTempFile();
            this.eachChunk { BinaryDataChunk chunk ->
                tempFile << chunk.byteData;
            }
        }
        throw new UnsupportedOperationException("Unknown binary data storage for [${this.binaryObject.id}:${this.binaryObject.originalFilename}].  Not a file, but no chunks either.");
    }//end toFile()

    /**
     * A simple method which executes a code block on each chunk in this binary object.
     */
    public void eachChunk( Closure userCode ){
        for( int i = 0; i < this.chunkCount; i++ ){
            BinaryDataChunk.withTransaction {
                BinaryDataChunk chunk = BinaryDataChunk.findByBinaryDataAndSequenceNumber(this, i);
                if( chunk != null ){
                    userCode.call(chunk);
                }else{
                    log.error("Could not find chunk #${i} on binary object ${this.binaryObject.id}:${this.binaryObject.originalFilename}")
                    throw new UnsupportedOperationException("Could not find chunk #${i} on binary object ${this.binaryObject.id}:${this.binaryObject.originalFilename}")
                }
            }
        }
    }//end eachChunk()

    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================
    private String trimToYear(String path){
       int year = Calendar.getInstance().get(Calendar.YEAR);
       int index = path.indexOf("/"+year+"/");
       return path.substring(index);
    }

    private File resolveOutputFile(String filename, String extension) {
        String timestamp = getTimestampString();
        File basedir = new File(getBasedirPath());
        if( !basedir.exists() )
            throw new UnsupportedOperationException("No such base file directory: ${basedir.canonicalPath}")
        File tatFilesMarker = new File(basedir, IDENTIFYING_PATH_COMPONENT);
        File dateFolder = getSubFolder(tatFilesMarker, timestamp);
        File byName = new File(dateFolder, filename);
        if( byName.exists() ){
            int counter = 1;
            while( true ){
                byName = new File(dateFolder, filename + "." + counter);
                if( !byName.exists() ){
                    break;
                }else{
                    counter++;
                }
            }
        }
        return byName;
    }//end resolveFileDirectory()

    private String getTimestampString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FILE_TS_PATTERN);
        return simpleDateFormat.format(Calendar.getInstance().getTime());
    }//end getTimestampString()

    private String getBasedirPath() {
        return SystemVariable.findBySystemVariableDefinition(SystemVariableDefinition.FILES_DIRECTORY)?.getStringValue();
    }

    /**
     * Returns the directory under the static configured filepath where to place the current file.  Based on date, like
     * yyyy/MM/dd
     */
    private File getSubFolder(File basedir, String formattedTimestamp) {
        String year = formattedTimestamp.substring(0, 4);
        String month = formattedTimestamp.substring(5, 7);
        String dayOfMonth = formattedTimestamp.substring(8, 10);

        File yearFolder = checkDir( basedir.canonicalPath + File.separator + year);
        File monthFolder = checkDir( yearFolder.canonicalPath + File.separator + month);
        File dayFolder = checkDir( monthFolder.canonicalPath + File.separator + dayOfMonth);

        return dayFolder;
    }//end basedir()

    private File checkDir(String path){
        File dir = new File(path);
        if( !dir.exists() ){
            if( !dir.mkdirs() ){
                log.error("Unable to create directory: @|red $path|@")
                throw new RuntimeException("Cannot create directory: $path")
            }
        }
        return dir;
    }//end checkDir()


}//end BinaryData
