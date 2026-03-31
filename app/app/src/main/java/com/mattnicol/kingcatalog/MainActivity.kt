package com.mattnicol.kingcatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mattnicol.kingcatalog.ui.navigation.AppNavGraph
import com.mattnicol.kingcatalog.ui.theme.KingCatalogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KingCatalogTheme {
                AppNavGraph()
            }
        }
    }
}
