package com.github.kr328.clash.service.clash

/** Tracks one runtime independently of a Service object that Android may keep bound. */
internal class RuntimeSession {
    enum class Start { Launch, AlreadyRunning, WaitForCleanup }
    data class Completion(val stoppedStartId: Int, val pendingStartId: Int?)

    private var running = false
    private var stopping = false
    private var activeStartId = 0
    private var pendingStartId: Int? = null

    @Synchronized
    fun isRunning(): Boolean = running

    @Synchronized
    fun hasPendingStart(): Boolean = pendingStartId != null

    @Synchronized
    fun start(startId: Int): Start {
        if (stopping) {
            pendingStartId = startId
            return Start.WaitForCleanup
        }

        activeStartId = startId
        if (running)
            return Start.AlreadyRunning

        running = true
        return Start.Launch
    }

    @Synchronized
    fun closeIfNotRequested(requested: () -> Boolean): Boolean {
        if (requested())
            return false
        beginStop()
        return true
    }

    @Synchronized
    fun disableRecovery(clearRequest: () -> Unit) {
        beginStop()
        if (pendingStartId == null)
            clearRequest()
    }

    @Synchronized
    fun beginStop() {
        if (running)
            stopping = true
    }

    @Synchronized
    fun finish(): Completion? {
        if (!running)
            return null

        val result = Completion(activeStartId, pendingStartId)
        running = false
        stopping = false
        activeStartId = 0
        pendingStartId = null
        return result
    }
}
