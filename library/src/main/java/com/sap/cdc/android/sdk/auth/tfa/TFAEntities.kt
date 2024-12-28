package com.sap.cdc.android.sdk.auth.tfa

import kotlinx.serialization.Serializable

/**
 * Created by Tal Mirmelshtein on 10/06/2024
 * Copyright: SAP LTD.
 */

@Serializable
data class TFAProviderEntity(
    val name: String,
    val authLevel: String
)

@Serializable
data class TFAProvidersEntity(
    val activeProviders: List<TFAProviderEntity> = emptyList(),
    val inactiveProviders: List<TFAProviderEntity> = emptyList()
)

@Serializable
data class TFAEmailEntity(
    val id: String,
    val obfuscated: String,
    val lastVerification: String,
)

enum class TFAPhoneMethod(val value: String) {
    SMS("sms"), VOICE("voice")
}

@Serializable
data class TFAPhoneEntity(
    val id: String,
    val obfuscated: String,
    val lastMethod: String,
    val lastVerification: String,
)


