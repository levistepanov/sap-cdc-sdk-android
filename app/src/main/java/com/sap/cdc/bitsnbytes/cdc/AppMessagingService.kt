package com.sap.cdc.bitsnbytes.cdc

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sap.cdc.android.sdk.CDCDebuggable
import com.sap.cdc.android.sdk.auth.notification.CDCMessageEventBus
import com.sap.cdc.android.sdk.auth.notification.MessageEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel



class AppMessagingService() : FirebaseMessagingService() {

    companion object {
        const val LOG_TAG = "AppMessagingService"
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        CDCDebuggable.log(LOG_TAG, "onCreate:")
        super.onCreate()
        CDCMessageEventBus.initialize(serviceScope)
    }

    override fun onNewToken(token: String) {
        CDCDebuggable.log(LOG_TAG, "onNewToken:")
        CDCMessageEventBus.emit(MessageEvent.EventWithToken(token))

    }

    override fun onMessageReceived(message: RemoteMessage) {
        CDCDebuggable.log(LOG_TAG, "onMessageReceived:")
        CDCMessageEventBus.emit(MessageEvent.EventWithRemoteMessageData(message.data))

    }

    override fun onDestroy() {
        CDCDebuggable.log(LOG_TAG, "onDestroy:")
        super.onDestroy()
        serviceScope.cancel() // Cancel the scope to avoid memory leaks
    }

}