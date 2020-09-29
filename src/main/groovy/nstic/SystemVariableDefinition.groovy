package nstic

/**
 * Created by brad on 10/17/14.
 */
public enum SystemVariableDefinition {
    //==================================================================================================================
    //  Variable Definitions
    //==================================================================================================================
    /**
     * Where files are held on the filesystem.
     */
    FILES_DIRECTORY("assessment.tool.filesdir");
    //==================================================================================================================
    //  Common Functionality
    //==================================================================================================================
    private String propertyName = null;
    private SystemVariableDefinition(String propertyName) {
        this.propertyName = propertyName;
    }
    public String getPropertyName(){
        return propertyName;
    }

}