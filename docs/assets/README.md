# Current demo screenshots

Captured on 2026-10-10 from the running React frontend, Kotlin private query
service, and Fuseki. No mocked browser responses or manually supplied UI values
were used. The data is synthetic; no real customer systems are connected.

| Image | Route | Requested viewport | Saved image |
| --- | --- | --- | --- |
| `dashboard-preview.jpg` | `/` | 1440 × 960 | 1425 × 950 |
| `follow-up-detail-preview.jpg` | `/recovery-cases/INC-GEN-SCN-20260611` | 1440 × 960 | 1425 × 950 |
| `dashboard-mobile-preview.jpg` | `/` | 390 × 844 | 375 × 812 |

The browser capture API scales the output; the saved dimensions above were
checked from the image files.

These JPEGs supersede the August 2026 PNG previews.

## Reproduce the data

Follow the root README's generation, service, forwarding-container, and
frontend commands. For the captures, an isolated in-memory Fuseki container was
used instead of the persistent Compose database:

```bash
docker run --rm --name dcai-screenshot-fuseki \
  -p 127.0.0.1:3031:3030 \
  atomgraph/fuseki:latest --update --mem /infrastructure
```

Use `DCAI_FUSEKI_DATASET_URL=http://host.docker.internal:3031/infrastructure`
in both Gradle Docker commands. Generate profile `mvp`, seed `20260610`, source
release `generated-mvp-seed-20260610`, and reasoning run
`generated-mvp-seed-20260610-reasoning`.

Wait for successful promotion/reasoning and for the UI to show loaded data
before capturing. The isolated dataset contains 48 generated cases. Existing
graphs in a persistent dataset can change counts and evidence timelines.

The displayed priorities and time-in-stage can be unknown because the source
contract does not supply them. Fallback row numbers are display ordering, not
reasoner-derived operational priorities. Capacity and GPU counts are simulated
source values. No external action was submitted during capture.

## Checks performed

- Source promotion and reasoning completed against the isolated dataset.
- Queue and recovery case loaded through the real private query endpoint.
- Desktop and mobile captures were visually inspected.
- Frontend unit tests, ESLint, and production build passed.

This screenshot refresh did not run the full Kotlin suite or introduce a
browser E2E suite.
