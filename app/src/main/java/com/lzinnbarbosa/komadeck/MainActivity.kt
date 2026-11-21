package com.lzinnbarbosa.komadeck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lzinnbarbosa.komadeck.presentation.navigation.KomaDeckNavGraph
import com.lzinnbarbosa.komadeck.presentation.theme.KomaDeckTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KomaDeckTheme {
                KomaDeckNavGraph()
            }
        }
    }
}
