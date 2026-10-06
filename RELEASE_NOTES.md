QasDM v52 / Android 52.0-test.2

- Browser history stores browsing time separately from refresh time; roles can query their own records.
- Independent idle diary trigger, action-description permission instructions, read state while in current DM settings.
- Voice bubble height matches single-line messages. Multiple unknown SMS identities can be added and restored independently.
- Internal action input dialog, navigation history for profiles, archives, music and plugin screens; double-back exit confirmation.
- Lightweight interface transitions and reduced-motion option.
- Declarative plugin workflows: user editor, role creation, inputs, model steps, conditions, messages, plugin opening, waits, pause/cancel and status.
- Android role-avatar notifications and deep links; skipped notification permission produces no role notifications.
- Native music playback with media notification, lock-screen controls and audio focus. Room operations are aggregated and passed to the role.
- Optional user-enabled foreground background runtime and battery-settings guidance. Runs while the app process survives; Android force-stop/OS termination stops role tasks. No cloud push or reboot startup is provided. Background service has a visible stop control.

Test build, Android 8+. Keep a backup before updating. Actual device background behavior depends on the system; this release is not a guarantee of perpetual background execution.
