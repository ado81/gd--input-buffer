# GD Input Buffer — Geode Android integration status

Last checked: 2026-11-07

## Confirmed launcher target

The official Geode Android launcher source currently declares:
- `SUPPORTED_VERSION_STRING = "2.2.144"`
- `SUPPORTED_VERSION_CODE = 41`
- warning threshold version code: `41`

Source: [Constants.kt](https://github.com/geode-sdk/android-launcher/blob/main/app/src/main/java/com/geode/launcher/utils/Constants.kt).

This confirms the launcher's declared target is the user's Geometry Dash 2.2.144. It does **not** prove that a custom native mod's hooks or collision logic are compatible.

## Bindings investigation: important refinement

The official `geode-sdk/bindings` repository does contain `bindings/2.200`; the earlier statement that only newer version directories existed was incomplete.

The bindings README says current work mainly targets 2.2091, while older-version binding contributions are also accepted. The `2.200` folder contains `GeometryDash.bro`, `Cocos2d.bro`, and related files.

Inspection of `bindings/2.200/GeometryDash.bro` found:
- `PlayLayer` and `PlayerObject` are declared with `[[link(android)]]`.
- Android-specific layout padding entries exist in the file.
- Relevant gameplay methods such as `PlayerObject::pushButton`, `PlayerObject::releaseButton`, `PlayerObject::preCollision`, `PlayerObject::postCollision`, and `PlayLayer::postUpdate` show Windows/macOS addresses, but no Android addresses in the inspected declarations.
- `GJBaseGameLayer::checkCollisions` likewise has Windows/macOS addresses only in the inspected declaration.

Therefore, `2.200` is a useful starting point for class and layout research, but the inspected data does **not** currently provide the Android function addresses needed to implement verified input interception or collision-state hooks. Do not infer Android offsets from Windows/macOS offsets.

The bindings CMake project selects a versioned folder through `GEODE_GD_VERSION` (defaulting to `bindings/<version>`) and has Android-specific build handling. This is build-system support, not proof that the target binary has all required bindings.

## Android ABI

The current official launcher Gradle configuration includes both:
- `arm64-v8a` (64-bit)
- `armeabi-v7a` (32-bit)

The user's installed game's active ABI is not yet known. A mod build must include the ABI used by the game process.

## Current repository implementation review

### Kotlin overlay
The floating `GD TAP` overlay currently only displays a demo toast, “Entrada procesada (demo)”. It does not send input to Geometry Dash. This must not be represented as a working gameplay feature.

### InputBufferEngine
The Kotlin engine-independent prototype:
- stores one pending tap;
- waits while safety is `DANGEROUS` or `UNKNOWN`;
- calls the supplied callback only when safety is `SAFE`;
- expires a pending tap after the configured frame window (default 120 frames);
- supports cancellation.

Limitations:
- no Geometry Dash frame source;
- no collision/safety source;
- no native input interception or replay;
- no monotonic-frame or overflow validation;
- callback runs while the synchronized method is active. A future integration should dispatch minimal work on the game thread.

## Intended native architecture

The real feature must run inside the game through a native Geode mod:

1. Intercept the player's input within the game.
2. Store a pending input with its originating frame.
3. Read a trustworthy safety result from the game's own simulation.
4. Treat unknown state as unsafe.
5. Replay a pending input only at a verified safe point on the game thread.
6. Expire or cancel stale pending inputs so delayed actions cannot surprise the player.

The Android overlay cannot inspect Geometry Dash's internal simulation state by itself.

## Engineering gates

1. Confirm the exact ABI used by the installed game (arm64-v8a or armeabi-v7a).
2. Establish whether the installed Android 2.2.144 binary maps to the available `2.200` binding set for Geode mod metadata and code generation.
3. Obtain or verify Android-specific addresses/signatures for input and simulation hooks against that exact binary; do not reuse Windows/macOS offsets.
4. Build a minimal native Geode mod that only reports that it loaded; add frame/input reporting only after suitable Android hooks are verified.
5. Add buffering and replay only after safety timing has been validated in controlled tests.

## Upstream references

- [Official Geode Android launcher version constants](https://github.com/geode-sdk/android-launcher/blob/main/app/src/main/java/com/geode/launcher/utils/Constants.kt)
- [Official Geode bindings README](https://github.com/geode-sdk/bindings)
- [Bindings for 2.200](https://github.com/geode-sdk/bindings/tree/main/bindings/2.200)
- [Bindings GeometryDash.bro for 2.200](https://github.com/geode-sdk/bindings/blob/main/bindings/2.200/GeometryDash.bro)
- [Official Geode SDK minimal mod template](https://github.com/geode-sdk/template-mods/tree/main/minimal)
- [Official Geode SDK documentation](https://docs.geode-sdk.org/)
