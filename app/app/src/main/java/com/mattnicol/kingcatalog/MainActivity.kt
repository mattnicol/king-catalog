package com.mattnicol.kingcatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import com.mattnicol.kingcatalog.ui.navigation.AppNavGraph
import com.mattnicol.kingcatalog.ui.screens.splash.SplashScreen
import com.mattnicol.kingcatalog.ui.theme.KingCatalogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KingCatalogTheme {
                var showSplash by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(2000L)
                    showSplash = false
                }
                if (showSplash) {
                    SplashScreen()
                } else {
                    AppNavGraph()
                }
            }
        }
    }
}
