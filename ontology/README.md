# Ontology authority

The runtime ontology contract is the `urn:dcai:ontology:` vocabulary in
`ontology/modules/`. `ontology/modules/state-vocabulary.ttl` defines the
cross-domain state separation contract.

The earlier `https://example.local/` scaffold files have been moved to
`ontology/legacy/`. They are historical reference artifacts only and are not
loaded by `SemanticValidationEngine`. New vocabulary, mappings, fixtures, and
shapes must not depend on those files.

In the authoritative model:

- `WorkflowStage` is the process position, exposed through `hasCurrentStage`.
- `IncidentLifecycleState` is the independent incident lifecycle, exposed
  through `hasIncidentLifecycleState`.
- `IncidentStageState` and `hasIncidentStageState` are deprecated because they
  duplicated the workflow stage.
- Every non-deprecated class derived from `ControlledVocabularyTerm` is closed with
  `owl:oneOf` in `state-vocabulary.ttl`; SHACL rejects an IRI outside the same
  list because OWL itself follows the open-world assumption.
- Canonical vocabulary identifiers and labels use lowercase kebab-case. Source
  spellings such as `IN_PROGRESS`, `N_PLUS_1`, or `AT_RISK` are normalized by
  the RDF mapper before promotion, so one semantic value always has one IRI and
  one canonical lexical form.
- The closed categories cover criticality, operational status, incident
  lifecycle, dependency role, impact scope, workflow-event status, redundancy,
  mitigation, vendor, evidence confidence, telemetry, validation, and work
  order status. Adding a value requires an explicit ontology and SHACL change.
