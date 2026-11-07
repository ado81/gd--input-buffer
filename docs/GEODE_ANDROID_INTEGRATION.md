# GD Input Buffer — Geode Android integration status

Last checked: 2026-11-07

## Confirmed compatibility

- The official Geode Android launcher repository currently declares:
  - `SUPPORTED_VERSION_STRING = "2.2.144"`
  - `SUPPORTED_VERSION_CODE = 41`
  - `SUPPORTED_VERSION_CODE_MIN_WARNING = 41`
- This is a positive sign for launching the user's installed Geometry Dash 2.2.144 through Geode.

## Important SDK caveat

The official `geode-sdk/bindings` repository README says its current binding work targets Geometry Dash 2.2091. The current bindings tree contains version directories such as `2.2081` and `2.2091`, but no directory named `2.2.144`.

Therefore, launcher compatibility is confirmed, but exact native SDK/binding compatibility for the Android 2.2.144 binary is **not yet confirmed**. Do not mark the mod as compatible or produce an installable mod package until the target binary's bindings and hook addresses are verified.

## Architecture decision

The Android overlay is not able to inspect Geometry Dash's internal simulation state. The actual feature must live inside a native Geode mod:

1. Receive the player's input inside the game.
2. Maintain a pending input in a frame-based queue.
3. Read a trustworthy safety result from the game's own simulation.
4. Treat unknown state as unsafe.
5. Replay a pending input only at a verified safe point.
6. Expire or cancel stale pending inputs to avoid delayed, surprising actions.

The existing Kotlin `InputBufferEngine` is only an engine-independent prototype. It is not connected to Geometry Dash and does not inject game input.

## Next engineering gates

1. Identify the exact Android ABI used by the installed game (arm64-v8a or armeabi-v7a).
2. Obtain/verify Geode SDK bindings for the exact 2.2.144 Android binary.
3. Verify the input interception hook and collision/simulation hook against those bindings.
4. Build a minimal native Geode mod that only reports game-frame/input events; do not replay inputs yet.
5. Add the buffer and enable replay only after frame safety is validated in controlled tests.

## Upstream references

- Geode Android launcher constants: https://github.com/geode-sdk/android-launcher/blob/main/app/src/main/java/com/geode/launcher/utils/Constants.kt
- Geode bindings repository and current target-version note: https://github.com/geode-sdk/bindings
- Official Geode SDK mod template: https://github.com/geode-sdk/template-mods/tree/main/minimal
- Official Geode SDK documentation: https://docs.geode-sdk.org/
