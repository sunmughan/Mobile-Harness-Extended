package com.jarves.mh

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarves.mh.ui.MainViewModel
import com.jarves.mh.ui.PocketDevApp
import com.jarves.mh.ui.theme.PocketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val notificationRoute = savedInstanceState?.getStringExtra(EXTRA_NOTIFICATION_ROUTE)
            ?: intent.getStringExtra(EXTRA_NOTIFICATION_ROUTE)
        setContent {
            val vm: MainViewModel = viewModel()
            vm.handleNotificationRoute(notificationRoute)
            val state by vm.state.collectAsStateWithLifecycle()
            PocketTheme(themeMode = state.themeMode) {
                PocketDevApp(vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.getStringExtra(EXTRA_NOTIFICATION_ROUTE)?.let { route ->
            val vm: MainViewModel = viewModel()
            vm.handleNotificationRoute(route)
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ROUTE = "notification_route"
    }
}
