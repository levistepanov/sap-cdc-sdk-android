package com.sap.cdc.android.sdk.auth.flow

import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_ACCOUNTS_GET_ACCOUNT_INFO
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_ACCOUNTS_GET_CONFLICTING_ACCOUNTS
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_ACCOUNTS_ID_TOKEN_EXCHANGE
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_ACCOUNTS_SET_ACCOUNT_INFO
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_TFA_EMAILS_COMPLETE_VERIFICATION
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_TFA_EMAILS_SEND_CODE
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_TFA_EMAIL_GET
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_TFA_FINALIZE
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_TFA_GET_PROVIDERS
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_TFA_INIT
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_TFA_PHONE_COMPLETE_VERIFICATION
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_TFA_PHONE_GET
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_TFA_PHONE_SEND_CODE
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_TFA_PUSH_OPT_IN
import com.sap.cdc.android.sdk.auth.AuthEndpoints.Companion.EP_TFA_PUSH_VERIFY
import com.sap.cdc.android.sdk.auth.AuthResponse
import com.sap.cdc.android.sdk.auth.AuthenticationApi
import com.sap.cdc.android.sdk.auth.AuthenticationService.Companion.CDC_AUTHENTICATION_SERVICE_SECURE_PREFS
import com.sap.cdc.android.sdk.auth.AuthenticationService.Companion.CDC_DEVICE_INFO
import com.sap.cdc.android.sdk.auth.IAuthResponse
import com.sap.cdc.android.sdk.auth.ResolvableContext
import com.sap.cdc.android.sdk.auth.ResolvableTFA
import com.sap.cdc.android.sdk.auth.session.SessionService
import com.sap.cdc.android.sdk.auth.tfa.TFAEmailEntity
import com.sap.cdc.android.sdk.auth.tfa.TFAPhoneEntity
import com.sap.cdc.android.sdk.auth.tfa.TFAPhoneMethod
import com.sap.cdc.android.sdk.core.CoreClient
import com.sap.cdc.android.sdk.extensions.getEncryptedPreferences
import kotlinx.serialization.json.Json

/**
 * Created by Tal Mirmelshtein on 10/06/2024
 * Copyright: SAP LTD.
 */

