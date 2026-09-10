package com.dcai.semanticservice.promotion

import com.dcai.semanticservice.ingestion.SourceExtractRdfMapper
import com.dcai.semanticservice.ontology.Dcai
import com.dcai.semanticservice.runtime.SemanticServiceComposition
import com.dcai.semanticservice.testfixtures.ProductionSourceExtractFixtures
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS

class ProductionGraphValidationGateTest {
    private val repoRoot = SemanticServiceComposition.locateRepoRoot()
    private val gate = ProductionGraphValidationGate(repoRoot)

    @Test
    fun acceptsMappedSourceCanonicalAndProvenanceGraph() {
        val mapping = SourceExtractRdfMapper().map(ProductionSourceExtractFixtures.validBatch())

        val report = gate.validate(mapping.combinedValidationModel())

        assertTrue(report.conforms, report.errors.joinToString(separator = "\n"))
        assertTrue(report.tripleCount > 0)
    }

    @Test
    fun rejectsCanonicalPromotionWhenRequiredZoneFactIsMissing() {
        val mapping = SourceExtractRdfMapper().map(ProductionSourceExtractFixtures.invalidMissingZoneBatch())

        val report = gate.validate(mapping.combinedValidationModel())

        assertFalse(report.conforms)
        assertTrue(report.errors.any { it.contains("SHACL validation failed") })
    }

    @Test
    fun rejectsCanonicalPromotionWhenLiteralAndControlledStateDisagree() {
        val mapping = SourceExtractRdfMapper().map(ProductionSourceExtractFixtures.validBatch())
        val candidate = mapping.combinedValidationModel()
        val asset = ResourceFactory.createResource("urn:dcai:asset:ASSET-GPU-RACK-ROW-A")
        candidate.removeAll(asset, Dcai.hasOperationalStatus, null)
        candidate.add(asset, Dcai.hasOperationalStatus, "running")

        val report = gate.validate(candidate)

        assertFalse(report.conforms)
        assertTrue(report.errors.any { it.contains("SHACL validation failed") })
    }

    @Test
    fun rejectsCanonicalPromotionWhenStateBelongsToMultipleCategories() {
        val mapping = SourceExtractRdfMapper().map(ProductionSourceExtractFixtures.validBatch())
        val candidate = mapping.combinedValidationModel()
        val lifecycleState = ResourceFactory.createResource("urn:dcai:state:incident-lifecycle:in-progress")
        candidate.add(lifecycleState, RDF.type, Dcai.OperationalStatus)

        val report = gate.validate(candidate)

        assertFalse(report.conforms)
        assertTrue(report.errors.any { it.contains("SHACL validation failed") })
    }

    @Test
    fun rejectsCanonicalPromotionWhenLifecycleStateIsAlsoWorkflowStage() {
        val mapping = SourceExtractRdfMapper().map(ProductionSourceExtractFixtures.validBatch())
        val candidate = mapping.combinedValidationModel()
        val lifecycleState = ResourceFactory.createResource("urn:dcai:state:incident-lifecycle:in-progress")
        candidate.add(lifecycleState, RDF.type, Dcai.WorkflowStage)

        val report = gate.validate(candidate)

        assertFalse(report.conforms)
        assertTrue(report.errors.any { it.contains("SHACL validation failed") })
    }

    @Test
    fun rejectsCanonicalPromotionWhenControlledStateIsOutsideClosedVocabulary() {
        val mapping = SourceExtractRdfMapper().map(ProductionSourceExtractFixtures.validBatch())
        val candidate = mapping.combinedValidationModel()
        val asset = ResourceFactory.createResource("urn:dcai:asset:ASSET-GPU-RACK-ROW-A")
        val unknownState = ResourceFactory.createResource("urn:dcai:state:operational-status:unbounded")
        candidate.removeAll(asset, Dcai.hasOperationalState, null)
        candidate.removeAll(asset, Dcai.hasOperationalStatus, null)
        candidate.add(asset, Dcai.hasOperationalState, unknownState)
        candidate.add(asset, Dcai.hasOperationalStatus, "unbounded")
        candidate.add(unknownState, RDF.type, Dcai.OperationalStatus)
        candidate.add(unknownState, Dcai.hasIdentifier, "unbounded")
        candidate.add(unknownState, RDFS.label, "unbounded")

        val report = gate.validate(candidate)

        assertFalse(report.conforms)
        assertTrue(report.errors.any { it.contains("SHACL validation failed") })
    }
}
