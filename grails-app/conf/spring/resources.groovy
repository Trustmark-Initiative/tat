import nstic.td.io.TrustmarkDefinitionV1_0Importer
import nstic.util.AuthFailureSecurityListener
import nstic.util.AuthSuccessSecurityListener
import nstic.util.converters.*

// Place your Spring DSL code here
beans = {

    authFailureListener(AuthFailureSecurityListener.class)
    authSuccessListener(AuthSuccessSecurityListener.class)

    tdV1Importer(TrustmarkDefinitionV1_0Importer.class)

    // Converters
    assessmentConverter(AssessmentConverter.class)
    assessmentStepDataConverter(AssessmentStepDataConverter.class)
    artifactDataConverter(ArtifactDataConverter.class)


}