package nstic.assessment

import com.googlecode.charts4j.Color

/**
 * Created by brad on 2/19/15.
 */
class ColorPalette {

    static Color DEFAULT_TEXT = Color.newColor("333333");
    static Color DEFAULT_BORDER = Color.newColor("dddddd");
    static Color SECTION_HEADER_COLOR = Color.newColor("f5f5f5");


    static Color PRIMARY_BUTTON = Color.newColor("337ab7");
    static Color SUCCESS_BUTTON = Color.newColor("5cb85c");
    static Color INFO_BUTTON = Color.newColor("5bc0de");
    static Color WARNING_BUTTON = Color.newColor("f0ad4e");
    static Color DANGER_BUTTON = Color.newColor("d9534f");


    /**
     * The Color that should be used for success messages.
     */
    static Color SUCCESS_BACKGROUND = Color.newColor("dff0d8");
    static Color SUCCESS_BORDER = Color.newColor("d6e9c6");
    static Color SUCCESS_TEXT = Color.newColor("3c763d");

    /**
     * The Color that should be used for warnings.
     */
    static Color WARNING_BACKGROUND = Color.newColor("fcf8e3");
    static Color WARNING_BORDER = Color.newColor("faebcc");
    static Color WARNING_TEXT = Color.newColor("8a6d3b");


    /**
     * The Color that should be used for information messages.
     */
    static Color INFORMATION_BACKGROUND = Color.newColor("d9edf7");
    static Color INFORMATION_BORDER = Color.newColor("bce8f1");
    static Color INFORMATION_TEXT = Color.newColor("31708f");

    /**
     * The Color that should be used for errors.
     */
    static Color ERROR_BACKGROUND = Color.newColor("f2dede");
    static Color ERROR_BORDER = Color.newColor("ebccd1");
    static Color ERROR_TEXT = Color.newColor("a94442");

    /*
     * Colors for specific terminology in the application.
     */
    static Color STEP_RESULT_SATISFIED = SUCCESS_TEXT;
    static Color STEP_RESULT_NOT_SATISFIED = ERROR_TEXT;
    static Color STEP_RESULT_NA = DEFAULT_BORDER;
    static Color STEP_RESULT_UNKNOWN = ERROR_BORDER;


    static Color ASSESSMENT_STATUS_ABORTED = WARNING_TEXT;
    static Color ASSESSMENT_STATUS_CREATED = DEFAULT_BORDER;
    static Color ASSESSMENT_STATUS_FAILED = ERROR_TEXT;
    static Color ASSESSMENT_STATUS_IN_PROGRESS = DEFAULT_BORDER;
    static Color ASSESSMENT_STATUS_PENDING_ASSESSED = WARNING_TEXT;
    static Color ASSESSMENT_STATUS_PENDING_ASSESSOR = ASSESSMENT_STATUS_PENDING_ASSESSED;
    static Color ASSESSMENT_STATUS_SUCCESS = SUCCESS_TEXT;
    static Color ASSESSMENT_STATUS_WAITING = DEFAULT_BORDER;





}
