package com.tapshield.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tapshield.app.ui.navigation.TapShieldNavGraph
import com.tapshield.app.ui.theme.TapShieldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TapShieldTheme {
                TapShieldNavGraph()
            }
        }
    }
}