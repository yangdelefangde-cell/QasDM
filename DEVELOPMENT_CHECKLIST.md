# v54 development checklist

Authorized: user requested the current round on 2026-10-09. APK and standalone HTML share index.html.

Implemented: blue controls and compact settings groups; profile menu deduplication; settings child return/layers; compact session labels and dialog; primary DM and interaction tool continuation; canonical comment avatars, flat reply threads, 3-reply folding, target names, regions, small hearts; optional audience simulation with immutable initial statistics, 20 simulated-entry cap, continuity and non-destructive refresh; comment-only automatic replies; artwork canvas thumbnail scaling; first-person diary prompt; font value/range/reset/preview; jump-button scope; native resized viewport bridge; original handwritten QDM launcher/favicon.

Validation: local Chromium regression includes persistence, simulation counts/identity/likes/baseline, UI layering, main DM failure continuation and native viewport bottom/old-message anchoring. Update/package consistency tests. GitHub CI compiles/lints Android and verifies unchanged signing certificate plus bundled source.

Device follow-up: physical Android IME resize/restore and notifications. No Android emulator or connected phone available here. Search and simulation tested with mocked model responses; no live user credentials supplied. Existing diary content retained. Legacy work initial statistics snapshot starts at migration because earlier edit history is unavailable. Existing incompatible signing certificates still require export/import migration; package identity and fixed signing are unchanged.
