# v53 development checklist

Authorized by user: 2026-10-08, entire current round, APK and HTML.

Implemented: notification/avatar timeout and lifecycle, stable signing/build identity, update fallback and startup card, public settings, persistent sessions and SMS association, optional user archive, simulated browser timestamps, diary visual distinction, system document picker retained, short about copy and original support image, first-launch prank, publishing permissions/results, work contrast, comments/tools, configurable Tavily search/extract with native bridge, viewport reading position, vector/adaptive icon.

Validation: local Chromium UI regression and session/comments tests; update comparison/fallback tests. Android compile/lint/signature and downloaded bundle pending GitHub build. Real-device notification/keyboard/old-signature migration require user validation.

Limitations: no separate domestic update hosting configured; both fallback sources currently GitHub. Search credentials not provided, no live provider request made. Existing incompatible Android signing certificates cannot be retroactively replaced; export/import needed for those versions. Shared session memory intentionally includes long-term memory only, not entire hidden chat histories.
