#pragma once

#include <cstdint>
#include <optional>

namespace gdib {

enum class FrameSafety {
    Safe,
    Dangerous,
    Unknown,
};

enum class BufferEvent {
    Stored,
    AlreadyPending,
    WaitingForSafeFrame,
    Executed,
    Expired,
    NothingPending,
};

// Deterministic, game-agnostic buffer state machine.
// The Geode hooks must supply real game frames/safety and perform replay
// themselves on the game thread. This class does not inspect the game.
class InputBufferCore {
public:
    explicit InputBufferCore(std::uint64_t maxWaitFrames = 120)
        : m_maxWaitFrames(maxWaitFrames) {}

    BufferEvent requestTap(std::uint64_t currentFrame) {
        if (m_requestedAtFrame.has_value()) {
            return BufferEvent::AlreadyPending;
        }
        m_requestedAtFrame = currentFrame;
        return BufferEvent::Stored;
    }

    BufferEvent onFrame(std::uint64_t currentFrame, FrameSafety safety) {
        if (!m_requestedAtFrame.has_value()) {
            return BufferEvent::NothingPending;
        }

        // A frame counter reset/regression invalidates the old request.
        if (currentFrame < *m_requestedAtFrame) {
            m_requestedAtFrame.reset();
            return BufferEvent::Expired;
        }

        // Subtraction is safe after the regression check; avoids addition overflow.
        if (currentFrame - *m_requestedAtFrame > m_maxWaitFrames) {
            m_requestedAtFrame.reset();
            return BufferEvent::Expired;
        }

        // Fail closed: only an explicitly verified SAFE frame can release input.
        if (safety != FrameSafety::Safe) {
            return BufferEvent::WaitingForSafeFrame;
        }

        m_requestedAtFrame.reset();
        return BufferEvent::Executed;
    }

    bool cancelPendingTap() {
        if (!m_requestedAtFrame.has_value()) {
            return false;
        }
        m_requestedAtFrame.reset();
        return true;
    }

    bool hasPendingTap() const {
        return m_requestedAtFrame.has_value();
    }

private:
    std::uint64_t m_maxWaitFrames;
    std::optional<std::uint64_t> m_requestedAtFrame;
};

} // namespace gdib
