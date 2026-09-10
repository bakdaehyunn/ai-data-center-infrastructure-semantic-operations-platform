package com.dcai.semanticservice.validation

import com.dcai.semanticservice.ontology.Dcai
import com.dcai.semanticservice.runtime.SemanticServiceComposition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.vocabulary.OWL
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS

class StateVocabularyContractTest {
    private val repoRoot = SemanticServiceComposition.locateRepoRoot()
    private val ontology = read("ontology/modules/state-vocabulary.ttl")
    private val shapes = read("shapes/state-vocabulary.ttl")

    @Test
    fun keepsOwlEnumerationsAndShaclAllowListsIdentical() {
        controlledCategories.forEach { category ->
            val owlMembers = ontology.enumerationMembers(category)
            val shaclMembers = shapes.allowedMembers(category)

            assertTrue(owlMembers.isNotEmpty(), "OWL enumeration is empty for ${category.localName}")
            assertEquals(owlMembers, shaclMembers, "OWL and SHACL vocabulary drift for ${category.localName}")
            assertTrue(
                ontology.allDifferentMemberSets().contains(owlMembers),
                "OWL enumeration members are not explicitly distinct for ${category.localName}",
            )
        }
    }

    @Test
    fun usesCanonicalUriIdentifierAndLabelForEveryVocabularyMember() {
        controlledCategories.forEach { category ->
            ontology.enumerationMembers(category).forEach { memberUri ->
                val member = ontology.getResource(memberUri)
                val canonicalCode = memberUri.substringAfterLast(':')

                assertTrue(ontology.contains(member, RDF.type, category))
                assertTrue(ontology.contains(member, Dcai.hasIdentifier, canonicalCode))
                assertTrue(ontology.contains(member, RDFS.label, canonicalCode))
            }
        }
    }

    private fun Model.enumerationMembers(category: Resource): Set<String> {
        val definition = getRequiredProperty(category, OWL.equivalentClass).resource
        return rdfList(getRequiredProperty(definition, OWL.oneOf).resource)
    }

    private fun Model.allowedMembers(category: Resource): Set<String> {
        val shape = listSubjectsWithProperty(TARGET_CLASS, category).toList()
            .single { candidate -> contains(candidate, IN) }
        return rdfList(getRequiredProperty(shape, IN).resource)
    }

    private fun Model.allDifferentMemberSets(): Set<Set<String>> =
        listSubjectsWithProperty(RDF.type, OWL.AllDifferent).toList()
            .map { individual -> rdfList(getRequiredProperty(individual, DISTINCT_MEMBERS).resource) }
            .toSet()

    private fun Model.rdfList(head: Resource): Set<String> {
        val members = linkedSetOf<String>()
        var cursor = head
        while (cursor != RDF.nil) {
            val member = getRequiredProperty(cursor, RDF.first).`object`
            require(member.isURIResource) { "Controlled vocabulary member must be an IRI: $member" }
            members += member.asResource().uri
            cursor = getRequiredProperty(cursor, RDF.rest).resource
        }
        return members
    }

    private fun read(relativePath: String): Model = ModelFactory.createDefaultModel().also { model ->
        RDFDataMgr.read(model, repoRoot.resolve(relativePath).toUri().toString())
    }

    private companion object {
        val TARGET_CLASS: Property = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#targetClass")
        val IN: Property = ResourceFactory.createProperty("http://www.w3.org/ns/shacl#in")
        val DISTINCT_MEMBERS: Property = ResourceFactory.createProperty(
            "http://www.w3.org/2002/07/owl#distinctMembers",
        )

        val controlledCategories: List<Resource> = listOf(
            Dcai.CriticalityLevel,
            Dcai.OperationalStatus,
            Dcai.IncidentLifecycleState,
            Dcai.DependencyRole,
            Dcai.ImpactScope,
            Dcai.WorkflowEventStatus,
            Dcai.RedundancyState,
            Dcai.MitigationState,
            Dcai.VendorState,
            Dcai.EvidenceConfidenceState,
            Dcai.TelemetryStatus,
            Dcai.ValidationStatus,
            Dcai.WorkOrderStatus,
        )
    }
}
