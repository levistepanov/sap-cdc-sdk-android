package com.sap.cdc.bitsnbytes.ui.view.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sap.cdc.android.sdk.auth.ResolvableContext
import com.sap.cdc.bitsnbytes.ui.theme.AppTheme
import com.sap.cdc.bitsnbytes.ui.view.custom.ActionOutlineInverseButton
import com.sap.cdc.bitsnbytes.ui.view.custom.IndeterminateLinearIndicator
import com.sap.cdc.bitsnbytes.ui.view.custom.LoadingStateColumn
import com.sap.cdc.bitsnbytes.ui.view.custom.SimpleErrorMessages
import com.sap.cdc.bitsnbytes.ui.viewmodel.IRegisterPhoneViewModel
import com.sap.cdc.bitsnbytes.ui.viewmodel.RegisterPhoneViewModelPreview

@Composable
fun RegisterPhoneScreen(
    resolvableContext: ResolvableContext,
    viewModel: IRegisterPhoneViewModel,
) {
    var loading by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf("") }

    var inputField by remember {
        mutableStateOf("")
    }

    val focusManager = LocalFocusManager.current

    LoadingStateColumn(
        loading = loading,
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        // UI elements.
        IndeterminateLinearIndicator(loading)

        Spacer(modifier = Modifier.size(80.dp))
        Text("Register Phone Number", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.size(12.dp))
        Text("Please enter your phone number", fontSize = 16.sp, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.size(24.dp))

        Column(
            modifier = Modifier
                .padding(start = 48.dp, end = 48.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Phone Number:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
            )
            TextField(
                inputField,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Enter phone number",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                    )
                },
                textStyle = TextStyle(
                    color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Normal
                ),
                onValueChange = {
                    inputField = it
                },
                keyboardActions = KeyboardActions {
                    focusManager.moveFocus(FocusDirection.Next)
                },
            )
            Spacer(modifier = Modifier.size(48.dp))

            ActionOutlineInverseButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp),
                text = "Send code",
                onClick = {
                    // Send code logic
                },
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        // Error message
        if (registerError.isNotEmpty()) {
            SimpleErrorMessages(
                text = registerError
            )
        }
    }
}

@Composable
@Preview
fun RegisterPhoneScreenPreview() {
    AppTheme {
        RegisterPhoneScreen(
            resolvableContext = ResolvableContext(),
            viewModel = RegisterPhoneViewModelPreview()
        )
    }
}