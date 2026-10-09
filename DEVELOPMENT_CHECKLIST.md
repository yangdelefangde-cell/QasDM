# v57 batch 27–47

Authorization: user approved the full batch with “那你现在可以改了”. HTML and APK share index.html.

Implemented: separate fullscreen settings (28), themes (29), named reusable connections and keys/model selection (30,43), grouped execution records and workspace (31–33), profile layout and group identity/message/settings repairs (34–38), workflow skills (39), back navigation (40), local indexed search (41), capability authorization (42), usage/context accounting (44), image adapters (45), file picker/share previews (46), compact follower counts (47), explicit plugin/MCP separation (27).

Limits: counts start at v57; token estimates are not tokenizer-exact; prices are user-entered estimates; PDF rendering depends on WebView; external API responses and Android file-manager intents require account/device validation. No blanket Android permission is requested for hypothetical future features.

Validation status: static/version/state tests pass. Browser regression and Android build/lint/signature checks are in progress before release.

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
