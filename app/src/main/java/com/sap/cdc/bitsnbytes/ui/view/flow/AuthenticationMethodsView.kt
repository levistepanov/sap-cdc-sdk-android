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
import com.sap.cdc.android.sdk.auth.ResolvableTFA
import com.sap.cdc.android.sdk.auth.tfa.TFAProvider
import com.sap.cdc.android.sdk.auth.tfa.TFAProviderEntity
import com.sap.cdc.android.sdk.auth.tfa.TFAProvidersEntity
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

        if (resolvableContext.tfa != null) {
            if (resolvableContext.tfa?.tfaProviders?.activeProviders?.isNotEmpty()!!) {
                // Active providers == TFA verification flow.
                AuthMethodsView(
                    resolvableContext.tfa?.tfaProviders?.activeProviders!!,
                    onItemClick = { provider ->
                        when (provider) {
                            TFAProvider.EMAIL.value -> {
                                NavigationCoordinator.INSTANCE.navigateUp()
                            }

                            TFAProvider.PHONE.value -> {
                                NavigationCoordinator.INSTANCE.navigateUp()
                            }

                            TFAProvider.TOTP.value -> {
                                NavigationCoordinator.INSTANCE.navigateUp()
                            }
                        }
                    }
                )
            } else if (resolvableContext.tfa?.tfaProviders?.inactiveProviders?.isNotEmpty()!!) {
                // Inactive providers == TFA registration flow.
                AuthMethodsView(
                    resolvableContext.tfa?.tfaProviders?.inactiveProviders!!,
                    onItemClick = { provider ->
                        when (provider) {
                            TFAProvider.PHONE.value -> {
                                // Start phone TFA registration flow.
                                NavigationCoordinator.INSTANCE.navigate(
                                    "${ProfileScreenRoute.RegisterPhone.route}/${resolvableContext.toJson()}"
                                )
                            }

                            TFAProvider.TOTP.value -> {
                                // Start TOTP TFA registration flow.
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.size(10.dp))

        LargeVerticalSpacer()

        ActionTextButton(
            "Back to login screen"
        ) {
            NavigationCoordinator.INSTANCE.navigateUp()
        }
    }
}

@Preview
@Composable
fun AuthMethodsScreenPreview() {
    AppTheme {
        AuthMethodsScreen(
            ResolvableContext(
                tfa = ResolvableTFA(
                    tfaProviders = TFAProvidersEntity(
                        activeProviders = listOf(
                            TFAProviderEntity(TFAProvider.EMAIL.value, "high"),
                            TFAProviderEntity(TFAProvider.PHONE.value, "high"),
                            TFAProviderEntity(TFAProvider.TOTP.value, "high")
                        )
                    )
                )
            )
        )
    }
}

@Composable
fun AuthMethodsView(
    authProviders: List<TFAProviderEntity>,
    onItemClick: (String) -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
    ) {
        authProviders.forEach {
            IconAndTextOutlineButton(
                modifier = Modifier.size(width = 240.dp, height = 44.dp),
                text = titleForAuthProvider(it.name),
                onClick = {
                    onItemClick(it.name)
                },
                iconResourceId = iconForAuthProvider(it.name),

                )
            Spacer(modifier = Modifier.size(10.dp))
        }
    }
}

@Preview
@Composable
fun AuthMethodsViewPreview() {
    AppTheme {
        AuthMethodsView(
            listOf(
                TFAProviderEntity(TFAProvider.PHONE.value, "high"),
                TFAProviderEntity(TFAProvider.TOTP.value, "high")
            )
        )
    }
}

@Composable
fun titleForAuthProvider(provider: String): String {
    when (provider) {
        TFAProvider.EMAIL.value -> {
            return "Send Code to Email"
        }

        TFAProvider.PHONE.value -> {
            return "Send Code to Phone"
        }

        TFAProvider.TOTP.value -> {
            return "Use a TOTP App"
        }

        else -> {
            return "Unknown"
        }
    }
}

@Composable
fun iconForAuthProvider(provider: String): Int {
    when (provider) {
        TFAProvider.EMAIL.value -> {
            return R.drawable.ic_email
        }

        TFAProvider.PHONE.value -> {
            return R.drawable.ic_device
        }

        TFAProvider.TOTP.value -> {
            return R.drawable.ic_lock
        }

        else -> {
            return R.drawable.ic_logo
        }
    }
}


