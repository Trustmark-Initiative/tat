package nstic.util;

import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.zip.*;

public class HtmlUtils {

    public  static String TARGET = 'target="_blank"'
    private static String SPACED_TARGET = ' target="_blank"'
    private static String HYPERLINK = '<a href='
    private static String CLOSING_BRACKET = '>'

    public static String inserTargetAttribToAnchorTag(String textWithAnchor) {
        // Do not process empty or null strings
        if (StringUtils.isEmpty(textWithAnchor)) {
            return textWithAnchor
        }

        // Do not process regular strings
        if (textWithAnchor.indexOf(HYPERLINK) == -1) {
            return textWithAnchor
        }

        // Do not process hyperlinks with target
        if (textWithAnchor.indexOf(TARGET) != -1) {
            return textWithAnchor
        }

        // position of closing hyperlink bracket
        Integer position = textWithAnchor.indexOf(CLOSING_BRACKET, textWithAnchor.indexOf(HYPERLINK))

        StringBuilder sb = new StringBuilder(textWithAnchor);
        sb.insert(position, SPACED_TARGET);
        return sb.toString();
    }


}
