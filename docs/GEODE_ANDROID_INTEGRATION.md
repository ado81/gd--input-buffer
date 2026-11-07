# GD Input Buffer — Geode Android integration status

Last checked: 2026-11-07

## Correct version mapping: Android 2.2.144 -> Geode 2.2081

The official Geode Android launcher source currently declares:
- `SUPPORTED_VERSION_STRING = "2.2.144"`
- `SUPPORTED_VERSION_CODE = 41`
- `SUPPORTED_VERSION_CODE_MIN_WARNING = 41`

However, the launcher does not use the Android display version string as the unified bindings version when a known package version code is present.

In `GamePackageUtils.kt`, the official mapping is:
- Android package version code `41` -> unified Geometry Dash version `2.2081`
- `getUnifiedVersionName()` returns the mapped value for known version codes.
- `getSupportedUnifiedVersion()` also returns the mapped value for the launcher's supported version code.

**Conclusion:** for a Google Play installation reporting Android version name `2.2.144`, the corresponding Geode bindings target is `2.2081`, provided the installed package version code is 41 as the official launcher expects. The earlier focus on `bindings/2.200` was incorrect for this target.

Sources:
- [Launcher version constants](https://github.com/geode-sdk/android-launcher/blob/main/app/src/main/java/com/geode/launcher/utils/Constants.kt)
- [Launcher version-code mapping and ABI detection](https://github.com/geode-sdk/android-launcher/blob/main/app/src/main/java/com/geode/launcher/utils/GamePackageUtils.kt)

## Correct bindings folder

The official bindings repository contains `bindings/2.2081` with `GeometryDash.bro` (about 20,684 lines) and an `Entry.bro`.

Inspection of the 2.2081 Broma file confirms:
- `GJBaseGameLayer`, `PlayLayer`, and `PlayerObject` are marked with `[[link(android)]]`.
- Relevant declarations include `GJBaseGameLayer::checkCollisions`, `GJBaseGameLayer::handleButton`, `PlayerObject::pushButton`, `PlayerObject::releaseButton`, `PlayerObject::preCollision`, and `PlayerObject::postCollision`.
- The listed explicit addresses for these methods are for Windows/macOS/iOS; the inspected declarations do not list numeric Android offsets.

The bindings README explains that `android` is a platform that can link directly to symbols. Thus, the absence of a numeric Android offset in these declarations is not by itself proof that the method is unavailable on Android. It means the native build must verify that the required symbols are available for the actual Android game binary and ABI.

The official Geode GD update procedure specifically says that Android symbol stubs should be generated from `libcocos2dcpp.so` for **both** Android architectures. That is the correct verification step; do not substitute Windows/macOS/iOS offsets.

Sources:
- [Bindings README and platform-linking syntax](https://github.com/geode-sdk/bindings)
- [2.2081 GeometryDash.bro](https://github.com/geode-sdk/bindings/blob/main/bindings/2.2081/GeometryDash.bro)
- [Official GD update procedure](https://github.com/geode-sdk/geode/wiki/GD-Update-Procedure)

## Native Geode scaffold added

A diagnostic-only native mod scaffold now exists in `geode-mod/`:
- `CMakeLists.txt` follows the official minimal Geode template.
- `mod.json` targets Android GD `2.2081` and Geode `5.11.2`.
- `src/main.cpp` logs a message when the mod loads.

This scaffold intentionally has **no gameplay hooks**, no input interception, no collision hooks, and no touch replay. It is not yet a verified build or installable mod package.

## Android ABI

The official launcher Gradle configuration includes:
- `arm64-v8a` (64-bit)
- `armeabi-v7a` (32-bit)

The launcher source includes ABI detection and logs the game version code and selected architecture. A correct native build must include the ABI used by the Geometry Dash process. We can prepare dual-ABI outputs, but symbol verification must be performed separately for each ABI.

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

1. Confirm the installed package version code is 41 (the launcher maps this to unified version 2.2081).
2. Confirm the ABI(s) present in the installed game package.
3. Verify that the needed Android symbols for `handleButton`, `pushButton`, `releaseButton`, and collision/update lifecycle methods are present in the corresponding Android binary stubs.
4. Build the diagnostic Geode scaffold for the selected Android ABI(s) and confirm that it loads without gameplay hooks.
5. Add frame/input diagnostics only after symbol availability and hook signatures are verified.
6. Add buffering and replay only after safety timing has been validated in controlled tests.

## Upstream references

- [Official Geode Android launcher version constants](https://github.com/geode-sdk/android-launcher/blob/main/app/src/main/java/com/geode/launcher/utils/Constants.kt)
- [Official Geode Android launcher version mapping](https://github.com/geode-sdk/android-launcher/blob/main/app/src/main/java/com/geode/launcher/utils/GamePackageUtils.kt)
- [Bindings README](https://github.com/geode-sdk/bindings)
- [Bindings for 2.2081](https://github.com/geode-sdk/bindings/tree/main/bindings/2.2081)
- [2.2081 GeometryDash.bro](https://github.com/geode-sdk/bindings/blob/main/bindings/2.2081/GeometryDash.bro)
- [Official Geode minimal mod template](https://github.com/geode-sdk/template-mods/tree/main/minimal)
- [Official Geode GD update procedure](https://github.com/geode-sdk/geode/wiki/GD-Update-Procedure)
- [Geode SDK documentation](https://docs.geode-sdk.org/)
