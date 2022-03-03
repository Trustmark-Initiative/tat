package nstic.web

import nstic.util.HtmlUtils

//import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification


class HtmlUtilsSpec extends Specification /*implements DomainUnitTest<Registry>*/ {
    public static final String ANCHOR_LESS_STRING = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc id " +
            "urna sed orci aliquam condimentum non vitae lorem. Quisque rutrum viverra urna malesuada viverra. Vivamus " +
            "eget justo congue, accumsan dui id, semper tellus. Sed risus sapien, vehicula ac turpis vel, tincidunt " +
            "euismod mauris. Etiam eu placerat mauris, sit amet cursus est. Mauris ligula odio, mattis eu tortor ut, " +
            "aliquet ullamcorper nisl. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos " +
            "himenaeos. Quisque vel tempor nunc. Fusce vel ligula eget nisi blandit aliquam id sit amet augue. Curabitur " +
            "nibh lectus, sollicitudin eu neque vitae, vestibulum interdum est. Aliquam vel quam ultricies, semper erat non, " +
            "porta arcu. Aliquam sodales porttitor nunc, sit amet aliquet lacus pellentesque ac. Interdum et malesuada " +
            "fames ac ante ipsum primis in faucibus. Etiam scelerisque tristique arcu, eu ultrices odio faucibus ac. Sed et urna mi. " +
            "Duis sodales, diam a vestibulum rhoncus, mi tellus facilisis arcu, porttitor elementum diam lacus a nunc. Maecenas " +
            "convallis gravida sapien, ut tristique nisi hendrerit ullamcorper. Aenean enim felis, gravida a neque id, lacinia " +
            "tincidunt elit. Donec a ornare lacus. In fringilla neque et fermentum cursus. Mauris sollicitudin purus sed ligula " +
            "egestas volutpat. Ut tempus, arcu sit amet porttitor malesuada, erat leo suscipit ligula, a fringilla est massa " +
            "vel lacus. Suspendisse iaculis quam a libero dapibus laoreet. In tempus neque at nulla pulvinar lacinia. " +
            "Etiam vitae purus nisi."
    public static final String EMPTY_STRING = ""
    public static final String ANCHORED_STRING_WITH_HREF_AND_TARGET_ATTRIB = 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc'  +
            'id urna sed orci aliquam condimentum non vitae lorem. Quisque rutrum viverra urna malesuada viverra. Vivamus'  +
            'eget justo congue, accumsan dui id, semper tellus (<a href="https://www.wikipedia.org" target="_blank">).'
    public static final String ANCHORED_STRING_WITH_HREF_AND_NO_TARGET_ATTRIB = 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc'  +
            'id urna sed orci aliquam condimentum non vitae lorem. Quisque rutrum viverra urna malesuada viverra. Vivamus'  +
            'eget justo congue, accumsan dui id, semper tellus (<a href="https://www.wikipedia.org">).'

    def setup() {
    }

    def cleanup() {
    }

    void "test insert empty string"() {

        String result = HtmlUtils.inserTargetAttribToAnchorTag(EMPTY_STRING)

        expect:"fixed"
            result.equals(EMPTY_STRING) == true
    }

    void "test insert into regular anchor-less string"() {

        String result = HtmlUtils.inserTargetAttribToAnchorTag(ANCHOR_LESS_STRING)

        expect:"fixed"
            result.equals(ANCHOR_LESS_STRING) == true
    }

    void "test insert into regular anchored string with href and target attribs"() {

        String result = HtmlUtils.inserTargetAttribToAnchorTag(ANCHORED_STRING_WITH_HREF_AND_TARGET_ATTRIB)

        expect:"fixed"
            result.equals(ANCHORED_STRING_WITH_HREF_AND_TARGET_ATTRIB) == true
    }

    void "test insert into regular anchored string with href and no target attribs"() {

        String result = HtmlUtils.inserTargetAttribToAnchorTag(ANCHORED_STRING_WITH_HREF_AND_NO_TARGET_ATTRIB)

        Integer targetIndex = result.indexOf(HtmlUtils.TARGET)
        expect:"fixed"
            targetIndex != -1
    }
}
