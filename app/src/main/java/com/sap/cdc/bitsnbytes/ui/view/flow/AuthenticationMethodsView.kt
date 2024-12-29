package com.sap.cdc.bitsnbytes.ui.view.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sap.cdc.android.sdk.auth.ResolvableContext
import com.sap.cdc.bitsnbytes.R
import com.sap.cdc.bitsnbytes.ui.route.NavigationCoordinator
import com.sap.cdc.bitsnbytes.ui.route.ProfileScreenRoute
import com.sap.cdc.bitsnbytes.ui.theme.AppTheme
import com.sap.cdc.bitsnbytes.ui.view.custom.ActionTextButton
import com.sap.cdc.bitsnbytes.ui.view.custom.IconAndTextOutlineButton
import com.sap.cdc.bitsnbytes.ui.view.custom.LargeVerticalSpacer
import com.sap.cdc.bitsnbytes.ui.view.custom.MediumVerticalSpacer
import com.sap.cdc.bitsnbytes.ui.view.custom.SmallVerticalSpacer

@Composable
fun AuthMethodsScreen(
    resolvableContext: ResolvableContext
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        LargeVerticalSpacer()
        Text("Auth Methods", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        SmallVerticalSpacer()
        Text("Use your preferred method", fontSize = 16.sp, fontWeight = FontWeight.Light)

        // Divider
        MediumVerticalSpacer()
        HorizontalDivider(
            modifier = Modifier.size(
                240.dp, 1.dp
            ), thickness = 1.dp, color = Color.LightGray
        )
        MediumVerticalSpacer()

        // Email TFA
        IconAndTextOutlineButton(
            modifier = Modifier.size(width = 240.dp, height = 44.dp),
            text = "Send Code to Email",
            onClick = {
                NavigationCoordinator.INSTANCE.navigate("${ProfileScreenRoute.AuthTabView.route}/1")
            },
            iconResourceId = R.drawable.ic_email,

            )
        Spacer(modifier = Modifier.size(10.dp))

        // Phone TFA
        IconAndTextOutlineButton(
            modifier = Modifier.size(width = 240.dp, height = 44.dp),
            text = "Send Code to Phone",
            onClick = {
                NavigationCoordinator.INSTANCE.navigate("${ProfileScreenRoute.AuthTabView.route}/1")
            },
            iconResourceId = R.drawable.ic_device,

            )
        Spacer(modifier = Modifier.size(10.dp))

        // TOTP TFA
        IconAndTextOutlineButton(
            modifier = Modifier.size(width = 240.dp, height = 44.dp),
            text = "Use a TOTP App",
            onClick = {
                NavigationCoordinator.INSTANCE.navigate("${ProfileScreenRoute.AuthTabView.route}/1")
            },
            iconResourceId = R.drawable.ic_lock,

            )
        Spacer(modifier = Modifier.size(10.dp))

        LargeVerticalSpacer()

        ActionTextButton(
            "Back to login screen"
        ) {

        }
    }
}

@Preview
@Composable
fun AuthMethodsScreenPreview() {
    AppTheme {
        AuthMethodsScreen(ResolvableContext())
    }
}