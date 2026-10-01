package com.jarves.mh

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarves.mh.ui.MainViewModel
import com.jarves.mh.ui.PocketDevApp
import com.jarves.mh.ui.theme.PocketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val notificationRoute = savedInstanceState?.getString(EXTRA_NOTIFICATION_ROUTE)
            ?: intent.getStringExtra(EXTRA_NOTIFICATION_ROUTE)
        val vm = ViewModelProvider(this)[MainViewModel::class.java]
        vm.handleNotificationRoute(notificationRoute)
        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            PocketTheme(themeMode = state.themeMode) {
                PocketDevApp(vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(EXTRA_NOTIFICATION_ROUTE)?.let { route ->
            val vm = ViewModelProvider(this)[MainViewModel::class.java]
            vm.handleNotificationRoute(route)
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ROUTE = "notification_route"
    }
}
