# GD Input Buffer — Geode Android integration status

Last checked: 2026-11-07

## Confirmed launcher compatibility

The official Geode Android launcher currently declares:
- `SUPPORTED_VERSION_STRING = "2.2.144"`
- `SUPPORTED_VERSION_CODE = 41`
- warning threshold version code: `41`

This confirms the launcher's declared target is the user's Geometry Dash 2.2.144. It does **not** prove that our future mod's hooks or collision logic are compatible.

## SDK/bindings status

The official `geode-sdk/bindings` README currently says its main binding work targets Geometry Dash 2.2081. The repository has version directories for releases such as 2.2081 and 2.2091, but no directory for 2.2.144.

Therefore:
- Launcher target: 2.2.144 (confirmed from official launcher source).
- Exact SDK bindings and native hook addresses for Android 2.2.144: not confirmed.
- A working native mod for the user's version: not built yet.

Do not reuse addresses from another game version. Do not label the mod compatible until the exact target binary and ABI are verified.

## Current repository implementation review

### Kotlin overlay
The floating `GD TAP` overlay currently only displays a demo toast, “Entrada procesada (demo)”. It does not send input to Geometry Dash. This is intentional and must not be represented as a working gameplay feature.

### InputBufferEngine
The Kotlin engine-independent prototype:
- stores one pending tap;
- waits while safety is `DANGEROUS` or `UNKNOWN`;
- calls the supplied callback only when safety is `SAFE`;
- expires a pending tap after the configured frame window (default 120 frames);
- supports cancellation.

Limitations found in review:
- It has no Geometry Dash frame source.
- It has no collision/safety source.
- It has no native input interception or replay implementation.
- It does not validate monotonic frame numbers or frame-counter overflow.
- Its callback runs while the engine's synchronized method is active; a future game integration should move execution to the game thread and keep callback work minimal.

## Intended native architecture

The real feature must run inside the game through a native Geode mod:

1. Intercept the player's input within the game.
2. Store a pending input with its originating frame.
3. Read a trustworthy safety result from the game's own simulation.
4. Treat unknown state as unsafe.
5. Replay a pending input only at a verified safe point on the game thread.
6. Expire or cancel stale pending inputs so delayed actions cannot surprise the player.

The Android overlay cannot inspect Geometry Dash's internal simulation state by itself.

## Next engineering gates

1. Identify the exact ABI used by the installed game (arm64-v8a or armeabi-v7a).
2. Determine whether a Geode SDK build can target the exact Android 2.2.144 binary; if not, establish a supported path rather than guessing addresses.
3. Verify input interception and collision/simulation hooks against that exact binary.
4. Build a minimal native mod that only reports frame and input events.
5. Add buffering and replay only after safety timing has been validated in controlled tests.

## Upstream references

- Official Geode Android launcher constants: https://github.com/geode-sdk/android-launcher/blob/main/app/src/main/java/com/geode/launcher/utils/Constants.kt
- Official Geode bindings and target-version note: https://github.com/geode-sdk/bindings
- Official Geode SDK mod template: https://github.com/geode-sdk/template-mods/tree/main/minimal
- Official Geode SDK documentation: https://docs.geode-sdk.org/
