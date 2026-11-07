#include <Geode/Geode.hpp>

using namespace geode::prelude;

// Diagnostic-only milestone: prove that the native mod loads.
// No input hooks, collision hooks, or touch replay are enabled here.
$on_mod(Loaded) {
    log::info("GD Input Buffer native scaffold loaded (diagnostic mode).");
}
