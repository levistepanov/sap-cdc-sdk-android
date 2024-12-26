package com.sap.cdc.android.sdk.auth.notification

interface IMessagingService {

    fun onMessageReceived(messageData: Map<String, String>)

    suspend fun onNewToken(token: String)

}