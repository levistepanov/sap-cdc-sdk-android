package com.sap.cdc.bitsnbytes.ui.activity

import android.os.Bundle

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.sap.cdc.bitsnbytes.ui.theme.AppTheme

class CDCPushAuthActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppTheme.colorScheme.background
                ) {

                }
            }
        }
    }
}