package com.ado81.gdinputbuffer

/**
 * Small, engine-agnostic input buffer core.
 *
 * The Geometry Dash integration must call [requestTap] when the player taps and
 * [onFrame] once per game frame with a collision/safety result obtained from
 * the game's own simulation. This class does not inspect the game or inject
 * touch events by itself.
 */
class InputBufferEngine(
    private val maxWaitFrames: Long = 120L
) {
    enum class FrameSafety {
        SAFE,
        DANGEROUS,
        UNKNOWN
    }

    enum class Event {
        STORED,
        ALREADY_PENDING,
        WAITING_FOR_SAFE_FRAME,
        EXECUTED,
        EXPIRED,
        NOTHING_PENDING
    }

    private data class PendingTap(
        val requestedAtFrame: Long,
        val expiresAtFrame: Long
    )

    private var pendingTap: PendingTap? = null

    init {
        require(maxWaitFrames > 0L) { "maxWaitFrames must be positive" }
    }

    @Synchronized
    fun requestTap(currentFrame: Long): Event {
        if (pendingTap != null) return Event.ALREADY_PENDING
        pendingTap = PendingTap(
            requestedAtFrame = currentFrame,
            expiresAtFrame = currentFrame + maxWaitFrames
        )
        return Event.STORED
    }

    /**
     * Call on the game/simulation thread once for each frame.
     * UNKNOWN is deliberately treated as unsafe: the tap remains buffered.
     */
    @Synchronized
    fun onFrame(
        currentFrame: Long,
        safety: FrameSafety,
        executeTap: () -> Unit
    ): Event {
        val tap = pendingTap ?: return Event.NOTHING_PENDING

        if (currentFrame > tap.expiresAtFrame) {
            pendingTap = null
            return Event.EXPIRED
        }

        if (safety != FrameSafety.SAFE) {
            return Event.WAITING_FOR_SAFE_FRAME
        }

        pendingTap = null
        executeTap()
        return Event.EXECUTED
    }

    @Synchronized
    fun cancelPendingTap(): Boolean {
        if (pendingTap == null) return false
        pendingTap = null
        return true
    }

    @Synchronized
    fun hasPendingTap(): Boolean = pendingTap != null
}
