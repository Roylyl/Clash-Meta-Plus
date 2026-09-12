package com.github.kr328.clash.service.clash.module

import android.app.Service
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.service.StatusProvider

class CloseModule(service: Service) : Module<CloseModule.RequestClose>(service) {
    object RequestClose

    override suspend fun run() {
        val broadcasts = receiveBroadcast {
            addAction(Intents.ACTION_CLASH_REQUEST_STOP)
        }

        // Cover a stop that arrived before this module registered its receiver.
        if (!StatusProvider.shouldStartClashOnBoot)
            enqueueEvent(RequestClose)

        while (true) {
            broadcasts.receive()
            // A later Start supersedes an already queued Stop broadcast.
            if (!StatusProvider.shouldStartClashOnBoot) {
                Log.d("User request close")
                enqueueEvent(RequestClose)
            }
        }
    }
}