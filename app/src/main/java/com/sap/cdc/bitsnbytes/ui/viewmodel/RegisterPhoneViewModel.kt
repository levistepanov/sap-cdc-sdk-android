package com.sap.cdc.bitsnbytes.ui.viewmodel

import android.content.Context
import com.sap.cdc.android.sdk.auth.ResolvableContext

interface IRegisterPhoneViewModel {

    suspend fun registerPhone(
        phoneNumber: String,
        resolvableContext: ResolvableContext
    ) {
        //Stub.
    }
}


class RegisterPhoneViewModelPreview : IRegisterPhoneViewModel

class RegisterPhoneViewModel(context: Context) : BaseViewModel(context), IRegisterPhoneViewModel {

}