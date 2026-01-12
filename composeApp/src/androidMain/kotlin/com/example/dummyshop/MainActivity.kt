package com.example.dummyshop

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.dummyshop.navigation.DummyShopNavHost

class MainActivity : ComponentActivity() {

    private var latestIntent: Intent? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        latestIntent = intent

        setContent {
            val navController = rememberNavController()

            LaunchedEffect(latestIntent) {
                latestIntent?.let { navController.handleDeepLink(it) }
            }

            DummyShopNavHost(navController = navController)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        latestIntent = intent
    }
}
