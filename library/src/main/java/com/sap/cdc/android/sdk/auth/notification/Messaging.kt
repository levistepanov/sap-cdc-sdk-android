package com.sap.cdc.android.sdk.auth.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sap.cdc.android.sdk.CDCDebuggable
import com.sap.cdc.android.sdk.auth.AuthenticationService
import com.sap.cdc.android.sdk.auth.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.abs


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

    fun dispose() {
        scope?.cancel()
        scope = null
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

/**
 * Notification issuer for push authentication flows.
 */
class CDCNotificationManager(
    private val authenticationService: AuthenticationService,
    private val notificationOptions: CDCNotificationOptions? = CDCNotificationOptions(),
) {

    companion object {
        const val LOG_TAG = "CDCNotificationManager"

        const val CDC_NOTIFICATIONS_CHANNEL_ID = "CDC_AUTHENTICATION_NOTIFICATIONS"
        const val CDC_NOTIFICATIONS_ACTIONS_REQUEST_CODE = 2020
        const val CDC_NOTIFICATIONS_CONTENT_REQUEST_CODE = 2021

        const val BUNDLE_ID_ACTION_DATA = "cdc_action_data"
    }

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
        val mode = data["mode"] ?: ""
        when (mode) {
            "optin", "verify" -> {
                notify(mode, data)
            }

            "cancel" -> {
                val gigyaAssertion = data["gigyaAssertion"]
                if (gigyaAssertion != null) {
                    cancel(abs(gigyaAssertion.hashCode().toDouble()).toInt())
                }
            }

            "" -> return
        }
    }

    private fun notify(mode: String, data: Map<String, String>) {

        // Parse data fields from cdc push.
        val title = data["title"]
        val body = data["body"]
        val gigyaAssertion = data["gigyaAssertion"]
        val verificationToken = data["verificationToken"]

        if (gigyaAssertion == null || verificationToken == null) {
            CDCDebuggable.log(LOG_TAG, "Missing gigyaAssertion in notification data.")
            return
        }

        // the unique notification id will be the hash code of the gigyaAssertion field.
        // gigyaAssertion hash will act as notification id.
        val notificationId = abs(gigyaAssertion.hashCode())

        val actionData =
            CDCNotificationActionData(mode, gigyaAssertion, verificationToken, notificationId)

        // Reference context.
        val context = authenticationService.siteConfig.applicationContext

        // Create notification manager.
        val notificationManager = NotificationManagerCompat.from(context)

        // Build notification.
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, CDC_NOTIFICATIONS_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title?.trim { it <= ' ' } ?: "")
                .setContentText(body?.trim { it <= ' ' } ?: "")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTimeoutAfter(notificationOptions?.notificationTimeout!!)
                .setAutoCancel(true)

        // Notification channel required for Android O and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CDC_NOTIFICATIONS_CHANNEL_ID,
                notificationOptions.notificationChannelTitle,
                NotificationManager.IMPORTANCE_HIGH
            )
            builder.setChannelId(channel.id)
            notificationManager.createNotificationChannel(channel)
        }

        // Define actions pending intent flags.
        val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

        // Activity content view to handle when the notification is clicked.
        if (notificationOptions.contentView != null) {
            // Content activity pending intent.
            val intent = Intent(context, notificationOptions.contentView)
            intent.putExtra(BUNDLE_ID_ACTION_DATA, actionData)

            val pendingIntent = PendingIntent.getActivity(
                context, CDC_NOTIFICATIONS_CONTENT_REQUEST_CODE,
                intent, flags
            )

            // We don't want the annoying enter animation.
            intent.addFlags(
                (Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )

            builder.setContentIntent(pendingIntent)
        }

        // Deny action.
        val denyIntent =
            Intent(context, CDCNotificationReceiver::class.java) // Missing receiver class
        denyIntent.putExtra(BUNDLE_ID_ACTION_DATA, actionData)

        denyIntent.setAction("Deny") // Missing resource string
        val denyPendingIntent =
            PendingIntent.getBroadcast(
                context, CDC_NOTIFICATIONS_ACTIONS_REQUEST_CODE, denyIntent,
                flags
            )


        // Approve action.
        val approveIntent = Intent(
            authenticationService.siteConfig.applicationContext,
            CDCNotificationReceiver::class.java
        ) // Missing receiver class
        approveIntent.putExtra(BUNDLE_ID_ACTION_DATA, actionData)

        approveIntent.setAction("Approve") // Missing resource string
        val approvePendingIntent =
            PendingIntent.getBroadcast(
                context, CDC_NOTIFICATIONS_ACTIONS_REQUEST_CODE, approveIntent,
                flags
            )

        builder
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                notificationOptions.actionNegative?.title ?: "Deny",
                denyPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_save,
                notificationOptions.actionPositive?.title ?: "Approve",
                approvePendingIntent
            )

        // Notify.
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(notificationId, builder.build())
        }
    }

    private fun cancel(idToCancel: Int) {
        if (idToCancel == 0) {
            return
        }
        CDCDebuggable.log(
            LOG_TAG,
            "Cancel notification with id = $idToCancel"
        )

        // Reference context.
        val context = authenticationService.siteConfig.applicationContext

        // Cancel notification.
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(idToCancel)
    }
}

class CDCNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val LOG_TAG = "CDCNotificationReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        // Check if broadcast is actionable. return if not.
        if (intent == null) {
            CDCDebuggable.log(LOG_TAG, "Intent is null.")
            return
        }
        if (intent.extras == null) {
            CDCDebuggable.log(LOG_TAG, "Intent extras are null.")
            return
        }

        // Broadcast is actionable.

        val actionData: CDCNotificationActionData? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(
                    CDCNotificationManager.BUNDLE_ID_ACTION_DATA,
                    CDCNotificationActionData::class.java
                )
            } else {
                intent.getParcelableExtra(CDCNotificationManager.BUNDLE_ID_ACTION_DATA)
            }

        // Cancel the notification.
        val notificationManager = NotificationManagerCompat.from(context!!)
        notificationManager.cancel(actionData?.notificationId ?: 0)

        // Handle the action.
        when (intent.action) {
            "Approve" -> {
                // Handle approve action.
                when (actionData?.mode) {
                    "optin" -> {
                        // Handle optin action.
                    }

                    "verify" -> {
                        // Handle verify action.
                    }
                }
            }

            "Deny" -> {
                // Handle deny action.
                when (actionData?.mode) {
                    "optin" -> {
                        // Handle optin action.
                    }

                    "verify" -> {
                        // Handle verify action.
                    }
                }
            }
        }

    }

}