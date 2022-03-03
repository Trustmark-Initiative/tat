package nstic.util;

import java.util.Properties;

public class DefaultMetadataUtils {
    public static String URLSAFE_IDENTIFIER = "@URLSAFE_IDENTIFIER@"

    public static String buildIdentifierPattern(Properties props) {
        StringBuilder url = new StringBuilder();
        String baseUrl = props.getProperty(AssessmentToolProperties.BASE_URL)
        url.append(baseUrl)
        String publicApi = props.getProperty(AssessmentToolProperties.PUBLIC_TRUSTMARK_API)
        url.append(publicApi)
        url.append("/" + URLSAFE_IDENTIFIER)

        return url.toString()
    }

    public static buildStatusUrlPattern(Properties props) {
        StringBuilder url = new StringBuilder()

        String baseUrl = props.getProperty(AssessmentToolProperties.BASE_URL)
        url.append(baseUrl)
        String publicApi = props.getProperty(AssessmentToolProperties.PUBLIC_TRUSTMARK_API)
        url.append(publicApi)
        url.append("/status/" + URLSAFE_IDENTIFIER)

        return url.toString()
    }
}
