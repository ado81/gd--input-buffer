#include "../src/InputBufferCore.hpp"

#include <cassert>
#include <cstdint>

using gdib::BufferEvent;
using gdib::FrameSafety;
using gdib::InputBufferCore;

int main() {
    InputBufferCore core(3);

    assert(core.onFrame(0, FrameSafety::Safe) == BufferEvent::NothingPending);
    assert(core.requestTap(10) == BufferEvent::Stored);
    assert(core.requestTap(10) == BufferEvent::AlreadyPending);
    assert(core.onFrame(11, FrameSafety::Dangerous) == BufferEvent::WaitingForSafeFrame);
    assert(core.onFrame(12, FrameSafety::Unknown) == BufferEvent::WaitingForSafeFrame);
    assert(core.hasPendingTap());
    assert(core.onFrame(13, FrameSafety::Safe) == BufferEvent::Executed);
    assert(!core.hasPendingTap());

    assert(core.requestTap(20) == BufferEvent::Stored);
    assert(core.onFrame(24, FrameSafety::Safe) == BufferEvent::Expired);
    assert(!core.hasPendingTap());

    assert(core.requestTap(30) == BufferEvent::Stored);
    assert(core.onFrame(29, FrameSafety::Safe) == BufferEvent::Expired);
    assert(!core.hasPendingTap());

    assert(core.requestTap(40) == BufferEvent::Stored);
    assert(core.cancelPendingTap());
    assert(!core.cancelPendingTap());
    assert(core.onFrame(41, FrameSafety::Safe) == BufferEvent::NothingPending);

    // The maximum frame value must not overflow expiry arithmetic.
    InputBufferCore edge(120);
    assert(edge.requestTap(UINT64_MAX - 1) == BufferEvent::Stored);
    assert(edge.onFrame(UINT64_MAX, FrameSafety::Unknown) == BufferEvent::WaitingForSafeFrame);
    assert(edge.onFrame(UINT64_MAX, FrameSafety::Safe) == BufferEvent::Executed);
}
