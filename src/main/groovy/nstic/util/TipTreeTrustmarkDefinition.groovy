package nstic.util

import nstic.web.tip.TIPReference

/**
 * Created by brad on 4/25/16.
 */
class TipTreeTrustmarkDefinition {

    private TipTreeTrustmarkDefinition(){}
    public TipTreeTrustmarkDefinition(String uniqueId, TIPReference tipReference){
        this.uniqueId = uniqueId;
        this.localId = tipReference.getReferenceName();
        this.databaseId = tipReference.getTrustmarkDefinition().getId().toString();
        this.uri = tipReference.getTrustmarkDefinition().getUri();
        this.ownerTipId = tipReference.getOwningTIPId().toString();
        this.name = tipReference.getTrustmarkDefinition().getName();
        this.version = tipReference.getTrustmarkDefinition().getTdVersion();
        this.description = tipReference.getTrustmarkDefinition().getDescription();
    }

    TipTreeNode parent;
    String databaseId;
    String uniqueId;
    String localId;
    String ownerTipId;
    String uri;
    String name;
    String description;
    String version;

    // If true, the trustmark definition is deselected from
    // the assessment intermediate page
    public boolean excludeFromAssessment = false;
}
