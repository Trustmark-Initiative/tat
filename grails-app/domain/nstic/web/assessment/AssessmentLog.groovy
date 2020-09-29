package nstic.web.assessment

/**
 * A collection of records ordered by timestamp, to track how an assessment progressed from start to finish.
 */
class AssessmentLog {

    static transients = ['sortedEntries', 'mostRecentEntry']

    static belongsTo = [
            assessment: Assessment
    ]

    // TODO any other information we care about tracking in a log?  Maybe we collapse this into the Assessment class?

    static hasMany = [
            entries: AssessmentLogEntry
    ]

    static constraints = {
        assessment(nullable: false)
        entries(nullable: true)
    }

    static mapping = {
        table(name: 'assessment_log')
        assessment(column: 'assessment_ref')
        entries(cascade: 'all-delete-orphan')
    }

    public void copyEntry( AssessmentLogEntry that ){
        this.addEntry(that.type, that.title, that.message, that.data);
    }

    public void addEntry(String type, String title, String message){
        this.addEntry(type, title, message, null, null)
    }
    public void addEntry(String type, String title, String message, String data){
        this.addEntry(type, title, message, data, null)
    }
    public void addEntry(String type, String title, String message, Map dataMap){
        this.addEntry(type, title, message, null, dataMap)
    }
    public void addEntry(String type, String title, String message, String data, Map dataMap){
        AssessmentLogEntry.withTransaction {
            AssessmentLogEntry entry = new AssessmentLogEntry(logg: this, type: type, title: title, message: message, data: data);
            entry.setData(dataMap);
            entry.save(failOnError: true);
            this.addToEntries(entry);
            this.save(failOnError: true);
        }
    }


    public List<AssessmentLogEntry> getSortedEntries() {
        def list = []
        list.addAll(this.entries)
        Collections.sort(list, {e1, e2 -> return e2.dateCreated.compareTo(e1.dateCreated)} as Comparator)
        return list;
    }//end getSortedEntries()


    public AssessmentLogEntry getMostRecentEntry() {
        AssessmentLogEntry mostRecent = null
        this.entries?.each{ entry ->
            if( !mostRecent || (entry.dateCreated.getTime() > mostRecent.dateCreated.getTime()) ){
                mostRecent = entry;
            }
        }
        return mostRecent;
    }//end getMostRecentEntry()

    /**
     * Returns the <b>most recent</b> log entry which has a type matching the given regex list.
     */
    public AssessmentLogEntry findLastEntryByTypeMatching( String ... typeRegexList ){
        AssessmentLogEntry matchingLogEntry = null;
        List<AssessmentLogEntry> sortedEntries = this.getSortedEntries()
        for( AssessmentLogEntry currentEntry : sortedEntries ){
            if( matchingLogEntry == null ){
                boolean matches = false;
                for( String regex : typeRegexList ){
                    if( currentEntry.type.matches(regex) ){
                        matches = true;
                        break;
                    }
                }
                if( matches ){
                    matchingLogEntry = currentEntry;
                    break;
                }
            }
        }
        return matchingLogEntry;
    }//end getLastEntry()

    public Map toJsonMap(boolean shallow = true) {
        def json = [
                id: this.id,
                assessment: [id: this.assessment.id]
        ]

        if( !shallow ) {
            def entriesJson = []
            def entries = this.getSortedEntries();
            entries.each { entry ->
                entriesJson.add(entry.toJsonMap(shallow));
            }
        }

        return json;
    }

}//end AssessmentLog
