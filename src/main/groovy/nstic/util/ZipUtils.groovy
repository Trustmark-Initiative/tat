package nstic.util;

import groovy.util.AntBuilder;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.*;

/**
 * Created by brad on 3/10/16.
 */
public class ZipUtils {

    public static void zipFolder(final File folder, final File zipFile) throws IOException {
        AntBuilder builder = new AntBuilder();
        builder.zip( destFile: zipFile.canonicalPath, basedir: folder.canonicalPath, includes: "**/*", level: '9' )
    }


}
