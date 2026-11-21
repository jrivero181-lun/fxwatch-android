<!-- Title: Conventional Commits, e.g. feat(core): short description -->

## Summary

<!-- What this PR does and why, in 2 or 3 sentences. State what is intentionally left out. -->

Spec: <!-- specs/NN-slug.md, or "None" -->

## Changes

<!-- Describe the changes by module or area. Use a table when it helps. Remove what doesn't apply. -->

| Module / area | Changes |
|---|---|
| | |

## Key decisions

<!-- One bullet per architectural decision: what was chosen, why, and what was discarded. Write "None" if there are none. -->

- **Decision.** Reason.

## Tests

<!-- New or updated tests and what they cover. If no tests were needed, explain why. -->

## Other changes

<!-- Build, CI, docs or housekeeping. Remove this section if empty. -->

## Verification

<!-- Check what you ran. Leave unchecked and explain anything that doesn't apply. -->

- [ ] `./gradlew ktlintCheck detekt testDebugUnitTest` passes locally
- [ ] `./gradlew lintDebug assembleDebug assembleRelease` passes locally
- [ ] Second run reuses the configuration cache
- [ ] `./gradlew connectedDebugAndroidTest` passes on an emulator or device
- [ ] The debug app opens without crashing
- [ ] CI workflow passes on this PR
