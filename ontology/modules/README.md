# Authoritative Ontology Modules

This directory contains the active versioned OWL/RDFS domain contract loaded by
the semantic service. The historical `https://example.local/` scaffold is kept
under `ontology/legacy/` and must not be imported by runtime modules, shapes,
queries, fixtures, or application code.

Active modules:

- `core.ttl`
- `infrastructure.ttl`
- `topology.ttl`
- `workflow.ttl`
- `impact.ttl`
- `evidence.ttl`
- `provenance.ttl`
- `ai-interaction.ttl`
- `operations.ttl`
- `state-vocabulary.ttl`

`state-vocabulary.ttl` is the authority for finite controlled vocabularies and
cross-category separation. Runtime validation loads schemas only from this
directory and executable SHACL constraints from the repository-level `shapes/`
directory.
