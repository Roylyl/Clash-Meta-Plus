package com.github.kr328.clash.service.clash

import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.clash.module.Module
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val globalLock = Mutex()

interface ClashRuntimeScope {
    fun <E, T : Module<E>> install(module: T): T
}

interface ClashRuntime {
    fun launch(onFailure: (Throwable) -> Unit, onStopped: () -> Unit = {})
    fun stop()
    fun requestGc()
}

fun CoroutineScope.clashRuntime(block: suspend ClashRuntimeScope.() -> Unit): ClashRuntime {
    return object : ClashRuntime {
        @Volatile
        private var runtimeJob: Job? = null

        override fun launch(onFailure: (Throwable) -> Unit, onStopped: () -> Unit) {
            check(runtimeJob?.isCompleted != false) { "Previous runtime is still cleaning up" }
            val job = launch(Dispatchers.IO, start = CoroutineStart.LAZY) {
                try {
                    globalLock.withLock {
                        Log.d("ClashRuntime: initialize")

                        try {
                            // Wait for all module cleanup before reporting failures. Handling
                            // them here prevents a failing module from crashing into a sticky
                            // process restart loop (for example, an invalid configuration).
                            coroutineScope {
                                Clash.reset()
                                Clash.clearOverride(Clash.OverrideSlot.Session)

                                val scope = object : ClashRuntimeScope {
                                    override fun <E, T : Module<E>> install(module: T): T {
                                        launch { module.execute() }
                                        return module
                                    }
                                }

                                scope.block()
                                coroutineContext.cancelChildren()
                            }
                        } finally {
                            withContext(NonCancellable) {
                                Clash.reset()
                                Clash.clearOverride(Clash.OverrideSlot.Session)
                                Log.d("ClashRuntime: destroyed")
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    onFailure(e)
                }
            }
            // Completion also runs if a stop races with the lazy job's initial dispatch.
            // It follows child cleanup and does not depend on Service.onDestroy().
            job.invokeOnCompletion { onStopped() }
            runtimeJob = job
            job.start()
        }

        override fun stop() {
            runtimeJob?.cancel()
        }

        override fun requestGc() {
            Clash.forceGc()
        }
    }
}
