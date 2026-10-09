# v56 development checklist

Authorization: user explicitly approved the entire batch on 2026-10-09 after clarifying that batches should not be limited artificially. HTML and APK share index.html.

## Implemented scope

- 01: provider presets, live model list, manual model ID, reusable connections, ElevenLabs plus compatible/custom TTS, translation provider selector.
- 02: visible task state, execution/failure records, stop and checkpoint continuation controls. This improves the existing runner; it does not imply parity with a full agent IDE.
- 04: permission-bound keyword memory search across messages, diaries, saved memories and summaries, with source identities; user and model entry points. Embedding similarity search is not included.
- 05–06: continuous story framing, screen shake/glow/float/scramble/vibration, effect toggle and reduced-motion behavior.
- 07: focused comparison of provider/knowledge workflows in RikkaHub, Kelivo and SillyTavern, recorded in DESIGN_RESEARCH_v56.md.
- 24: continuous cover wallpaper layer and legible bottom navigation.
- 25: full backup of SMS, media and audio stores, empty-device restoration, old-format compatibility and media validation.
- 26: diary reading layout on phone/tablet, better new-entry instruction, preservation of existing text.

## Verification

Local syntax, version and state-schema checks are run before submission. CI additionally exercises existing regressions and new provider, TTS, memory permission, task, effect, wallpaper, diary and cross-device media restore tests, followed by Android compile/lint, signature and bundled-source checks. CI run 37925360834 passed the full page/device-transfer regressions, Android compile/lint, signature and bundled-source checks on 2026-10-09. The final native-vibration addition is rechecked before release.

23 remains installation verification on the user's device. Prior user-verified v54 keyboard/notifications (10) and search/audience simulation (11) remain verified. New HTML/APK build behavior needs user experience feedback; automated tests do not replace physical-device checks.

Development rule: each feedback round updates the checklist; no future code changes without explicit permission. Use entire agreed batches and unified HTML/APK delivery.
