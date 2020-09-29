package nstic.util

import nstic.web.td.TrustmarkDefinition
import nstic.web.tip.TIPReference
import nstic.web.tip.TrustInteroperabilityProfile

/**
 * Provides information about TIP Trees, for display or whatever purpose.
 * <br/><br/>
 * Created by brad on 4/25/16.
 */
class TipTreeNode {
    private TipTreeNode(){}

    TipTreeNode(String id, TrustInteroperabilityProfile tip){
        this.uniqueId = id
        this.uri = tip.getUri()
        this.name = tip.getName()
        this.version = tip.getTipVersion()
        this.trustExpression = tip.getTipExpression()
    }

    TipTreeNode parent
    String localId // The tipReference used by parent trust expression
    String uniqueId
    String uri
    String name
    String description
    String version
    Integer sequence
    String trustExpression
    List<TipTreeNode> children = []
    List<TipTreeTrustmarkDefinition> trustmarkDefinitionReferences = []


    List<TipTreeNode> getChildren() {
        if( children == null )
            children = []
        return children
    }

    List<TipTreeTrustmarkDefinition> getTrustmarkDefinitionReferences() {
        if( trustmarkDefinitionReferences == null )
            trustmarkDefinitionReferences = []
        return trustmarkDefinitionReferences
    }

    void addTrustmarkDefinitionReference(TipTreeTrustmarkDefinition td){
        this.getTrustmarkDefinitionReferences().add(td);
    }
    void addChild(TipTreeNode child){
        this.getChildren().add(child);
    }

    static TipTreeNode getTreeRecursively(TrustInteroperabilityProfile tip, TipTreeNode parent = null) {
        String thisUUID = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        TipTreeNode node = new TipTreeNode(thisUUID, tip);
        node.parent = parent;
        for( TIPReference tipReference : tip.getReferences() ){
            if( tipReference.getTrustmarkDefinition() != null ){
                String tdUUID = UUID.randomUUID().toString().toUpperCase().replace("-", "");
                TipTreeTrustmarkDefinition tdRef = new TipTreeTrustmarkDefinition(tdUUID, tipReference);
                tdRef.parent = node;
                node.addTrustmarkDefinitionReference(tdRef);
            }else if( tipReference.getTrustInteroperabilityProfile() != null ){
                TipTreeNode child = getTreeRecursively(tipReference.getTrustInteroperabilityProfile(), node);
                child.setSequence(tipReference.getNumber())
                child.localId = tipReference.getReferenceName();
                node.addChild(child);
            }
        }
        return node
    }

    static TipTreeNode getTreeNonRecursively(TrustInteroperabilityProfile tip) {
        TipTreeNode parentNode = new TipTreeNode(UUID.randomUUID().toString().toUpperCase().replace("-", "")
                , tip)
        TipTreeNode node = parentNode
        TrustInteroperabilityProfile t = tip
        List<TipTreeNode> nodes = new ArrayList<>()
        List<TrustInteroperabilityProfile> tips = new ArrayList<>()
        int i = 0, j = 0
        while (node != null)  {
            List<TIPReference> refs = t.getReferences().sort {r1, r2 -> return r1.number.compareTo(r2.number); }
            refs.forEach {
                r ->
                if(r.getTrustmarkDefinition() != null)  {
                    TipTreeTrustmarkDefinition tdRef = new TipTreeTrustmarkDefinition(UUID.randomUUID().toString().toUpperCase().replace("-", "")
                            , r)
                    tdRef.parent = node
                    node.addTrustmarkDefinitionReference(tdRef)
                } else  if( r.getTrustInteroperabilityProfile() != null)  {
                    TrustInteroperabilityProfile p = r.getTrustInteroperabilityProfile()
                    TipTreeNode child = new TipTreeNode(UUID.randomUUID().toString().toUpperCase().replace("-", "")
                            , p)
                    child.parent = node
                    child.sequence = r.getNumber()
                    child.localId = r.getReferenceName()
                    node.addChild(child)
                    nodes.add(child)
                    tips.add(p)
                }
            }
            if(i < nodes.size())  {
                node = nodes.get(i++)
                t = tips.get(j++)
            }
            else
                node = null

        }
        return parentNode
    }

}
