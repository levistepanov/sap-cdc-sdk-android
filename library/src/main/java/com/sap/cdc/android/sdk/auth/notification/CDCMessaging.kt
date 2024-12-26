package com.sap.cdc.android.sdk.auth.notification

import com.sap.cdc.android.sdk.auth.AuthenticationService
import com.sap.cdc.android.sdk.auth.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Created by Tal Mirmelshtein on 20/07/2024
 * Copyright: SAP LTD.
 */

object CDCMessageEventBus {

    private val eventFlow = MutableSharedFlow<MessageEvent>()
    private var scope: CoroutineScope? = null

    fun initialize(scope: CoroutineScope) {
        this.scope = scope
    }

    fun subscribe(block: suspend (MessageEvent) -> Unit) {
        scope?.let {
            eventFlow.onEach(block).launchIn(it)
        }
    }

    fun emit(appEvent: MessageEvent) = scope?.launch { eventFlow.emit(appEvent) }
}


sealed class MessageEvent {

    data class EventWithToken(val token: String) : MessageEvent()

    data class EventWithRemoteMessageData(val data: Map<String, String>) : MessageEvent()
}

interface IFCMTokenRequest {

    fun requestFCMToken()
}

class CDCNotificationManager(
    private val authenticationService: AuthenticationService
) {

    init {
        CDCMessageEventBus.subscribe {
            when (it) {
                is MessageEvent.EventWithToken -> onNewToken(it.token)
                is MessageEvent.EventWithRemoteMessageData -> onMessageReceived(it.data)
            }
        }
    }

    private fun onNewToken(token: String) {
        // Handle the token
        authenticationService.updateDeviceInfo(DeviceInfo(pushToken = token))
    }

    private fun onMessageReceived(data: Map<String, String>) {
        // Handle the message
    }
}