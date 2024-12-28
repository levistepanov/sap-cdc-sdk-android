package com.sap.cdc.bitsnbytes.cdc

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sap.cdc.android.sdk.auth.notification.CDCMessageEventBus
import com.sap.cdc.android.sdk.auth.notification.MessageEvent


class AppMessagingService() : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        CDCMessageEventBus.emit(MessageEvent.EventWithToken(token))

    }

    override fun onMessageReceived(message: RemoteMessage) {
        CDCMessageEventBus.emit(MessageEvent.EventWithRemoteMessageData(message.data))
    }

}