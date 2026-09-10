package com.dcai.semanticservice.validation

import com.dcai.semanticservice.runtime.SemanticServiceComposition
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertTrue

class OntologyAuthorityBoundaryTest {
    private val repoRoot = SemanticServiceComposition.locateRepoRoot()

    @Test
    fun activeRuntimeArtifactsDoNotReferenceLegacyOntologyNamespace() {
        val offenders = activeRoots.flatMap { relativeRoot ->
            filesUnder(repoRoot.resolve(relativeRoot))
                .filter { file -> file.readText().contains(LEGACY_NAMESPACE) }
                .map { file -> repoRoot.relativize(file).toString() }
        }

        assertTrue(
            offenders.isEmpty(),
            "Active runtime artifacts reference the legacy ontology namespace: ${offenders.joinToString()}",
        )
    }

    private fun filesUnder(root: Path): List<Path> {
        if (!Files.exists(root)) return emptyList()
        return Files.walk(root).use { paths ->
            paths.filter { path -> Files.isRegularFile(path) }.toList()
        }
    }

    private companion object {
        val LEGACY_NAMESPACE = "https://example.local/" + "ai-data-center-infrastructure#"
        val activeRoots = listOf(
            "ontology/modules",
            "shapes",
            "queries",
            "fixtures/rdf",
            "semantic-service/src",
        )
    }
}