class AccountAuthFlow(coreClient: CoreClient, sessionService: SessionService) :
    AuthFlow(coreClient, sessionService) {

    /**
     * Request updated account information.
     *
     * @see [accounts.getAccountInfo](https://help.sap.com/docs/SAP_CUSTOMER_DATA_CLOUD/8b8d6fffe113457094a17701f63e3d6a/cab69a86edae49e2be93fd51b78fc35b.html?q=accounts.getAccountInfo)
     */
    suspend fun getAccountInfo(parameters: MutableMap<String, String>? = mutableMapOf()): IAuthResponse {
        withParameters(parameters!!)
        val accountResponse =
            AuthenticationApi(coreClient, sessionService).genericSend(
                EP_ACCOUNTS_GET_ACCOUNT_INFO,
                this.parameters
            )
        return AuthResponse(accountResponse)
    }

    /**
     * Update account information.
     * NOTE: some account parameters needs to be JSON serialized.
     *
     * @see [accounts.setAccountInfo](https://help.sap.com/docs/SAP_CUSTOMER_DATA_CLOUD/8b8d6fffe113457094a17701f63e3d6a/41398a8670b21014bbc5a10ce4041860.html?q=accounts.getAccountInfo)
     */
    suspend fun setAccountInfo(parameters: MutableMap<String, String>? = mutableMapOf()): IAuthResponse {
        withParameters(parameters!!)
        val setAccountResponse =
            AuthenticationApi(coreClient, sessionService).genericSend(
                EP_ACCOUNTS_SET_ACCOUNT_INFO,
                this.parameters
            )
        return AuthResponse(setAccountResponse)
    }

    /**
     * Request conflicting accounts information.
     * NOTE: Call requires regToken due to interruption source.
     *
     * @see [accounts.getConflictingAccounts](https://help.sap.com/docs/SAP_CUSTOMER_DATA_CLOUD/8b8d6fffe113457094a17701f63e3d6a/4134d7df70b21014bbc5a10ce4041860.html?q=conflictingAccounts)
     */
    suspend fun getConflictingAccounts(parameters: MutableMap<String, String>? = mutableMapOf()): IAuthResponse {
        withParameters(parameters!!)
        val conflictingAccountsResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_ACCOUNTS_GET_CONFLICTING_ACCOUNTS,
            this.parameters
        )
        return AuthResponse(conflictingAccountsResponse)
    }

    /**
     * Applications (mobile/web) within the same site group are now able to share a session from the mobile application
     * to a web page running the JS SDK.
     *
     * Request code required to exchange the session.
     */
    suspend fun getAuthCode(parameters: MutableMap<String, String>? = mutableMapOf()): IAuthResponse {
        withParameters(parameters!!)
        parameters["resource"] = "urn:gigya:account" //TODO: check removing parameter?
        parameters["subject_token_type"] =
            "urn:gigya:token-type:mobile" //TODO: check removing parameter?
        parameters["response_type"] = "code"
        val exchangeAuthCodeResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_ACCOUNTS_ID_TOKEN_EXCHANGE,
            this.parameters
        )
        return AuthResponse(exchangeAuthCodeResponse)
    }


    /**
     * Request account two factor authentication providers:
     * Active - Providers that are currently active and the user can use to authenticate.
     * Inactive - Providers that are currently inactive and the user can activate to use for authentication.
     */
    suspend fun getTFAProviders(parameters: MutableMap<String, String>? = mutableMapOf()): IAuthResponse {
        withParameters(parameters!!)
        val tfaProvidersResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_GET_PROVIDERS,
            this.parameters
        )
        return AuthResponse(tfaProvidersResponse)
    }

    /**
     * Initiate push TFA registration.
     * NOTE: Requires deviceInfo to be sent.
     */
    suspend fun optInForPushTFA(): IAuthResponse {
        val initTFAResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_INIT,
            parameters
        )
        if (initTFAResponse.isError()) return AuthResponse(initTFAResponse)

        // Clear parameters for reuse.
        parameters.clear()

        val assertion = initTFAResponse.stringField("gigyaAssertion") ?: ""

        // Obtain device info from secure storage.
        val esp = coreClient.siteConfig.applicationContext.getEncryptedPreferences(
            CDC_AUTHENTICATION_SERVICE_SECURE_PREFS
        )
        val deviceInfo = esp.getString(CDC_DEVICE_INFO, "") ?: ""

        parameters["gigyaAssertion"] = assertion
        parameters["deviceInfo"] = deviceInfo
        val pushOptInResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_PUSH_OPT_IN,
            parameters
        )
        return AuthResponse(pushOptInResponse)
    }

    suspend fun finalizeOptInForPushTFA(): IAuthResponse {
        val verifyPushResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_PUSH_VERIFY,
            parameters
        )
        if (verifyPushResponse.isError()) return AuthResponse(verifyPushResponse)

        // Clear parameters for reuse.
        parameters.remove("verificationToken")
        parameters["providerAssertion"] = verifyPushResponse.stringField("providerAssertion") ?: ""

        val finalizePushResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_FINALIZE,
            parameters
        )
        return AuthResponse(finalizePushResponse)
    }

    suspend fun verifyPushTFA(): IAuthResponse {
        val verifyPushResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_PUSH_VERIFY,
            parameters
        )
        return AuthResponse(verifyPushResponse)
    }

    suspend fun getRegisteredEmails(resolvableContext: ResolvableContext): IAuthResponse {
        val initTFAResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_INIT,
            parameters
        )
        if (initTFAResponse.isError()) return AuthResponse(initTFAResponse)

        // Clear parameters for reuse.
        parameters.clear()

        val assertion = initTFAResponse.stringField("gigyaAssertion") ?: ""

        parameters["gigyaAssertion"] = assertion
        val getEmailsResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_EMAIL_GET,
            parameters
        )
        if (getEmailsResponse.isError()) return AuthResponse(getEmailsResponse)

        val emailsJson = getEmailsResponse.stringField("emails")
        val emails = Json.decodeFromString<List<TFAEmailEntity>>(emailsJson!!)

        val authResponse = AuthResponse(getEmailsResponse)
        resolvableContext.tfa?.assertion = assertion
        resolvableContext.tfa?.emails = emails
        authResponse.resolvableContext = resolvableContext
        return authResponse
    }

    suspend fun sendEmailCode(resolvableContext: ResolvableContext): IAuthResponse {
        val sendCodeResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_EMAILS_SEND_CODE,
            parameters
        )
        if (sendCodeResponse.isError()) return AuthResponse(sendCodeResponse)
        val phvToken = sendCodeResponse.stringField("phvToken") ?: ""
        val authResponse = AuthResponse(sendCodeResponse)
        resolvableContext.tfa?.phvToken = phvToken
        authResponse.resolvableContext = resolvableContext
        return authResponse
    }

    suspend fun registerPhone(
        resolvableContext: ResolvableContext,
        phoneNumber: String
    ): IAuthResponse {
        val initTFAResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_INIT,
            parameters
        )

        // Clear parameters for reuse.
        parameters.remove("provider")

        val assertion = initTFAResponse.stringField("gigyaAssertion") ?: ""

        parameters["gigyaAssertion"] = assertion
        parameters["phone"] = phoneNumber
        if (initTFAResponse.isError()) return AuthResponse(initTFAResponse)
        val sendCodeResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_PHONE_SEND_CODE,
            parameters
        )
        val phvToken = sendCodeResponse.stringField("phvToken") ?: ""
        val authResponse = AuthResponse(sendCodeResponse)
        resolvableContext.tfa?.phvToken = phvToken
        authResponse.resolvableContext = resolvableContext
        return authResponse
    }

    suspend fun getRegisteredPhoneNumbers(resolvableContext: ResolvableContext): IAuthResponse {
        val initTFAResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_INIT,
            parameters
        )
        if (initTFAResponse.isError()) return AuthResponse(initTFAResponse)

        // Clear parameters for reuse.
        parameters.remove("provider")
        parameters.remove("mode")

        val assertion = initTFAResponse.stringField("gigyaAssertion") ?: ""

        parameters["gigyaAssertion"] = assertion
        val getPhoneNumbersResult = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_PHONE_GET,
            parameters
        )
        if (getPhoneNumbersResult.isError()) return AuthResponse(getPhoneNumbersResult)

        val phonesJson = getPhoneNumbersResult.stringField("phones")
        val phones = Json.decodeFromString<List<TFAPhoneEntity>>(phonesJson!!)

        val authResponse = AuthResponse(getPhoneNumbersResult)
        resolvableContext.tfa?.assertion = assertion
        resolvableContext.tfa?.phones = phones
        authResponse.resolvableContext = resolvableContext
        return authResponse
    }

    suspend fun sendPhoneCode(resolvableContext: ResolvableContext): IAuthResponse {
        val sendCodeResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_PHONE_SEND_CODE,
            parameters
        )
        if (sendCodeResponse.isError()) return AuthResponse(sendCodeResponse)
        val phvToken = sendCodeResponse.stringField("phvToken") ?: ""
        val authResponse = AuthResponse(sendCodeResponse)
        resolvableContext.tfa?.phvToken = phvToken
        authResponse.resolvableContext = resolvableContext
        return authResponse
    }

    suspend fun verifyCode(
        resolvableContext: ResolvableContext,
        provider: String,
        rememberDevice: Boolean
    ): IAuthResponse {
        val verifyCodeResponse = AuthenticationApi(coreClient, sessionService).genericSend(
            when (provider) {
                "email" -> EP_TFA_EMAILS_COMPLETE_VERIFICATION
                "phone" -> EP_TFA_PHONE_COMPLETE_VERIFICATION
                else -> "" // Redundant.
            },
            parameters
        )
        if (verifyCodeResponse.isError()) return AuthResponse(verifyCodeResponse)

        // Clear parameters for reuse.
        parameters.clear()

        val providerAssertion = verifyCodeResponse.stringField("providerAssertion") ?: ""

        parameters["regToken"] = resolvableContext.regToken ?: ""
        parameters["gigyaAssertion"] = resolvableContext.tfa?.assertion ?: ""
        parameters["providerAssertion"] = providerAssertion
        parameters["tempDevice"] = (!rememberDevice).toString()
        val finalizeTFAResult = AuthenticationApi(coreClient, sessionService).genericSend(
            EP_TFA_FINALIZE,
            parameters
        )
        //TODO: call finalize registration?
        return AuthResponse(finalizeTFAResult)
    }
}