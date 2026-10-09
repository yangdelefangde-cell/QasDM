# v55 development checklist

Authorized: user explicitly allowed edits on 2026-10-09, after requesting HTML and APK co-development. Both outputs share index.html.

Implemented in this round: audit items 12–19; workflow cancellation/request coordination (20); independent-session browsing state reset (21); shared-source/version/HTML/APK delivery (22). Dark menu/SMS theme consistency (03) and root home-back handling (08) receive targeted corrections. Other feature ideas remain pending: supplier/model API/TTS configuration (01), broader task UX (02), cross-chat memory search beyond existing chat retrieval (04), story-context wording (05), additional story effects (06), comparable-app research (07).

Validation: version/update and backup-schema/runtime-cleanup tests run locally. Browser regressions cover active deletion/reload, transient flags, invalid imports, switched-conversation reply ownership, write deduplication/resume, dialog cancellation, workflow input/network cancellation, root home-back and SMS restore/reload. CI runs browser regressions then Android compile/lint and signature/bundled-source verification. Record actual CI outcome when complete.

User has already verified v54 Android keyboard/notifications (10) and live search/audience simulation (11), reporting no observed issues. Preserve that status; this does not substitute for v55 regression results. Physical Android behavior still needs installation verification after this new build. Backup media remains excluded; legacy backups/snapshots do not contain SMS.
