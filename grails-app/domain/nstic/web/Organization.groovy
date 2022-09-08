package nstic.web

import java.util.stream.Collectors

class Organization {

    static transients = ['sortedComments', 'sortedArtifacts', 'sortedTrustmarkRecipientIdentifiers']

    int id
    String uri                                      // The URN which uniquely identifies this organization
    String identifier                               // The short human-readable identifier for this org
    String name                                     // A distinct "human readable" name for this organization
    Boolean isTrustmarkProvider = Boolean.FALSE     // A flag that, if true, identifies this organization as
                                                    // a trustmark provider

    ContactInformation primaryContact;

    static hasMany = [
            administrators: User,
            assessors: User,
            contacts: ContactInformation,
            artifacts: OrganizationArtifact,
            comments: OrganizationComment,
            documents: Documents,
            certificates: SigningCertificate,
            trustmarkRecipientIdentifiers: TrustmarkRecipientIdentifier
    ]

    static constraints = {
        uri(nullable: false, blank: false, maxSize: 255)
        identifier(nullable: true, blank: true, maxSize: 50)
        name(nullable: false, blank: false, maxSize: 255)
        isTrustmarkProvider(nullable: false)
        primaryContact(nullable: true)
        contacts(nullable: true)
        artifacts(nullable: true)
        administrators(nullable: true)
        assessors(nullable: true)
        comments(nullable: true)
        documents(nullable: true)
        certificates(nullable: true)
        trustmarkRecipientIdentifiers(nullable: true)
        trustmarkRecipientIdentifiers cascade: "all-delete-orphan"
    }

    static mapping = {
        table(name:'organization')
        identifier(column: 'short_name')
        primaryContact(column: 'primary_contact_ref')
    }

    public static Organization newOrganization(String uri, String identifier, String name, Boolean isTrustmarkProvider) {
        Organization org = new Organization(uri: uri, identifier: identifier, name: name, isTrustmarkProvider: isTrustmarkProvider)
        org.addTrustmarkRecipientIdentifierUri(uri, true)

        return org
    }

    public String defaultTustmarkRecipientIdentifierUri() {
        return TrustmarkRecipientIdentifier.findByOrganizationAndDefaultTrustmarkRecipientIdentifier(this, true)
    }

    public void addTrustmarkRecipientIdentifierUri(String uri, boolean defaultTrustmarkRecipientIdentifier = false) {
        this.addToTrustmarkRecipientIdentifiers(
                new TrustmarkRecipientIdentifier(uri: uri, defaultTrustmarkRecipientIdentifier: defaultTrustmarkRecipientIdentifier))
    }

    public OrganizationArtifact findArtifact( String id ){
        OrganizationArtifact oa = null;
        if( this.artifacts && !this.artifacts.isEmpty() ){
            for( OrganizationArtifact current : this.artifacts ){
                if( current.getId().toString().equals(id) || current.getDisplayName().equalsIgnoreCase(id) ){
                    oa = current;
                    break;
                }
            }
        }
        return oa;
    }

    public List<OrganizationArtifact> getSortedArtifacts() {
        List<OrganizationArtifact> sorted = []
        if( this.artifacts && !this.artifacts.isEmpty() )
            sorted.addAll(this.artifacts)
        Collections.sort(sorted, {a1, a2 -> return a1.displayName?.compareToIgnoreCase(a2.displayName); } as Comparator);
        return sorted;
    }//end getSortedCOmments

    public List<OrganizationComment> getSortedComments() {
        List<OrganizationComment> sortedComments = []
        if( this.comments && !this.comments.isEmpty() )
            sortedComments.addAll(this.comments)
        Collections.sort(sortedComments, {c1, c2 -> return c1.dateCreated.compareTo(c2.dateCreated);} as Comparator);
        return sortedComments;
    }//end getSortedCOmments

    public List<TrustmarkRecipientIdentifier> getSortedTrustmarkRecipientIdentifiers() {
        List<TrustmarkRecipientIdentifier> sorted = []
        if( this.trustmarkRecipientIdentifiers && !this.trustmarkRecipientIdentifiers.isEmpty() ) {
            sorted.addAll(this.trustmarkRecipientIdentifiers)
            sorted = sorted.stream()
                    .sorted((p1, p2) -> Boolean.compare(p2.defaultTrustmarkRecipientIdentifier, p1.defaultTrustmarkRecipientIdentifier))
                    .collect(Collectors.toList())
        }

        return sorted;
    }

    public Map toJsonMap(boolean shallow = false) {
        def json = [
                id: this.id,
                shortName: this.identifier,
                name: this.name,
                uri: this.uri,
                isTrustmarkProvider: isTrustmarkProvider
        ]
        if( shallow ){
            json.put("primaryContactId", primaryContact?.id)
            if (contacts) {
                def jsonContacts = []
                contacts.each { contact ->
                    jsonContacts.add(contact.id);
                }
                json.put("contactIds", jsonContacts);
            }
        }else {
            if (primaryContact != null) {
                json.put("primaryContact", primaryContact?.toJsonMap())
            }
            if (contacts) {
                def jsonContacts = []
                contacts.each { contact ->
                    jsonContacts.add(contact.toJsonMap());
                }
                json.put("contacts", jsonContacts);
            }
        }
        return json;
    }


    public boolean equals(Object other){
        if( other && other instanceof Organization ){
            return ((Organization) other).id == this.id
        }
        return false;
    }
    public int hashCode(){
        return this.id
    }

}//end Organization
