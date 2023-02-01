import nstic.td.io.TrustmarkDefinitionV1_0Importer
import nstic.util.converters.*

// Place your Spring DSL code here
beans = {

    tdV1Importer(TrustmarkDefinitionV1_0Importer.class)

    // Converters
    assessmentConverter(AssessmentConverter.class)
    assessmentStepDataConverter(AssessmentStepDataConverter.class)
    artifactDataConverter(ArtifactDataConverter.class)

}