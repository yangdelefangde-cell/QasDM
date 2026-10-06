QasDM v52-1 / Android 52.1-test.1

- Fix root tab visibility, footer placement and back navigation replaying closed pages.
- Clear saved tool evidence, interruption/resume state and task drafts; abort active role runs and discard old model replies after memory reset. Diaries remain preserved as described by the existing clear dialog.
- Single role-avatar notification; rasterize avatars for Android and provide a silhouette fallback.
- Separate role permissions: inspect currently playing music room (off by default), inspect existing music playlist (on by default). Current-room tool exposes song and optional second listener name only while playback is active.
- Smooth private-chat transitions and individual selected-message lift with reduced-motion support.
- Workflow editor available directly from plugins; automatic step IDs and dropdown branches/plugin choices, no user code required.
- Image upload source chooser: system gallery or system document picker. Other files use the system document picker.

Android 8+. Back up before updating. Native notification layout and picker behavior require device verification.
