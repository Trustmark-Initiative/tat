package nstic.util

import nstic.web.assessment.Trustmark
import nstic.web.assessment.TrustmarkStatus
import nstic.web.td.TrustmarkDefinition

class DifferentialAssessmentProcessor {

    static void process(TipTreeNode treeData, List<Trustmark> trustmarks) {
        Set<String> tmSet = new HashSet<String>();
        trustmarks.forEach { tm ->
            // only process active trusmarks
            if (tm.status == TrustmarkStatus.OK || tm.status == TrustmarkStatus.ACTIVE) {
                TrustmarkDefinition td = tm.trustmarkDefinition;
                tmSet.add(td.uri);
            }
        }

        // recursively process TIPs
        processData(treeData, tmSet);
    }

    static void processData(TipTreeNode treeData, Set<String> tmSet) {
        if (treeData.trustmarkDefinitionReferences.size() > 0) {
            treeData.trustmarkDefinitionReferences.forEach { td ->
                if (tmSet.contains(td.uri)) {
                    td.excludeFromAssessment = true;
                }
            }
        }

        if (treeData.children.size() > 0) {
            treeData.children.forEach {treeNode ->
                processData(treeNode, tmSet);
            }
        }
    }
}
